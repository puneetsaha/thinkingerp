package com.thinkingerp.data.database

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseFactory {
    fun createDriver(): SqlDriver
}
