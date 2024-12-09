package com.mobile_programming.sari_sari_inventory_app.ui.receipt

import androidx.lifecycle.ViewModel
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import com.mobile_programming.sari_sari_inventory_app.ui.product.ProductDetails
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProduct
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ProductAmountBottomSheetState
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ProductWithAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.util.Calendar

class ReceiptViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private var _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    init {
        val bottomSheetState = ProductAmountBottomSheetState(
            onDismissRequest = { toggleBottomSheet(isVisible = false) },
            onValueChange = {
                updateProductWithAmount(
                    amount = it.toIntOrNull() ?: 0
                )
            },
            onConfirmClick = suspend {
                updateProductAmount(_uiState.value.bottomSheetState.productWithAmount)
                toggleBottomSheet(isVisible = false)
            }
        )

        _uiState.update {
            it.copy(
                bottomSheetState = bottomSheetState
            )
        }
    }

    fun addProductToReceipt(productWithAmount: ProductWithAmount) {
        val receiptMap = _uiState.value.receiptMap.toMutableMap()
        val productDetails = productWithAmount.productDetails
        val amount = productWithAmount.amount

        receiptMap[productDetails] = amount

        val total = getReceiptTotal(receiptMap)

        _uiState.update {
            it.copy(
                receiptMap = receiptMap,
                total = total
            )
        }
    }

    fun removeProductFromReceipt(productDetails: ProductDetails) {
        val receiptMap = _uiState.value.receiptMap.toMutableMap()
        val productTotal = (productDetails.price.toDoubleOrNull() ?: 0.0)
                .times(receiptMap[productDetails] ?: 0)

        _uiState.update {
            it.copy(
                receiptMap = receiptMap.apply {
                    remove(productDetails)
                },
                total = it.total - productTotal
            )
        }
    }

    private fun getReceiptTotal(receiptMap: Map<ProductDetails, Int>): Double {
        var total = 0.0

        receiptMap.forEach {
            total += it.key.price.toDouble() * it.value
        }

        return total
    }

    suspend fun confirmSale() {
        val receiptMap = _uiState.value.receiptMap
        insertReceipt(receiptMap)

        receiptMap.forEach {
            subtractProductStock(it.key.toProduct(), it.value)
        }

        clearReceipt()
    }

    private suspend fun insertReceipt(receiptMap: Map<ProductDetails, Int>): Long {

        val productsWithAmounts = receiptMap.mapKeys {
            it.key.toProduct()
        }

        val receipt = Receipt(
            id = 0,
            dateCreated = Calendar.getInstance().time,
        )

        return inventoryRepository.insertReceipt(receipt, productsWithAmounts)
    }

    private suspend fun subtractProductStock(product: Product, amount: Int) {
        inventoryRepository.updateProduct(
            product.copy(stock = product.stock - amount)
        )
    }

    fun clearReceipt() {
        _uiState.update {
            ReceiptUiState()
        }
    }

    fun toggleBottomSheet(
        productWithAmount: ProductWithAmount = _uiState.value.bottomSheetState.productWithAmount,
        isVisible: Boolean
    ) {
        _uiState.update {
            it.copy(
                bottomSheetState = it.bottomSheetState.copy(
                    productWithAmount = productWithAmount,
                    isShowing = isVisible
                )
            )
        }
    }

    private fun updateProductAmount(productWithAmount: ProductWithAmount) {
        val receiptMap = _uiState.value.receiptMap.toMutableMap()

        if (!receiptMap.containsKey(productWithAmount.productDetails)) return

        receiptMap[productWithAmount.productDetails] = productWithAmount.amount

        _uiState.update {
            it.copy(
                receiptMap = receiptMap
            )
        }
    }

    private fun updateProductWithAmount(
        productDetails: ProductDetails =
            _uiState.value.bottomSheetState.productWithAmount.productDetails,
        amount: Int = 0
    ) {
        val bottomSheetState = _uiState.value.bottomSheetState
        val productWithAmount = bottomSheetState.productWithAmount.copy(
            productDetails = productDetails,
            amount = amount,
        )

        val newBottomSheetState = bottomSheetState.copy(
            productWithAmount = productWithAmount,
            isInputValid = validateInput(productWithAmount)
        )

        _uiState.update {
            it.copy(
                bottomSheetState = newBottomSheetState
            )
        }
    }

    private fun validateInput(productWithAmount: ProductWithAmount) : Boolean {
        val productStock = productWithAmount.productDetails.stock.toIntOrNull() ?: 0
        val amount = productWithAmount.amount

        return amount != 0 && amount <= productStock
    }
}

data class ReceiptUiState(
    val bottomSheetState: ProductAmountBottomSheetState = ProductAmountBottomSheetState(),
    val receiptMap: Map<ProductDetails, Int> = mapOf(),
    val total: Double = 0.0
)

fun ProductWithAmount.getTotalPrice() : Double {
    return (productDetails.price.toDoubleOrNull() ?: 0.0) * amount
}

fun ProductWithAmount.formattedTotalPrice() : String {
    return NumberFormat.getCurrencyInstance().format(getTotalPrice())
}

fun Map<ProductDetails, Int>.toList() : List<ProductWithAmount> {
    return this.map {
        ProductWithAmount(it.key, it.value)
    }
}