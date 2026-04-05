package com.thinkingerp.ui.screens.settings

import com.thinkingerp.data.repository.InventoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object SettingsKeys {
    const val STORE_NAME = "store_name"
    const val STORE_GSTIN = "store_gstin"
    const val CLAUDE_API_KEY = "claude_api_key"
}

data class SettingsUiState(
    val storeName: String = "",
    val storeGstin: String = "",
    val claudeApiKey: String = "",
    val saved: Boolean = false,
)

class SettingsViewModel(private val repo: InventoryRepository) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        scope.launch(Dispatchers.Default) {
            _state.update {
                it.copy(
                    storeName = repo.getSetting(SettingsKeys.STORE_NAME) ?: "",
                    storeGstin = repo.getSetting(SettingsKeys.STORE_GSTIN) ?: "",
                    claudeApiKey = repo.getSetting(SettingsKeys.CLAUDE_API_KEY) ?: "",
                )
            }
        }
    }

    fun onStoreNameChanged(value: String) = _state.update { it.copy(storeName = value, saved = false) }
    fun onStoreGstinChanged(value: String) = _state.update { it.copy(storeGstin = value, saved = false) }
    fun onApiKeyChanged(value: String) = _state.update { it.copy(claudeApiKey = value, saved = false) }

    fun save() {
        val s = _state.value
        scope.launch(Dispatchers.Default) {
            repo.setSetting(SettingsKeys.STORE_NAME, s.storeName)
            repo.setSetting(SettingsKeys.STORE_GSTIN, s.storeGstin)
            repo.setSetting(SettingsKeys.CLAUDE_API_KEY, s.claudeApiKey)
            _state.update { it.copy(saved = true) }
        }
    }
}
