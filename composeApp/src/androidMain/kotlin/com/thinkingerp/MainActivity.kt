package com.thinkingerp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.thinkingerp.data.DataSeeder
import com.thinkingerp.data.database.DatabaseFactory
import com.thinkingerp.data.database.ThinkingERPDatabase
import com.thinkingerp.data.repository.InventoryRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val driver = DatabaseFactory(this).createDriver()
        val db = ThinkingERPDatabase(driver)
        val repo = InventoryRepository(db)
        DataSeeder.seed(repo)
        val container = AppContainer(repo, driver)

        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                App()
            }
        }
    }
}
