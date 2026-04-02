package com.thinkingerp.data

import com.thinkingerp.data.repository.InventoryRepository
import com.thinkingerp.domain.model.Item

/**
 * Seeds dummy items for testing. Safe to call on every launch — skips if data exists.
 *
 * Barcodes are valid EAN-13 (13-digit, check digit verified), using Indian GS1 prefix 890:
 *   8901234000007 — Surf Excel 1kg
 *   8901234000014 — Colgate Strong Teeth 100ml
 *   8901234000021 — Dettol Antiseptic 200ml
 *   8901234000038 — Ariel Matic 500g
 *   8901234000045 — Lifebuoy Soap 75g
 */
object DataSeeder {

    val dummyBarcodes = listOf(
        "8901234000007" to "Surf Excel 1kg",
        "8901234000014" to "Colgate Strong Teeth 100ml",
        "8901234000021" to "Dettol Antiseptic 200ml",
        "8901234000038" to "Ariel Matic 500g",
        "8901234000045" to "Lifebuoy Soap 75g",
    )

    fun seed(repo: InventoryRepository) {
        if (repo.getAllItems().isNotEmpty()) return

        val items = listOf(
            Item(
                barcode = "8901234000007",
                name = "Surf Excel 1kg",
                company = "HUL",
                mrp = 120.0,
                hsnCode = "3402",
                gstRate = 18.0,
                quantity = 50,
                unit = "PKT",
            ),
            Item(
                barcode = "8901234000014",
                name = "Colgate Strong Teeth 100ml",
                company = "Colgate-Palmolive",
                mrp = 55.0,
                hsnCode = "3306",
                gstRate = 12.0,
                quantity = 80,
                unit = "PCS",
            ),
            Item(
                barcode = "8901234000021",
                name = "Dettol Antiseptic 200ml",
                company = "Reckitt",
                mrp = 95.0,
                hsnCode = "3808",
                gstRate = 18.0,
                quantity = 30,
                unit = "BTL",
            ),
            Item(
                barcode = "8901234000038",
                name = "Ariel Matic 500g",
                company = "P&G",
                mrp = 180.0,
                hsnCode = "3402",
                gstRate = 18.0,
                quantity = 40,
                unit = "PKT",
            ),
            Item(
                barcode = "8901234000045",
                name = "Lifebuoy Soap 75g",
                company = "HUL",
                mrp = 28.0,
                hsnCode = "3401",
                gstRate = 12.0,
                quantity = 120,
                unit = "PCS",
            ),
        )
        items.forEach { repo.upsertItem(it) }
    }
}
