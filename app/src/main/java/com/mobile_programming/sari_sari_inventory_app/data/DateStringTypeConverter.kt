package com.mobile_programming.sari_sari_inventory_app.data

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class DateStringTypeConverter {
    @TypeConverter
    fun formattedStringToDate(value: String): Date {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()

        return calendar.apply { time = formatter.parse(value) as Date }.time
    }

    @TypeConverter
    fun dateToFormattedString(value: Date) : String {
        val formatter = SimpleDateFormat("yyyy-MM-dd")

        return formatter.format(value)
    }
}