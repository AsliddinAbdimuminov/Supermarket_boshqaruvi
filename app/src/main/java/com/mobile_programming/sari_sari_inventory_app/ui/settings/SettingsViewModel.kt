package com.mobile_programming.sari_sari_inventory_app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.utils.UserPreferencesDataStoreConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private var _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {

            val isDarkModeEnabled = inventoryRepository.getPreference(
                UserPreferencesDataStoreConstants.IS_DARK_MODE,
                false
            ).filterNotNull()
                .first()

            _uiState.update { oldState ->
                oldState.copy(
                    isDarkModeEnabled = isDarkModeEnabled
                )
            }
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            inventoryRepository.putPreference(
                UserPreferencesDataStoreConstants.IS_DARK_MODE,
                enabled
            )

            _uiState.update { oldState ->
                oldState.copy(isDarkModeEnabled = enabled)
            }
        }
    }

}

data class SettingsUiState(
    val isDarkModeEnabled: Boolean = false
)