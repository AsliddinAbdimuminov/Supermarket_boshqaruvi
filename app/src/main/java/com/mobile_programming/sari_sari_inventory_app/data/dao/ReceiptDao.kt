package com.mobile_programming.sari_sari_inventory_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import com.mobile_programming.sari_sari_inventory_app.data.relation.ProductsPerReceipt
import com.mobile_programming.sari_sari_inventory_app.data.relation.RevenueOnDate
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReceipt(receipt: Receipt) : Long

    @Delete
    suspend fun deleteReceipt(receipt: Receipt)

    @Query("SELECT * FROM receipts ")
    fun getAllReceipts() : Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE id = :id ")
    fun getReceipt(id: Long) : Flow<Receipt?>

    /**
     * Assotsiativ shaxs orqali mahsulotni chekga biriktiradi
     */
    @Query(
        "INSERT INTO productsperreceipt (productId, receiptID, amount, revenue) " +
        "VALUES (:productId, :receiptId, :amount, :totalRevenue)"
    )
    suspend fun insertProductToReceipt(
        productId: Long,
        receiptId: Long,
        amount: Int,
        totalRevenue: Double
    )

    /**
     * Kvitansiya identifikatorini o'z ichiga olgan ProductsPerReceipt ob'ektlari ro'yxatini oladi.
     */
    @Query("SELECT * FROM productsperreceipt WHERE receiptId = :id")
    fun getListOfProducts(id: Long) : List<ProductsPerReceipt>

    @Query("SELECT SUM(revenue) " +
            "FROM productsperreceipt " +
            "WHERE receiptId = :id "
    )
    fun getTotalCost(id: Long) : Double

    @Query(
        "SELECT r.dateCreated as date, SUM(ppr.revenue) as totalRevenue " +
            "FROM receipts r " +
            "INNER JOIN productsperreceipt ppr ON ppr.receiptId = r.id " +
            "WHERE r.dateCreated >= :dateFrom " +
            "AND r.dateCreated < :dateTo " +
            "GROUP BY r.dateCreated"
    )
    fun getRevenueFromDates(dateFrom: Date, dateTo: Date) : Flow<List<RevenueOnDate>>
}