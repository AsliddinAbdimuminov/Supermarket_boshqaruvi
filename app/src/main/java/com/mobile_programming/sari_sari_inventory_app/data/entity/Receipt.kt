package com.mobile_programming.sari_sari_inventory_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "Receipts"
)
data class Receipt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateCreated: Date,
)


