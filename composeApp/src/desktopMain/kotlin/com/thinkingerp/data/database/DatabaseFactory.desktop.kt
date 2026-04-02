package com.thinkingerp.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DatabaseFactory {
    actual fun createDriver(): SqlDriver {
        val dbPath = File(System.getProperty("user.home"), ".thinkingerp/thinkingerp.db")
        dbPath.parentFile?.mkdirs()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbPath.absolutePath}")
        ThinkingERPDatabase.Schema.create(driver) // IF NOT EXISTS — safe to call every time
        return driver
    }
}
