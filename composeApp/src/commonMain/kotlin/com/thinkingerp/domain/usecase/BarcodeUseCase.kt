package com.thinkingerp.domain.usecase

/**
 * Validates an EAN-13 barcode (13 digits) using its check digit.
 * Odd positions (1,3,5..11) × 1, even positions (2,4,6..12) × 3.
 */
fun validateEan13(barcode: String): Boolean {
    if (barcode.length != 13 || !barcode.all { it.isDigit() }) return false
    val digits = barcode.map { it.digitToInt() }
    val sum = digits.dropLast(1).mapIndexed { i, d ->
        if (i % 2 == 0) d else d * 3
    }.sum()
    val checkDigit = (10 - sum % 10) % 10
    return checkDigit == digits.last()
}
