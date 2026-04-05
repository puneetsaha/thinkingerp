package com.thinkingerp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.thinkingerp.LocalAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val repo = LocalAppContainer.current.inventoryRepo
    val vm = remember { SettingsViewModel(repo) }
    val state by vm.state.collectAsState()
    var showApiKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Store Details", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = state.storeName,
                onValueChange = { vm.onStoreNameChanged(it) },
                label = { Text("Store Name") },
                placeholder = { Text("e.g. Sharma General Store") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = state.storeGstin,
                onValueChange = { vm.onStoreGstinChanged(it) },
                label = { Text("Store GSTIN") },
                placeholder = { Text("15-character GSTIN") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            HorizontalDivider()
            Text("AI Settings", style = MaterialTheme.typography.titleMedium)
            Text(
                "Required for the 'Ask Inventory' feature. Get your key from console.anthropic.com",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = state.claudeApiKey,
                onValueChange = { vm.onApiKeyChanged(it) },
                label = { Text("Claude API Key") },
                placeholder = { Text("sk-ant-...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showApiKey = !showApiKey }) {
                        Text(if (showApiKey) "Hide" else "Show")
                    }
                },
            )

            Button(
                onClick = { vm.save() },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save Settings") }

            if (state.saved) {
                Text("Settings saved.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
