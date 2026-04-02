package com.thinkingerp.domain.model

data class Item(
    val barcode: String,       // UPC-A 12-digit
    val name: String,
    val company: String,
    val mrp: Double,
    val hsnCode: String,
    val gstRate: Double,       // 0, 5, 12, 18, 28
    val quantity: Int,
    val unit: String = "PCS",
)
