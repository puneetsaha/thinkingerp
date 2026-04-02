package com.thinkingerp

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.thinkingerp.data.DataSeeder
import com.thinkingerp.data.database.DatabaseFactory
import com.thinkingerp.data.database.ThinkingERPDatabase
import com.thinkingerp.data.repository.InventoryRepository

fun main() {
    val db = ThinkingERPDatabase(DatabaseFactory().createDriver())
    val repo = InventoryRepository(db)
    DataSeeder.seed(repo)
    val container = AppContainer(repo)

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "ThinkingERP",
            state = rememberWindowState(width = 1024.dp, height = 768.dp),
        ) {
            CompositionLocalProvider(LocalAppContainer provides container) {
                App()
            }
        }
    }
}
