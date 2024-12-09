package com.mobile_programming.sari_sari_inventory_app.data

import androidx.datastore.preferences.core.Preferences
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import com.mobile_programming.sari_sari_inventory_app.data.relation.ProductSale
import com.mobile_programming.sari_sari_inventory_app.data.relation.RevenueOnDate
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface InventoryRepository {
    // Mahsulot funktsiyalari
    suspend fun insertProduct(product: Product) : Long

    suspend fun updateProduct(product: Product)

    suspend fun deleteProduct(product: Product)

    fun getAllProducts() : Flow<List<Product>>

    fun getProduct(id: Long) : Flow<Product?>

    fun getProductByNumber(productNumber: String) : Flow<Product?>

    fun searchForProduct(nameOrNumber: String) : Flow<List<Product>>

    fun checkIfProductNumberExists(productNumber: String) : Boolean

    fun countOutOfStock() : Flow<Int>

    fun countLowOnStock(max: Int) : Flow<Int>

    // Qabul qilish funktsiyalari
    suspend fun insertReceipt(receipt: Receipt, products: Map<Product, Int>) : Long

    suspend fun deleteReceipt(receipt: Receipt)

    fun getAllReceipts() : Flow<List<Receipt>>

    fun getReceipt(id: Long) : Flow<Receipt?>

    suspend fun getProductsInReceipt(id: Long) : Map<Long, Int>

    fun getReceiptTotalCost(id: Long) : Double

    // Statistika bilan bog'liq funktsiyalar
    suspend fun getProductSalesFromDates(
        id: Long?,
        dateFrom: Date,
        dateTo: Date
    ) : Flow<List<ProductSale>>

    suspend fun getRevenueFromDates(
        dateFrom: Date,
        dateTo: Date
    ) : Flow<List<RevenueOnDate>>

    // Preferences Ma'lumotlar ombori funktsiyalari
    suspend fun <T> getPreference(
        key: Preferences.Key<T>,
        defaultValue: T
    ) : Flow<T>

    suspend fun <T> putPreference(
        key: Preferences.Key<T>,
        value: T
    )

    suspend fun <T> removePreference(
        key: Preferences.Key<T>
    )

    suspend fun <T> clearAllPreference()
}