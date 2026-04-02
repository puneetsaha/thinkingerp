package com.thinkingerp.ui.screens.purchase

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.thinkingerp.LocalAppContainer
import com.thinkingerp.data.DataSeeder
import com.thinkingerp.domain.model.Item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseBillScreen(onBack: () -> Unit) {
    val repo = LocalAppContainer.current.inventoryRepo
    val vm = remember { PurchaseBillViewModel(repo) }
    val state by vm.state.collectAsState()

    // Local UI state
    var barcodeInput by remember { mutableStateOf("") }
    var scannedItem by remember { mutableStateOf<Item?>(null) }
    var itemQty by remember { mutableStateOf("1") }
    var itemPrice by remember { mutableStateOf("") }
    var supplierInput by remember { mutableStateOf("") }
    var lookupError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.saved) { if (state.saved) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase Bill") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { vm.saveBill() },
                        enabled = state.lines.isNotEmpty(),
                        modifier = Modifier.padding(end = 8.dp),
                    ) { Text("Save Bill") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // ── Barcode input + Add button ───────────────────────────────────
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = barcodeInput,
                        onValueChange = {
                            barcodeInput = it
                            scannedItem = null
                            lookupError = null
                        },
                        label = { Text("Barcode") },
                        placeholder = { Text("13-digit EAN-13") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val item = repo.getItemByBarcode(barcodeInput.trim())
                            if (item != null) {
                                scannedItem = item
                                itemPrice = item.mrp.toString()
                                itemQty = "1"
                                lookupError = null
                            } else {
                                scannedItem = null
                                lookupError = "No item found for barcode: ${barcodeInput.trim()}"
                            }
                        },
                        enabled = barcodeInput.length == 13,
                    ) { Text("Add") }
                }
                lookupError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp))
                }
            }

            // ── Item detail form (shown after successful lookup) ──────────────
            scannedItem?.let { item ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("Item Details", style = MaterialTheme.typography.titleSmall)
                            HorizontalDivider()
                            DetailRow("Name", item.name)
                            DetailRow("Company", item.company)
                            DetailRow("MRP", "₹${item.mrp}")
                            DetailRow("GST", "${item.gstRate}% | HSN: ${item.hsnCode}")
                            DetailRow("Current Stock", "${item.quantity} ${item.unit}")
                            HorizontalDivider()
                            OutlinedTextField(
                                value = supplierInput,
                                onValueChange = { supplierInput = it },
                                label = { Text("Supplier Name") },
                                placeholder = { Text("Optional") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = itemQty,
                                    onValueChange = { itemQty = it },
                                    label = { Text("Qty") },
                                    modifier = Modifier.width(90.dp),
                                    singleLine = true,
                                )
                                OutlinedTextField(
                                    value = itemPrice,
                                    onValueChange = { itemPrice = it },
                                    label = { Text("Purchase Price ₹") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                )
                            }
                            Button(
                                onClick = {
                                    vm.onSupplierChanged(supplierInput)
                                    vm.onBarcodeScanned(item.barcode)
                                    val qty = itemQty.toIntOrNull() ?: 1
                                    val price = itemPrice.toDoubleOrNull() ?: item.mrp
                                    vm.onQuantityChanged(item.barcode, qty)
                                    vm.onPriceChanged(item.barcode, price)
                                    scannedItem = null
                                    barcodeInput = ""
                                    lookupError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Add to Bill") }
                        }
                    }
                }
            }

            // ── Sample barcodes ───────────────────────────────────────────────
            item {
                Text("Sample barcodes (tap to fill):",
                    style = MaterialTheme.typography.labelMedium)
            }
            items(DataSeeder.dummyBarcodes) { (barcode, name) ->
                OutlinedCard(
                    onClick = {
                        barcodeInput = barcode
                        scannedItem = null
                        lookupError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                            Text(barcode, style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace)
                        }
                        Text("Tap to fill", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // ── Bill items list ───────────────────────────────────────────────
            if (state.lines.isNotEmpty()) {
                item {
                    HorizontalDivider()
                    Text("Bill Items (${state.lines.size})",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 4.dp))
                }
                items(state.lines, key = { it.id }) { line ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(line.item.name, style = MaterialTheme.typography.bodyMedium)
                                Text("Qty: ${line.quantity}  ×  ₹${line.purchasePrice}  =  ₹%.2f".format(line.purchasePrice * line.quantity),
                                    style = MaterialTheme.typography.bodySmall)
                                Text("GST ${line.item.gstRate}%",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { vm.removeLine(line.item.barcode) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }

                // GST Summary card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            SummaryRow("Subtotal", "₹%.2f".format(state.subtotal))
                            if (!state.isInterState) {
                                SummaryRow("CGST", "₹%.2f".format(state.cgst))
                                SummaryRow("SGST", "₹%.2f".format(state.sgst))
                            } else {
                                SummaryRow("IGST", "₹%.2f".format(state.igst))
                            }
                            HorizontalDivider()
                            SummaryRow(
                                "Total", "₹%.2f".format(state.total),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = state.isInterState,
                                    onCheckedChange = { vm.onInterStateToggled(it) },
                                )
                                Text("Inter-state transaction (use IGST)")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = style)
        Text(value, style = style)
    }
}
