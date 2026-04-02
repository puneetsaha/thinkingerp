package com.thinkingerp.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    onNavigateToPurchase: () -> Unit,
    onNavigateToSell: () -> Unit,
    onNavigateToQuery: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("ThinkingERP", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(48.dp))

        Button(onClick = onNavigateToPurchase, modifier = Modifier.fillMaxWidth()) {
            Text("Purchase Bill")
        }
        Spacer(Modifier.height(16.dp))

        Button(onClick = onNavigateToSell, modifier = Modifier.fillMaxWidth()) {
            Text("Sell Invoice")
        }
        Spacer(Modifier.height(16.dp))

        OutlinedButton(onClick = onNavigateToQuery, modifier = Modifier.fillMaxWidth()) {
            Text("Ask Inventory (AI)")
        }
    }
}
