package com.mobile_programming.sari_sari_inventory_app

import android.app.Application
import com.mobile_programming.sari_sari_inventory_app.data.AppContainer
import com.mobile_programming.sari_sari_inventory_app.data.AppDataContainer

class InventoryApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}