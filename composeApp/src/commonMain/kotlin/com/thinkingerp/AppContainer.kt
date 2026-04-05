package com.thinkingerp

import androidx.compose.runtime.compositionLocalOf
import app.cash.sqldelight.db.SqlDriver
import com.thinkingerp.data.repository.InventoryRepository

class AppContainer(
    val inventoryRepo: InventoryRepository,
    val sqlDriver: SqlDriver,
)

val LocalAppContainer = compositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}
