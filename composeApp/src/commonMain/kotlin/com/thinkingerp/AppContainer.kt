package com.thinkingerp

import androidx.compose.runtime.compositionLocalOf
import com.thinkingerp.data.repository.InventoryRepository

class AppContainer(val inventoryRepo: InventoryRepository)

val LocalAppContainer = compositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}
