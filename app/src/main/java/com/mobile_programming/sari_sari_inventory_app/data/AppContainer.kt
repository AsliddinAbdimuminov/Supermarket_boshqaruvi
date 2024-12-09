package com.mobile_programming.sari_sari_inventory_app.data

import android.content.Context

interface AppContainer {
    val inventoryRepository : InventoryRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val inventoryRepository: InventoryRepository by lazy {
        OfflineInventoryRepository(
            InventoryDatabase.getDatabase(context).productDao(),
            InventoryDatabase.getDatabase(context).receiptDao(),
            UserPreferencesDataStoreHelper(context)
        )
    }
}