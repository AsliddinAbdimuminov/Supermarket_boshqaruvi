package com.mobile_programming.sari_sari_inventory_app.data.relation

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import java.util.Date

@Entity(
    tableName = "ProductsPerReceipt",
    indices = [Index(value = ["productId", "receiptId"])],
    foreignKeys = [
        ForeignKey(
            entity = Receipt::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
//        ForeignKey(
//            entity = Product::class,
//            parentColumns = ["id"],
//            childColumns = ["productId"],
//            onDelete = ForeignKey.NO_ACTION,
//            onUpdate = ForeignKey.CASCADE
//        )

            /*
     * Malumot berilganda qator o'chirilmasligini xohlardim
     * mahsulot o'chiriladi, lekin chet el kalitiga cheklov qo'yadi
     * muvaffaqiyatsiz. Uni chet el kaliti sifatida sozlash emas, balki ishlaydi, lekin
     * kelajakda muammolarga olib kelishi mumkin.
     * */
    ]
)
data class ProductsPerReceipt(
    @PrimaryKey val id: Long = 0,
    val productId: Long,
    val receiptId: Long,
    val amount: Int,
    val revenue: Double,
)

data class RevenueOnDate(
    val date: Date,
    val totalRevenue: Double
)

data class ProductSale(
    @Embedded val product: Product? = null,
    val amountSold: Int,
    val totalRevenue: Double,
)