package com.thinkingerp.ui.screens.query

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.thinkingerp.LocalAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaturalLanguageQueryScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val vm = remember { NaturalLanguageQueryViewModel(container.inventoryRepo, container.sqlDriver) }
    val state by vm.state.collectAsState()
    var showSql by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ask Inventory") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Question input
            OutlinedTextField(
                value = state.question,
                onValueChange = { vm.onQuestionChanged(it) },
                label = { Text("Ask a question") },
                placeholder = { Text("e.g. Which items have stock below 10?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Button(
                onClick = { vm.onAsk() },
                enabled = state.question.isNotBlank() && !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Thinking...")
                } else {
                    Text("Ask")
                }
            }

            // Error
            state.error?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(it, modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            // Generated SQL (collapsible)
            if (state.generatedSql.isNotBlank()) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Generated SQL", style = MaterialTheme.typography.labelMedium)
                            TextButton(onClick = { showSql = !showSql }) {
                                Text(if (showSql) "Hide" else "Show")
                            }
                        }
                        if (showSql) {
                            Text(
                                state.generatedSql,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                            )
                        }
                    }
                }
            }

            // Results table
            if (state.results.isNotEmpty()) {
                Text("${state.results.size} result(s)", style = MaterialTheme.typography.labelMedium)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Header
                    item {
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            state.columnOrder.forEach { col ->
                                Text(
                                    col,
                                    modifier = Modifier.width(140.dp).padding(4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                    // Rows
                    items(state.results) { row ->
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            state.columnOrder.forEach { col ->
                                Text(
                                    row[col] ?: "",
                                    modifier = Modifier.width(140.dp).padding(4.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}
