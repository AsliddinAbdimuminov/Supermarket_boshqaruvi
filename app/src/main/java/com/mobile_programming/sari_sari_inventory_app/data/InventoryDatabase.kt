package com.mobile_programming.sari_sari_inventory_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mobile_programming.sari_sari_inventory_app.data.dao.ProductDao
import com.mobile_programming.sari_sari_inventory_app.data.dao.ReceiptDao
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import com.mobile_programming.sari_sari_inventory_app.data.relation.ProductsPerReceipt
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@Database(
    entities = [
        Product::class,
        Receipt::class,
        ProductsPerReceipt::class
    ],
    version = 6,
)
@TypeConverters(DateStringTypeConverter::class)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun receiptDao(): ReceiptDao

    companion object {
        @Volatile
        private var Instance: InventoryDatabase? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getDatabase(context: Context) : InventoryDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context, InventoryDatabase::class.java, "inventoryDatabase"
                )
//                    .createFromAsset("database/inventoryDatabase.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}