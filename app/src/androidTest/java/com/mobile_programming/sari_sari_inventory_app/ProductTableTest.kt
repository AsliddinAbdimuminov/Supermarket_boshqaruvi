package com.mobile_programming.sari_sari_inventory_app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mobile_programming.sari_sari_inventory_app.data.InventoryDatabase
import com.mobile_programming.sari_sari_inventory_app.data.dao.ProductDao
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ProductTableTest {
    private lateinit var db: InventoryDatabase
    private lateinit var productDao: ProductDao

    @Before
    fun up() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, InventoryDatabase::class.java
        ).build()
        productDao = db.productDao()
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

    @Test
    @Throws(Exception::class)
    fun productTable_InsertProduct_ProductInserted() = runTest {
        val product = Product(
            id = 1,
            productNumber = "123456789",
            productName = "Muzqaymoq",
            price = 250.0,
            stock = 60,
            imageUri = null
        )

        productDao.insertProduct(product)
        val productList: List<Product> = productDao.getAllProducts().filterNotNull().first()

        assertEquals(1, productList.size)
        assertEquals(product, productList.first())
    }

    @Test
    @Throws(Exception::class)
    fun productTable_UpdateAndDeleteProduct_ProductUpdatedThenDeleted() = runTest {
        val productList = populateProductTable()

        // Second Product in the list and database are updated here
        productList[1] = productList[1].copy(productName = "Smoothie", price = 120.50)
        productDao.updateProduct(productList[1])

        assertEquals(productList[1], productDao.getProduct(2).firstOrNull())

        // Second Product in the list and database are deleted here
        productDao.deleteProduct(productList[1])
        productList.remove(productList[1])

        assertEquals(productList, productDao.getAllProducts().firstOrNull())
    }

    @Test
    @Throws(Exception::class)
    fun productTable_SearchByProductNameAndNumber_ProductListFiltered() = runTest {
        val productList = populateProductTable()

        val productNumberToFind = "10"
        val filteredByProductNumber = productList.filter {
            it.productNumber?.contains(productNumberToFind) ?: false
        }

        val productNameToFind = "sharbat"
        val filteredByProductName = productList.filter {
            it.productName.contains(productNameToFind, ignoreCase = true)
        }

        assertEquals(
            filteredByProductNumber,
            productDao.searchForProduct(productNumberToFind)
                .filterNotNull()
                .first()
        )

        assertEquals(
            filteredByProductName,
            productDao.searchForProduct(productNameToFind)
                .filterNotNull()
                .first()
        )
    }

    @Test
    fun productTable_NonExistingProduct_ReturnedNull() = runTest {
        populateProductTable()
        val incorrectId = 201

        assertEquals(null, productDao.getProduct(incorrectId.toLong()).firstOrNull())
    }

    @Test
    @Throws(Exception::class)
    fun productTable_NonExistingNameAndNumber_EmptyListReturned() = runTest {
        populateProductTable()
        val incorrectName = "!@#naksdjak!!@#bad"
        val incorrectNumber = "131314215315535151"

        assertTrue(productDao.searchForProduct(incorrectName).filterNotNull().first().isEmpty())
        assertTrue(productDao.searchForProduct(incorrectNumber).filterNotNull().first().isEmpty())
    }
}