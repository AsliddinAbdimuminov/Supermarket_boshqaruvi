package com.mobile_programming.sari_sari_inventory_app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mobile_programming.sari_sari_inventory_app.data.InventoryDatabase
import com.mobile_programming.sari_sari_inventory_app.data.dao.ProductDao
import com.mobile_programming.sari_sari_inventory_app.data.dao.ReceiptDao
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import com.mobile_programming.sari_sari_inventory_app.data.relation.ProductSale
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class ReceiptTableTest {
    private lateinit var db: InventoryDatabase
    private lateinit var productDao: ProductDao
    private lateinit var receiptDao: ReceiptDao

    @Before
    fun up() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, InventoryDatabase::class.java
        ).build()
        productDao = db.productDao()
        receiptDao = db.receiptDao()
    }

    @After
    @Throws(IOException::class)
    fun down() {
        db.close()
    }

    private suspend fun populateProductTable() : MutableList<Product> {
        val productList = mutableListOf(
            Product(
                id = 1,
                productNumber = "123456789",
                productName = "Muzqaymoq",
                price = 250.0,
                stock = 60,
                imageUri = null
            ),
            Product(
                id = 2,
                productNumber = "987654321",
                productName = "Sharbat",
                price = 650.0,
                stock = 20,
                imageUri = null
            ),
            Product(
                id = 3,
                productNumber = "111222333",
                productName = "Jelly",
                price = 20.0,
                stock = 105,
                imageUri = null
            ),
            Product(
                id = 4,
                productNumber = "102223334",
                productName = "Sovuq sharbat",
                price = 35.0,
                stock = 45,
                imageUri = null
            ),
            Product(
                id = 5,
                productNumber = "222103334",
                productName = "Issiq sharbat",
                price = 80.0,
                stock = 10,
                imageUri = null
            ),
            Product(
                id = 6,
                productNumber = "222333410",
                productName = "Mild Smoothie",
                price = 90.0,
                stock = 30,
                imageUri = null
            ),
        )

        productList.forEach {
            productDao.insertProduct(it)
        }

        return productList
    }

    private suspend fun populateReceiptTable() : MutableList<Receipt> {
        val receipts = mutableListOf(
            Receipt(
                1,
                Calendar.getInstance().time,
            ),
            Receipt(
                2,
                Calendar.getInstance().apply {
                    add(Calendar.DATE, -30)
                }.time,
            ),
            Receipt(
                3,
                Calendar.getInstance().apply {
                    add(Calendar.DATE, 15)
                }.time,
            ),
            Receipt(
                4,
                Calendar.getInstance().apply {
                    add(Calendar.DATE, -16)
                }.time,
            ),
            Receipt(
                5,
                Calendar.getInstance().apply {
                    add(Calendar.DATE, -16)
                }.time,
            ),
        )

        receipts.forEach {
            receiptDao.insertReceipt(it)
        }

        return receipts
    }

    @Test
    @Throws(Exception::class)
    fun receiptTable_RetrieveReceiptWithProducts_ReturnedReceiptWithProducts() = runTest {
        val products = populateProductTable()
        val receipts = populateReceiptTable()

        for(i in 0..2) {
            receiptDao.insertProductToReceipt(
                products[i].id, receipts[0].id, 1, products[i].price * 1
            )
        }

        val productsInReceipt = receiptDao.getListOfProducts(receipts[0].id)
        assertEquals(3, productsInReceipt.size)
    }

    @Test
    @Throws(Exception::class)
    fun receiptTable_RetrieveTotalCostOfReceipt_ReturnTotalCostOfReceipt() = runTest {
        val products = populateProductTable()
        val receipts = populateReceiptTable()

        receiptDao.insertProductToReceipt(
            products[0].id,
            receipts[0].id,
            2,
            products[0].price * 2
        )

        receiptDao.insertProductToReceipt(
            products[5].id,
            receipts[0].id,
            3,
            products[5].price * 3
        )

        val totalCost = (products[0].price * 2) + (products[5].price * 3)
        val totalCostQuery = receiptDao.getTotalCost(receipts[0].id)

        assertEquals(totalCost, totalCostQuery, 0.0)
    }

    @Test
    @Throws(Exception::class)
    fun receiptTable_RetrieveRevenueOnDates_ReturnedRevenueOnDates() = runTest {
        val products = populateProductTable()
        val receipts = populateReceiptTable()

        // Total Revenue: 600
        receiptDao.insertProductToReceipt(
            products[0].id,
            receipts[0].id,
            2,
            products[0].price * 2
        )

        receiptDao.insertProductToReceipt(
            products[2].id,
            receipts[0].id,
            3,
            products[2].price * 3
        )

        // Total Revenue: 820
        receiptDao.insertProductToReceipt(
            products[0].id,
            receipts[1].id,
            3,
            products[0].price.times(3)
        )

        receiptDao.insertProductToReceipt(
            products[3].id,
            receipts[1].id,
            2,
            products[3].price.times(2)
        )

        // Total Revenue: 270
        receiptDao.insertProductToReceipt(
            products[5].id,
            receipts[3].id,
            3,
            products[5].price.times(3)
        )

        // Total Revenue: 270
        receiptDao.insertProductToReceipt(
            products[5].id,
            receipts[4].id,
            3,
            products[5].price.times(3)
        )

        val totalRevenueLastMonth =
            (products[0].price * 3) + (products[3].price * 2) + (products[5].price * 6)
        val dateNow = Calendar.getInstance()
        val dateFromLastMonth = Calendar.getInstance().apply { add(Calendar.DATE, -30) }

        val totalRevenueFromQuery =
            receiptDao.getRevenueFromDates(dateFromLastMonth.time, dateNow.time)
                .filterNotNull()
                .first()
                .sumOf {
                it.totalRevenue
            }

        assertEquals(
            totalRevenueLastMonth,
            totalRevenueFromQuery,
            0.0
        )
    }

    @Test
    @Throws(Exception::class)
    fun receiptTable_ProductSalesLastMonth_ReturnedRelevantProductSales() = runTest {
        val products = populateProductTable()
        val receipts = populateReceiptTable()

        // Total Revenue: 600
        receiptDao.insertProductToReceipt(
            products[0].id,
            receipts[0].id,
            2,
            products[0].price.times(2)
        )
        receiptDao.insertProductToReceipt(
            products[2].id,
            receipts[0].id,
            3,
            products[2].price.times(3)
        )

        // Total Revenue: 820
        receiptDao.insertProductToReceipt(
            products[0].id,
            receipts[1].id,
            3,
            products[0].price.times(3)
        )
        receiptDao.insertProductToReceipt(
            products[3].id,
            receipts[1].id,
            2,
            products[3].price.times(2)
        )

        // Total Revenue: 270
        receiptDao.insertProductToReceipt(
            products[5].id,
            receipts[2].id,
            3,
            products[5].price.times(3)
        )

        // Total Revenue: 270
        receiptDao.insertProductToReceipt(
            products[5].id,
            receipts[3].id,
            3,
            products[5].price.times(3)
        )
        receiptDao.insertProductToReceipt(
            products[0].id,
            receipts[3].id,
            4,
            products[0].price.times(4)
        )

        // Total Revenue: 270
        receiptDao.insertProductToReceipt(
            products[5].id,
            receipts[4].id,
            3,
            products[5].price.times(3)
        )

        val expectedResult = listOf(
            ProductSale(
                product = products[0],
                amountSold = 7,
                totalRevenue = products[0].price * 7
            ),
            ProductSale(
                product = products[3],
                amountSold = 2,
                totalRevenue = products[3].price * 2
            ),
            ProductSale(
                product = products[5],
                amountSold = 6,
                totalRevenue = products[5].price * 6
            )
        )

        val queryResult = productDao.getSalesFromDates(
            id = null,
            dateFrom = Calendar.getInstance().apply { add(Calendar.DATE, -30) }.time,
            dateTo = Calendar.getInstance().time
        ).filterNotNull().first()

        assertEquals(expectedResult, queryResult)
    }
}