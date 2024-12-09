package com.mobile_programming.sari_sari_inventory_app.ui.product

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.utils.TextInputErrorType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat


class ProductEntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val productNumber: String? = savedStateHandle[PRODUCT_NUMBER_NAV_ARG_KEY]

    private var _uiState = MutableStateFlow(ProductEntryUiState())
    val uiState: StateFlow<ProductEntryUiState> = _uiState.asStateFlow()

    companion object {
        const val PRODUCT_NUMBER_ERROR_KEY = "Product Number"
        const val PRODUCT_NAME_ERROR_KEY = "Product Name"
        const val PRODUCT_PRICE_ERROR_KEY = "Price"
        const val PRODUCT_STOCK_ERROR_KEY = "Stock"

        const val PRODUCT_NUMBER_NAV_ARG_KEY = "productNumber"
    }

    init {
        resetUiState()
    }

    fun updateUiState(productDetails: ProductDetails) {
        val errorMap = validateInput(productDetails)
        val isInputValid = hasErrors(errorMap)

        _uiState.update {
            it.copy(
                productDetails = productDetails,
                isInputValid = isInputValid,
                detailsErrorMap = errorMap
            )
        }
    }

    private fun resetUiState() {
        val productDetails = ProductDetails(
            productNumber = productNumber ?: ""
        )

        _uiState.update {
            ProductEntryUiState(
                productDetails = productDetails,
                detailsErrorMap = mapOf(
                    Pair(PRODUCT_NUMBER_ERROR_KEY, null),
                    Pair(PRODUCT_NAME_ERROR_KEY, null),
                    Pair(PRODUCT_PRICE_ERROR_KEY, null),
                    Pair(PRODUCT_STOCK_ERROR_KEY, null)
                )
            )
        }
    }

    private fun validateInput(
        productDetails: ProductDetails = _uiState.value.productDetails
    ): Map<String, TextInputErrorType?> {
        val errorMap = _uiState.value.detailsErrorMap.toMutableMap()

        checkIfProductNumberExists(productDetails.productNumber)

        errorMap[PRODUCT_NAME_ERROR_KEY] =
            if (productDetails.productName.isEmpty()) {
                TextInputErrorType.FieldRequired
            } else {
                null
            }
        errorMap[PRODUCT_PRICE_ERROR_KEY] =
            if (productDetails.price.isEmpty()) {
                TextInputErrorType.FieldRequired
            } else {
                null
            }

        errorMap[PRODUCT_STOCK_ERROR_KEY] =
            if (productDetails.stock.isEmpty()) {
                TextInputErrorType.FieldRequired
            } else {
                null
            }

        return errorMap
    }

    // Had to separate this into its own function and coroutine
    // since updating the ui via a coroutine will create issues in the UI
    private fun checkIfProductNumberExists(
        productNumber: String
    ) {
        if (productNumber.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            if (inventoryRepository.checkIfProductNumberExists(productNumber)) {
                val errorMap = _uiState.value.detailsErrorMap.toMutableMap()
                errorMap[PRODUCT_NUMBER_ERROR_KEY] = TextInputErrorType.DuplicateFound

                _uiState.update {
                    it.copy(
                        detailsErrorMap = errorMap,
                        isInputValid = false
                    )
                }
            }
        }
    }

    private fun hasErrors(
        errorMap: Map<String, TextInputErrorType?>
    ): Boolean {

        errorMap.values.forEach {
            if (it != null) return false
        }

        return true
    }

    suspend fun insertProduct() {
        inventoryRepository.insertProduct(_uiState.value.productDetails.toProduct())
    }
}

data class ProductEntryUiState(
    val productDetails: ProductDetails = ProductDetails(),
    val detailsErrorMap: Map<String, TextInputErrorType?> = mapOf(),
    val isInputValid: Boolean = false,
)

data class ProductDetails(
    val id: String = "",
    val productNumber: String = "",
    val productName: String = "",
    val price: String = "",
    val stock: String = "",
    val imageUri: Uri? = null
)

fun ProductDetails.toProduct(): Product {
    return Product(
        id = id.toLongOrNull() ?: 0,
        productNumber = productNumber.ifBlank { null },
        productName = productName,
        price = price.toDoubleOrNull() ?: 0.0,
        stock = stock.toIntOrNull() ?: 0,
        imageUri = imageUri?.toString()
    )
}

fun Product.toProductDetails(): ProductDetails {
    return ProductDetails(
        id = id.toString(),
        productNumber = productNumber ?: "",
        productName = productName,
        price = price.toString(),
        stock = stock.toString(),
        imageUri = imageUri?.let { Uri.parse(it) }
    )
}

fun Product.formattedPrice(): String {
    return NumberFormat.getCurrencyInstance().format(price)
}