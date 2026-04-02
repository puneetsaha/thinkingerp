package com.thinkingerp.ui.screens.purchase

import com.thinkingerp.currentMillis
import com.thinkingerp.data.repository.InventoryRepository
import com.thinkingerp.generateId
import com.thinkingerp.domain.model.Item
import com.thinkingerp.domain.model.PurchaseBill
import com.thinkingerp.domain.model.PurchaseBillItem
import com.thinkingerp.domain.usecase.validateEan13
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BillLineItem(
    val item: Item,
    val quantity: Int = 1,
    val purchasePrice: Double = item.mrp,
) {
    val id: String get() = item.barcode
}

data class PurchaseBillUiState(
    val supplierName: String = "",
    val lines: List<BillLineItem> = emptyList(),
    val isInterState: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
) {
    val subtotal get() = lines.sumOf { it.purchasePrice * it.quantity }
    val cgst get() = if (isInterState) 0.0 else lines.sumOf { it.purchasePrice * it.quantity * it.item.gstRate / 200.0 }
    val sgst get() = cgst
    val igst get() = if (isInterState) lines.sumOf { it.purchasePrice * it.quantity * it.item.gstRate / 100.0 } else 0.0
    val total get() = subtotal + cgst + sgst + igst
}

class PurchaseBillViewModel(private val repo: InventoryRepository) {

    private val _state = MutableStateFlow(PurchaseBillUiState())
    val state: StateFlow<PurchaseBillUiState> = _state.asStateFlow()

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
        _state.update { s ->
            val existing = s.lines.indexOfFirst { it.item.barcode == barcode }
            val newLines = if (existing >= 0) {
                s.lines.toMutableList().also {
                    it[existing] = it[existing].copy(quantity = it[existing].quantity + 1)
                }
            } else {
                s.lines + BillLineItem(item)
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
            s.copy(lines = s.lines.map { if (it.item.barcode == barcode) it.copy(purchasePrice = price) else it })
        }
    }

    fun removeLine(barcode: String) {
        _state.update { it.copy(lines = it.lines.filter { l -> l.item.barcode != barcode }) }
    }

    fun onSupplierChanged(name: String) = _state.update { it.copy(supplierName = name) }

    fun onInterStateToggled(value: Boolean) = _state.update { it.copy(isInterState = value) }

    fun clearError() = _state.update { it.copy(error = null) }

    fun saveBill() {
        val s = _state.value
        if (s.lines.isEmpty()) {
            _state.update { it.copy(error = "Add at least one item") }
            return
        }
        val bill = PurchaseBill(
            id = generateId(),
            billNumber = "PB-${currentMillis()}",
            billDate = currentMillis(),
            supplierName = s.supplierName.ifBlank { "Unknown Supplier" },
            items = s.lines.map { line ->
                PurchaseBillItem(
                    id = generateId(),
                    billId = "",
                    barcode = line.item.barcode,
                    itemName = line.item.name,
                    quantity = line.quantity,
                    purchasePrice = line.purchasePrice,
                    mrp = line.item.mrp,
                    gstRate = line.item.gstRate,
                    isInterState = s.isInterState,
                )
            },
            isInterState = s.isInterState,
        )
        repo.savePurchaseBill(bill)
        _state.update { PurchaseBillUiState(saved = true) }
    }
}

