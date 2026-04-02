package com.thinkingerp.domain.model

data class SellInvoice(
    val id: String,
    val invoiceNumber: String,
    val invoiceDate: Long,
    val customerName: String = "Cash",
    val customerGstin: String = "",
    val items: List<SellInvoiceItem> = emptyList(),
    val isInterState: Boolean = false,
    val notes: String = "",
) {
    val subtotal: Double get() = items.sumOf { it.lineTotal }
    val cgst: Double get() = if (isInterState) 0.0 else items.sumOf { it.cgst }
    val sgst: Double get() = if (isInterState) 0.0 else items.sumOf { it.sgst }
    val igst: Double get() = if (isInterState) items.sumOf { it.igst } else 0.0
    val total: Double get() = subtotal + cgst + sgst + igst
}

data class SellInvoiceItem(
    val id: String,
    val invoiceId: String,
    val barcode: String,
    val itemName: String,
    val quantity: Int,
    val sellPrice: Double,
    val mrp: Double,
    val gstRate: Double,
    val isInterState: Boolean = false,
) {
    val taxableAmount: Double get() = sellPrice * quantity
    val cgst: Double get() = if (isInterState) 0.0 else taxableAmount * gstRate / 200.0
    val sgst: Double get() = if (isInterState) 0.0 else taxableAmount * gstRate / 200.0
    val igst: Double get() = if (isInterState) taxableAmount * gstRate / 100.0 else 0.0
    val lineTotal: Double get() = taxableAmount + cgst + sgst + igst
}
