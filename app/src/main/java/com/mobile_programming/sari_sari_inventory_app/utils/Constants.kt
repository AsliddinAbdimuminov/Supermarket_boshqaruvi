package com.mobile_programming.sari_sari_inventory_app.utils

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.mobile_programming.sari_sari_inventory_app.R

const val productImageDir = "/thumbnails/"

enum class SortingType {
    ByProductName,
    ByProductNumber,
    ByPrice,
    ByStock,
    ByRevenue,
    ByNumberSold
}

enum class TextInputErrorType(val stringResourceId: Int) {
    DuplicateFound(R.string.error_duplicate),
//    MaximumExceeded(R.string.error_maximum_exceeded),
//    MinimumNotReached(R.string.error_minimum_not_reached),
    FieldRequired(R.string.error_field_required)
}

object UserPreferencesDataStoreConstants {
    val IS_DARK_MODE = booleanPreferencesKey("IS_DARK_MODE")
}