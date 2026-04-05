package com.thinkingerp.data.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Executes a raw SELECT query. Column names are parsed from the SELECT clause.
 * Claude's system prompt instructs it to never use SELECT *, so named columns are always present.
 * Only SELECT statements are permitted.
 */
fun executeRawSelect(driver: SqlDriver, sql: String): List<Map<String, String>> {
    val trimmed = sql.trim()
    require(trimmed.uppercase().startsWith("SELECT")) { "Only SELECT queries are allowed" }

    val columnNames = parseColumnNames(trimmed)
    val results = mutableListOf<Map<String, String>>()

    driver.executeQuery(
        identifier = null,
        sql = trimmed,
        mapper = { cursor ->
            while (cursor.next().value) {
                val row = mutableMapOf<String, String>()
                columnNames.forEachIndexed { i, name ->
                    row[name] = cursor.getString(i) ?: ""
                }
                results.add(row)
            }
            app.cash.sqldelight.db.QueryResult.Value(Unit)
        },
        parameters = 0,
    )

    return results
}

private fun parseColumnNames(sql: String): List<String> {
    return try {
        val upper = sql.uppercase()
        val selectStart = upper.indexOf("SELECT") + 6
        val fromIdx = upper.indexOf(" FROM ")
        if (fromIdx < 0) return emptyList()
        val selectClause = sql.substring(selectStart, fromIdx).trim()
        selectClause.split(",").map { col ->
            val trimmed = col.trim()
            // Handle "expr AS alias" or "table.column" → take last part
            val asIdx = trimmed.uppercase().lastIndexOf(" AS ")
            if (asIdx >= 0) {
                trimmed.substring(asIdx + 4).trim().removeSurrounding("\"").removeSurrounding("`")
            } else {
                trimmed.substringAfterLast(".").trim()
                    .removeSurrounding("\"").removeSurrounding("`")
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}
