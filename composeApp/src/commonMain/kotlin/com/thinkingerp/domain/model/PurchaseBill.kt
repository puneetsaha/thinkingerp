package com.thinkingerp.domain.model

data class PurchaseBill(
    val id: String,
    val billNumber: String,
    val billDate: Long,
    val supplierName: String,
    val supplierGstin: String = "",
    val items: List<PurchaseBillItem> = emptyList(),
    val isInterState: Boolean = false,
    val notes: String = "",
) {
    val subtotal: Double get() = items.sumOf { it.lineTotal }
    val cgst: Double get() = if (isInterState) 0.0 else items.sumOf { it.cgst }
    val sgst: Double get() = if (isInterState) 0.0 else items.sumOf { it.sgst }
    val igst: Double get() = if (isInterState) items.sumOf { it.igst } else 0.0
    val total: Double get() = subtotal + cgst + sgst + igst
}

data class PurchaseBillItem(
    val id: String,
    val billId: String,
    val barcode: String,
    val itemName: String,
    val quantity: Int,
    val purchasePrice: Double,
    val mrp: Double,
    val gstRate: Double,
    val isInterState: Boolean = false,
) {
    val taxableAmount: Double get() = purchasePrice * quantity
    val cgst: Double get() = if (isInterState) 0.0 else taxableAmount * gstRate / 200.0
    val sgst: Double get() = if (isInterState) 0.0 else taxableAmount * gstRate / 200.0
    val igst: Double get() = if (isInterState) taxableAmount * gstRate / 100.0 else 0.0
    val lineTotal: Double get() = taxableAmount + cgst + sgst + igst
}
