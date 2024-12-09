package com.mobile_programming.sari_sari_inventory_app

import androidx.lifecycle.ViewModel
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.utils.UserPreferencesDataStoreConstants
import kotlinx.coroutines.flow.Flow

class MainActivityViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    suspend fun collectDarkModePreference() : Flow<Boolean> {
        return inventoryRepository.getPreference(
            UserPreferencesDataStoreConstants.IS_DARK_MODE,
            false
        )
    }
}