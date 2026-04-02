package com.thinkingerp.ui.screens.query

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaturalLanguageQueryScreen(onBack: () -> Unit) {
    var question by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

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
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text("Ask a question about your inventory") },
                placeholder = { Text("e.g. How many units of Surf Excel do I have?") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { /* TODO: call ClaudeQueryService, execute SQL, show result */ },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Ask")
            }
            Spacer(Modifier.height(16.dp))
            if (result.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(result, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
