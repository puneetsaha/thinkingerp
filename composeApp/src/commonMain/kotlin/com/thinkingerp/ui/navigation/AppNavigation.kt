package com.thinkingerp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thinkingerp.ui.screens.dashboard.DashboardScreen
import com.thinkingerp.ui.screens.purchase.PurchaseBillScreen
import com.thinkingerp.ui.screens.query.NaturalLanguageQueryScreen
import com.thinkingerp.ui.screens.sell.SellInvoiceScreen
import com.thinkingerp.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object PurchaseBill : Screen("purchase_bill")
    data object SellInvoice : Screen("sell_invoice")
    data object Query : Screen("query")
    data object Settings : Screen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToPurchase = { navController.navigate(Screen.PurchaseBill.route) },
                onNavigateToSell = { navController.navigate(Screen.SellInvoice.route) },
                onNavigateToQuery = { navController.navigate(Screen.Query.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            )
        }
        composable(Screen.PurchaseBill.route) {
            PurchaseBillScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.SellInvoice.route) {
            SellInvoiceScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Query.route) {
            NaturalLanguageQueryScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
