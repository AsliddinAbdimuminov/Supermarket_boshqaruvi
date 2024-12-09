package com.mobile_programming.sari_sari_inventory_app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Products",
    indices = [
        Index(value = ["productNumber"], unique = true),
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productNumber: String?,
    val productName: String,
    val price: Double,
    val stock: Int,
    val imageUri: String?
)
