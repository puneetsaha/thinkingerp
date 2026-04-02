# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ThinkingERP** is a store-keeper accounting app for the Indian market (GST-compliant), targeting **Android and Windows desktop** from a single Kotlin codebase.

Core features:
- **Purchase Bill** — scan EAN-13 barcode → add items to inventory
- **Sell Invoice** — scan EAN-13 barcode → deduct from inventory
- **GST support** — CGST+SGST (intra-state) / IGST (inter-state) per line item
- **Natural language inventory queries** — powered by Claude API (claude-sonnet-4-6)
- Lightweight standalone SQLite DB (~50k items max, no cloud dependency)

## Tech Stack

| Layer | Library |
|-------|---------|
| UI | Compose Multiplatform (JetBrains) |
| Navigation | `org.jetbrains.androidx.navigation:navigation-compose` |
| Database | SQLDelight 2 — schema in `ThinkingERPDatabase.sq` |
| HTTP (Claude API) | Ktor client |
| DI | Koin 4 |
| Barcode — Android | ML Kit Barcode Scanning |
| Barcode — Desktop | ZXing core |

## Build & Run Commands

```bash
# Run on Android (connected device or emulator)
./gradlew :composeApp:installDebug

# Run on Desktop (macOS/Windows)
./gradlew :composeApp:run

# Build Windows installer (Exe/MSI)
./gradlew :composeApp:packageMsi

# Build release APK
./gradlew :composeApp:assembleRelease

# Run all tests
./gradlew :composeApp:allTests

# Regenerate SQLDelight code after schema changes
./gradlew :composeApp:generateCommonMainThinkingERPDatabaseInterface
```

Open the project in **Android Studio** (Ladybug or newer with KMP plugin).

## Architecture

```
composeApp/src/
├── commonMain/          # Shared across Android + Desktop
│   ├── kotlin/com/thinkingerp/
│   │   ├── App.kt                        # Root @Composable
│   │   ├── ai/ClaudeQueryService.kt      # Claude API → SQL generation
│   │   ├── data/database/
│   │   │   └── DatabaseFactory.kt        # expect class, platform-specific drivers
│   │   ├── domain/model/                 # Item, PurchaseBill, SellInvoice domain models
│   │   ├── domain/usecase/               # BarcodeUseCase (EAN-13 validation)
│   │   └── ui/
│   │       ├── navigation/AppNavigation.kt
│   │       └── screens/{dashboard,purchase,sell,query}/
│   └── sqldelight/com/thinkingerp/
│       └── ThinkingERPDatabase.sq        # Single source of truth for schema + queries
├── androidMain/
│   ├── kotlin/com/thinkingerp/
│   │   ├── MainActivity.kt
│   │   ├── barcode/AndroidBarcodeScanner.kt   # ML Kit implementation
│   │   └── data/database/DatabaseFactory.android.kt
│   └── AndroidManifest.xml
└── desktopMain/
    └── kotlin/com/thinkingerp/
        ├── main.kt
        ├── barcode/DesktopBarcodeScanner.kt   # ZXing implementation (USB scanner / webcam)
        └── data/database/DatabaseFactory.desktop.kt  # stores DB at ~/.thinkingerp/
```

### Key design decisions

- **`expect`/`actual` pattern** is used for `DatabaseFactory` and the barcode scanner — common interface, platform implementation.
- **GST calculation** lives entirely in domain model computed properties (`PurchaseBill`, `SellInvoice`). `isInterState = true` → IGST only; `false` → CGST + SGST split equally.
- **Claude AI flow**: user types question → `ClaudeQueryService.generateSqlQuery()` sends schema + question to `claude-sonnet-4-6` → Claude returns a raw `SELECT` SQL string → app executes it via SQLDelight raw query → result shown to user.
- **Claude API key** must be stored in `store_settings` table under key `claude_api_key` (never hardcoded).

## Database

Schema is defined in `ThinkingERPDatabase.sq`. SQLDelight generates type-safe Kotlin from it — never write raw SQL in Kotlin code, use the generated `ThinkingERPDatabase` queries instead.

Tables: `items`, `purchase_bills`, `purchase_bill_items`, `sell_invoices`, `sell_invoice_items`, `store_settings`.

Desktop DB location: `~/.thinkingerp/thinkingerp.db`

## GST Rules (India)

- GST rates per item: **0%, 5%, 12%, 18%, 28%**
- Intra-state sale/purchase: split as **CGST = SGST = rate/2**
- Inter-state sale/purchase: **IGST = full rate** (no CGST/SGST)
- `is_inter_state` flag on each bill/invoice controls which tax columns are populated
