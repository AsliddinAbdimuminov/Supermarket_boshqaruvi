package com.mobile_programming.sari_sari_inventory_app.data

import androidx.datastore.preferences.core.Preferences
import com.mobile_programming.sari_sari_inventory_app.data.dao.ProductDao
import com.mobile_programming.sari_sari_inventory_app.data.dao.ReceiptDao
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import com.mobile_programming.sari_sari_inventory_app.data.relation.ProductSale
import com.mobile_programming.sari_sari_inventory_app.data.relation.RevenueOnDate
import kotlinx.coroutines.flow.Flow
import java.util.Date

class OfflineInventoryRepository(
    private val productDao: ProductDao,
    private val receiptDao: ReceiptDao,
    private val preferencesDatastore: UserPreferencesDataStoreApi,
) : InventoryRepository {
    override suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(product)
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    override suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    override fun getProduct(id: Long): Flow<Product?> {
        return productDao.getProduct(id)
    }

    override fun getProductByNumber(productNumber: String): Flow<Product?> {
        return productDao.getProductByNumber(productNumber)
    }

    override fun searchForProduct(nameOrNumber: String): Flow<List<Product>> {
        return productDao.searchForProduct(nameOrNumber)
    }

    override fun checkIfProductNumberExists(productNumber: String): Boolean {
        return productDao.checkIfProductNumberExists(productNumber)
    }

    override fun countOutOfStock(): Flow<Int> {
        return productDao.countOutOfStock()
    }

    override fun countLowOnStock(max: Int): Flow<Int> {
        return productDao.countLowOnStock(max)
    }

    override suspend fun insertReceipt(receipt: Receipt, products: Map<Product, Int>): Long {
        val id = receiptDao.insertReceipt(receipt)

        products.forEach {
            receiptDao.insertProductToReceipt(
                productId = it.key.id,
                receiptId = id,
                amount = it.value,
                totalRevenue = it.value * it.key.price
            )
        }

        return id
    }

    override suspend fun deleteReceipt(receipt: Receipt) {
        receiptDao.deleteReceipt(receipt)
    }

    override fun getAllReceipts(): Flow<List<Receipt>> {
        return receiptDao.getAllReceipts()
    }

    override fun getReceipt(id: Long): Flow<Receipt?> {
        return receiptDao.getReceipt(id)
    }

    // <Object(Mahsulot), Int> yoki <Long(ProductId), Int)> xaritasini qaytarish yaxshiroqmi?
    // Muammo va buzilish bo'lishi mumkin
    override suspend fun getProductsInReceipt(id: Long): Map<Long, Int> {
        return receiptDao.getListOfProducts(id).associateBy(
            { it.productId },
            { it.amount }
        )
    }

    override fun getReceiptTotalCost(id: Long): Double {
        return receiptDao.getTotalCost(id)
    }

    override suspend fun getProductSalesFromDates(
        id: Long?,
        dateFrom: Date,
        dateTo: Date
    ): Flow<List<ProductSale>> {
        return productDao.getSalesFromDates(id, dateFrom, dateTo)
    }

    override suspend fun getRevenueFromDates(
        dateFrom: Date,
        dateTo: Date
    ): Flow<List<RevenueOnDate>> {
        return receiptDao.getRevenueFromDates(dateFrom, dateTo)
    }

    override suspend fun <T> getPreference(
        key: Preferences.Key<T>,
        defaultValue: T
    ): Flow<T> {
        return preferencesDatastore.getPreference(key, defaultValue)
    }

    override suspend fun <T> putPreference(
        key: Preferences.Key<T>,
        value: T
    ) {
        preferencesDatastore.putPreference(key, value)
    }

    override suspend fun <T> removePreference(
        key: Preferences.Key<T>
    ) {
        preferencesDatastore.removePreference(key)
    }

    override suspend fun <T> clearAllPreference() {
        preferencesDatastore.clearAllPreference<T>()
    }

}