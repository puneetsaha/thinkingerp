package com.thinkingerp.data.repository

import com.thinkingerp.currentMillis
import com.thinkingerp.data.database.ThinkingERPDatabase
import com.thinkingerp.domain.model.Item
import com.thinkingerp.domain.model.PurchaseBill
import com.thinkingerp.domain.model.PurchaseBillItem
import com.thinkingerp.domain.model.SellInvoice
import com.thinkingerp.domain.model.SellInvoiceItem

class InventoryRepository(private val db: ThinkingERPDatabase) {

    private val queries = db.thinkingERPDatabaseQueries

    // ─── Items ───────────────────────────────────────────────────────────────

    fun getItemByBarcode(barcode: String): Item? {
        val r = queries.getItemByBarcode(barcode).executeAsOneOrNull() ?: return null
        return mapItem(r.barcode, r.name, r.company, r.mrp, r.hsn_code, r.gst_rate, r.quantity, r.unit)
    }

    fun getAllItems(): List<Item> =
        queries.getAllItems().executeAsList().map {
            mapItem(it.barcode, it.name, it.company, it.mrp, it.hsn_code, it.gst_rate, it.quantity, it.unit)
        }

    fun upsertItem(item: Item) {
        val now = currentMillis()
        queries.upsertItem(
            barcode = item.barcode,
            name = item.name,
            company = item.company,
            mrp = item.mrp,
            hsn_code = item.hsnCode,
            gst_rate = item.gstRate,
            quantity = item.quantity.toLong(),
            unit = item.unit,
            created_at = now,
            updated_at = now,
        )
    }

    // ─── Purchase Bill ────────────────────────────────────────────────────────

    fun savePurchaseBill(bill: PurchaseBill) {
        db.transaction {
            queries.insertPurchaseBill(
                id = bill.id,
                bill_number = bill.billNumber,
                bill_date = bill.billDate,
                supplier_name = bill.supplierName,
                supplier_gstin = bill.supplierGstin,
                subtotal = bill.subtotal,
                cgst = bill.cgst,
                sgst = bill.sgst,
                igst = bill.igst,
                total = bill.total,
                is_inter_state = if (bill.isInterState) 1L else 0L,
                notes = bill.notes,
                created_at = currentMillis(),
            )
            bill.items.forEach { item ->
                queries.insertPurchaseBillItem(
                    id = item.id,
                    bill_id = bill.id,
                    barcode = item.barcode,
                    item_name = item.itemName,
                    quantity = item.quantity.toLong(),
                    purchase_price = item.purchasePrice,
                    mrp = item.mrp,
                    gst_rate = item.gstRate,
                    cgst = item.cgst,
                    sgst = item.sgst,
                    igst = item.igst,
                    line_total = item.lineTotal,
                )
                queries.updateItemQuantity(
                    delta = item.quantity.toLong(),
                    updatedAt = currentMillis(),
                    barcode = item.barcode,
                )
            }
        }
    }

    // ─── Sell Invoice ─────────────────────────────────────────────────────────

    fun saveSellInvoice(invoice: SellInvoice) {
        invoice.items.forEach { item ->
            val current = getItemByBarcode(item.barcode)
            if (current == null || current.quantity < item.quantity) {
                throw InsufficientStockException(item.itemName, current?.quantity ?: 0)
            }
        }
        db.transaction {
            queries.insertSellInvoice(
                id = invoice.id,
                invoice_number = invoice.invoiceNumber,
                invoice_date = invoice.invoiceDate,
                customer_name = invoice.customerName,
                customer_gstin = invoice.customerGstin,
                subtotal = invoice.subtotal,
                cgst = invoice.cgst,
                sgst = invoice.sgst,
                igst = invoice.igst,
                total = invoice.total,
                is_inter_state = if (invoice.isInterState) 1L else 0L,
                notes = invoice.notes,
                created_at = currentMillis(),
            )
            invoice.items.forEach { item ->
                queries.insertSellInvoiceItem(
                    id = item.id,
                    invoice_id = invoice.id,
                    barcode = item.barcode,
                    item_name = item.itemName,
                    quantity = item.quantity.toLong(),
                    sell_price = item.sellPrice,
                    mrp = item.mrp,
                    gst_rate = item.gstRate,
                    cgst = item.cgst,
                    sgst = item.sgst,
                    igst = item.igst,
                    line_total = item.lineTotal,
                )
                queries.updateItemQuantity(
                    delta = -item.quantity.toLong(),
                    updatedAt = currentMillis(),
                    barcode = item.barcode,
                )
            }
        }
    }

    // ─── Settings ─────────────────────────────────────────────────────────────

    fun getSetting(key: String): String? =
        queries.getSetting(key).executeAsOneOrNull()

    fun setSetting(key: String, value: String) =
        queries.upsertSetting(key, value)

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private fun mapItem(
        barcode: String, name: String, company: String, mrp: Double,
        hsnCode: String, gstRate: Double, quantity: Long, unit: String,
    ) = Item(barcode, name, company, mrp, hsnCode, gstRate, quantity.toInt(), unit)
}

class InsufficientStockException(val itemName: String, val available: Int) :
    Exception("Insufficient stock for $itemName (available: $available)")

