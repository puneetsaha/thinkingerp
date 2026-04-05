package com.thinkingerp.ui.screens.query

import app.cash.sqldelight.db.SqlDriver
import com.thinkingerp.ai.ClaudeQueryService
import com.thinkingerp.data.database.executeRawSelect
import com.thinkingerp.data.repository.InventoryRepository
import com.thinkingerp.ui.screens.settings.SettingsKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QueryUiState(
    val question: String = "",
    val generatedSql: String = "",
    val results: List<Map<String, String>> = emptyList(),
    val columnOrder: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class NaturalLanguageQueryViewModel(
    private val repo: InventoryRepository,
    private val driver: SqlDriver,
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(QueryUiState())
    val state: StateFlow<QueryUiState> = _state.asStateFlow()

    fun onQuestionChanged(q: String) = _state.update { it.copy(question = q, error = null) }

    fun onAsk() {
        val question = _state.value.question.trim()
        if (question.isBlank()) return

        val apiKey = repo.getSetting(SettingsKeys.CLAUDE_API_KEY)
        if (apiKey.isNullOrBlank()) {
            _state.update { it.copy(error = "Claude API key not set. Go to Settings to add it.") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null, generatedSql = "", results = emptyList()) }

        scope.launch(Dispatchers.Default) {
            val service = ClaudeQueryService(apiKey)
            try {
                val sqlResult = service.generateSqlQuery(question)
                sqlResult.fold(
                    onSuccess = { sql ->
                        try {
                            val rows = executeRawSelect(driver, sql)
                            val columns = rows.firstOrNull()?.keys?.toList() ?: emptyList()
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    generatedSql = sql,
                                    results = rows,
                                    columnOrder = columns,
                                )
                            }
                        } catch (e: Exception) {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    generatedSql = sql,
                                    error = "SQL execution failed: ${e.message}",
                                )
                            }
                        }
                    },
                    onFailure = { e ->
                        _state.update {
                            it.copy(isLoading = false, error = "Claude API error: ${e.message}")
                        }
                    }
                )
            } finally {
                service.close()
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
