package com.thinkingerp.ui.screens.sell

import com.thinkingerp.currentMillis
import com.thinkingerp.data.repository.InventoryRepository
import com.thinkingerp.data.repository.InsufficientStockException
import com.thinkingerp.domain.model.Item
import com.thinkingerp.domain.model.SellInvoice
import com.thinkingerp.domain.model.SellInvoiceItem
import com.thinkingerp.domain.usecase.validateEan13
import com.thinkingerp.generateId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SellLineItem(
    val item: Item,
    val quantity: Int = 1,
    val sellPrice: Double = item.mrp,
) {
    val id: String get() = item.barcode
    val isOverStock: Boolean get() = quantity > item.quantity
}

data class SellInvoiceUiState(
    val customerName: String = "",
    val lines: List<SellLineItem> = emptyList(),
    val isInterState: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
) {
    val subtotal get() = lines.sumOf { it.sellPrice * it.quantity }
    val cgst get() = if (isInterState) 0.0 else lines.sumOf { it.sellPrice * it.quantity * it.item.gstRate / 200.0 }
    val sgst get() = cgst
    val igst get() = if (isInterState) lines.sumOf { it.sellPrice * it.quantity * it.item.gstRate / 100.0 } else 0.0
    val total get() = subtotal + cgst + sgst + igst
    val hasStockError get() = lines.any { it.isOverStock }
}

class SellInvoiceViewModel(private val repo: InventoryRepository) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(SellInvoiceUiState())
    val state: StateFlow<SellInvoiceUiState> = _state.asStateFlow()

    fun onBarcodeScanned(barcode: String) {
        if (!validateEan13(barcode)) {
            _state.update { it.copy(error = "Invalid EAN-13 barcode: $barcode") }
            return
        }
        val item = repo.getItemByBarcode(barcode)
        if (item == null) {
            _state.update { it.copy(error = "Item not found for barcode $barcode") }
            return
        }
        if (item.quantity <= 0) {
            _state.update { it.copy(error = "${item.name} is out of stock") }
            return
        }
        _state.update { s ->
            val existing = s.lines.indexOfFirst { it.item.barcode == barcode }
            val newLines = if (existing >= 0) {
                s.lines.toMutableList().also {
                    it[existing] = it[existing].copy(quantity = it[existing].quantity + 1)
                }
            } else {
                s.lines + SellLineItem(item)
            }
            s.copy(lines = newLines, error = null)
        }
    }

    fun onQuantityChanged(barcode: String, qty: Int) {
        if (qty <= 0) { removeLine(barcode); return }
        _state.update { s ->
            s.copy(lines = s.lines.map { if (it.item.barcode == barcode) it.copy(quantity = qty) else it })
        }
    }

    fun onPriceChanged(barcode: String, price: Double) {
        _state.update { s ->
            s.copy(lines = s.lines.map { if (it.item.barcode == barcode) it.copy(sellPrice = price) else it })
        }
    }

    fun removeLine(barcode: String) {
        _state.update { it.copy(lines = it.lines.filter { l -> l.item.barcode != barcode }) }
    }

    fun onCustomerChanged(name: String) = _state.update { it.copy(customerName = name) }

    fun onInterStateToggled(value: Boolean) = _state.update { it.copy(isInterState = value) }

    fun clearError() = _state.update { it.copy(error = null) }

    fun saveInvoice() {
        val s = _state.value
        if (s.lines.isEmpty()) {
            _state.update { it.copy(error = "Add at least one item") }
            return
        }
        if (s.hasStockError) {
            _state.update { it.copy(error = "Some items exceed available stock") }
            return
        }
        val invoice = SellInvoice(
            id = generateId(),
            invoiceNumber = "SI-${currentMillis()}",
            invoiceDate = currentMillis(),
            customerName = s.customerName.ifBlank { "Cash" },
            items = s.lines.map { line ->
                SellInvoiceItem(
                    id = generateId(),
                    invoiceId = "",
                    barcode = line.item.barcode,
                    itemName = line.item.name,
                    quantity = line.quantity,
                    sellPrice = line.sellPrice,
                    mrp = line.item.mrp,
                    gstRate = line.item.gstRate,
                    isInterState = s.isInterState,
                )
            },
            isInterState = s.isInterState,
        )
        _state.update { it.copy(isLoading = true) }
        scope.launch(Dispatchers.Default) {
            try {
                repo.saveSellInvoice(invoice)
                _state.update { SellInvoiceUiState(saved = true) }
            } catch (e: InsufficientStockException) {
                _state.update { it.copy(isLoading = false, error = "Insufficient stock: ${e.itemName} (available: ${e.available})") }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Save failed: ${e.message}") }
            }
        }
    }
}
