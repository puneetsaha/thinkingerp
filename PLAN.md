# ThinkingERP — Implementation Plan

## Project Summary
KMP store-keeper ERP for Android + Windows. GST-compliant billing, EAN-13 barcode scanning,
SQLite inventory, Claude AI natural language queries, PDF invoices.
Future: lightweight backend server with phone as thin client.

---

## Status Overview

| Phase | What | Status |
|-------|------|--------|
| 1 | Scaffold, build config, domain models, DB schema | ✅ Done |
| 2 | InventoryRepository (single repo), DataSeeder, Platform utils | ✅ Done |
| 3 | PurchaseBillViewModel + screen (full flow) | ✅ Done |
| 4 | SellInvoiceViewModel + screen | ✅ Done |
| 5 | SettingsViewModel + screen (store name, GSTIN, API key) | ✅ Done |
| 6 | NaturalLanguageQueryViewModel + screen (Claude API → SQL → table) | ✅ Done |
| 7 | RawQueryExecutor | ✅ Done |
| 8 | Barcode scanner — Android (CameraX + ML Kit) + Desktop (USB wedge) | ⏳ Pending |
| 9 | PDF invoice generation — Android (PdfDocument) + Desktop (PDFBox) | ⏳ Pending |
| 10 | Backend server + thin client Android app | ⏳ Pending (last) |

---

## Phase 8 — Barcode Scanner (next)

**`commonMain/barcode/BarcodeScanner.kt`**
```kotlin
expect class BarcodeScanner {
    @Composable
    fun ScannerView(onBarcodeDetected: (String) -> Unit, modifier: Modifier = Modifier)
}
```

**`androidMain/barcode/BarcodeScanner.android.kt`**
- `AndroidView { PreviewView }` + CameraX `ImageAnalysis` + ML Kit `BarcodeScanning.getClient(FORMAT_EAN_13)`
- Debounce: same barcode ignored within 2 seconds
- Validate with `validateEan13()` before firing callback
- Requires CAMERA permission check at call site

**`desktopMain/barcode/BarcodeScanner.desktop.kt`**
- `OutlinedTextField` that accumulates characters from USB wedge scanner
- Fires callback when 13 digits received + Enter key
- Validates with `validateEan13()`

**Gradle deps to add (`libs.versions.toml`):**
```toml
camerax = "1.4.1"
camerax-core      = { module = "androidx.camera:camera-core",     version.ref = "camerax" }
camerax-camera2   = { module = "androidx.camera:camera-camera2",  version.ref = "camerax" }
camerax-lifecycle = { module = "androidx.camera:camera-lifecycle", version.ref = "camerax" }
camerax-view      = { module = "androidx.camera:camera-view",     version.ref = "camerax" }
```

**Wire into screens:** Replace the manual barcode text field in `PurchaseBillScreen` and `SellInvoiceScreen` with `BarcodeScanner().ScannerView(...)`. Keep the manual field as fallback.

---

## Phase 9 — PDF Invoice Generation

**`commonMain/pdf/PdfLayoutData.kt`**
Shared data class pre-formatting all strings (date, money, GST). Both platform actuals consume this.

**`commonMain/pdf/PdfGenerator.kt`**
```kotlin
expect class PdfGenerator {
    fun generatePurchasePdf(bill: PurchaseBill, storeName: String, storeGstin: String): ByteArray
    fun generateSellPdf(invoice: SellInvoice, storeName: String, storeGstin: String): ByteArray
}
```

**`androidMain/pdf/PdfGenerator.android.kt`**
`android.graphics.pdf.PdfDocument` (A4: 595×842pt). Canvas + Paint.
Layout: store header → bill number + date → supplier/customer → item table (Item | Qty | Rate | GST% | CGST | SGST/IGST | Total) → GST summary → grand total.

**`desktopMain/pdf/PdfGenerator.desktop.kt`**
PDFBox 3.x (`PDDocument`, `PDPage`, `PDPageContentStream`). Same layout. Save to `ByteArrayOutputStream`.

**Gradle deps to add:**
```toml
pdfbox = "3.0.3"
pdfbox = { module = "org.apache.pdfbox:pdfbox", version.ref = "pdfbox" }
```

**Wire into ViewModels:** After `saveBill()` / `saveInvoice()` succeeds, call `PdfGenerator` → store `pdfBytes` in state → screen shows "Download PDF" button → platform file-save dialog.

---

## Phase 10 — Backend Server + Thin Client (last)

**Goal:** Move business logic and DB to a lightweight server. Android app becomes a thin client — only UI, no local DB.

**Server stack (Kotlin):**
- **Ktor** server (already a dependency — same lib, server mode)
- **SQLite** via SQLDelight (same schema, runs on the server machine — likely the Windows desktop)
- Exposes REST API: `POST /purchase`, `POST /sell`, `GET /inventory`, `POST /query`
- Claude API calls move to server side (API key never on phone)
- Runs as a Windows service or background process on the store's PC

**Android thin client:**
- Replaces `InventoryRepository` calls with HTTP calls via Ktor client
- Needs server IP/port configured in Settings screen
- Offline mode: show cached last-known inventory; block saves when disconnected

**Architecture:**
```
Android phone (thin client)          Windows PC (server)
  ┌─────────────────────┐              ┌──────────────────────┐
  │  Compose UI          │   HTTP/LAN  │  Ktor server          │
  │  Ktor client         │ ──────────► │  InventoryRepository  │
  │  No local DB         │             │  SQLite DB            │
  └─────────────────────┘             │  Claude API calls     │
                                       └──────────────────────┘
```

**New module:** `:server` (Kotlin JVM, Ktor server, shares domain models with `:composeApp` via a `:shared` module)

**Settings screen additions:** Server URL field (e.g. `http://192.168.1.5:8080`) + connection test button.

---

## Verification (end-to-end)

1. Build: `./gradlew :composeApp:assembleDebug` — APK builds clean
2. Purchase Bill: tap sample barcode → item form appears → save → inventory qty increases
3. Sell Invoice: same flow → stock decreases, blocked if insufficient
4. Settings: enter Claude API key → save
5. Ask Inventory: type "which items have less than 20 units?" → SQL shown → results table populated
6. PDF: save bill → PDF bytes generated → file saved
7. Camera scanner (Android): point at real EAN-13 barcode → auto-fills form
8. Server mode: Android connects to Windows PC over LAN → all operations go through server
