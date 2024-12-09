package com.mobile_programming.sari_sari_inventory_app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.data.relation.ProductSale
import com.mobile_programming.sari_sari_inventory_app.utils.SortingType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar

class HomeViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private var _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshHomeContent()
    }

    fun refreshHomeContent() {
        viewModelScope.launch {
            val outOfStockCount = getOutOfStockCount()
            val lowOnStockCount = getLowOnStockCount()
            val productSales = getProductSalesLastMonth()

            val sortedProductSales = sortProductSalesList(
                productSales = productSales,
                sortingType = _uiState.value.sortingType,
                ascending = _uiState.value.sortAscending
            )

            val productStatsList = getProductStatsList(
                sortedProductSales
            )

            _uiState.update {
                it.copy(
                    outOfStockCount = outOfStockCount,
                    lowOnStockCount = lowOnStockCount,
                    productSales = productSales,
                    productStats = productStatsList
                )
            }
        }
    }

    private suspend fun getOutOfStockCount() : Int {
        return inventoryRepository.countOutOfStock()
            .filterNotNull()
            .first()
    }

    private suspend fun getLowOnStockCount() : Int {
        return inventoryRepository.countLowOnStock(10)
            .filterNotNull()
            .first()
    }

    private suspend fun getProductSalesLastMonth() : List<ProductSale> {
        return inventoryRepository.getProductSalesFromDates(
            id = null,
            dateFrom = Calendar.getInstance().apply { add(Calendar.DATE, -30) }.time,
            dateTo = Calendar.getInstance().apply { add(Calendar.DATE, 0) }.time
        )
            .filterNotNull()
            .first()
    }

    fun changeSortingType(
        sortingType: SortingType = _uiState.value.sortingType,
        ascending: Boolean = true
    ) {
        val sortedSales = sortProductSalesList(
            productSales = _uiState.value.productSales,
            sortingType = sortingType,
            ascending = ascending
        )

        val productStats = getProductStatsList(
            productSales = sortedSales,
            sortingType = sortingType
        )

        _uiState.update { oldUiState ->
            oldUiState.copy(
                productSales = sortedSales,
                productStats = productStats,
                sortingType = sortingType,
                sortAscending = ascending
            )
        }
    }

    private fun sortProductSalesList(
        productSales: List<ProductSale>,
        sortingType: SortingType,
        ascending: Boolean
    ): List<ProductSale> {

        return if (ascending) {
            productSales.sortedWith(
                compareBy {
                    when (sortingType) {
                        SortingType.ByNumberSold -> it.amountSold
                        else -> it.totalRevenue
                    }
                }
            )
        } else {
            productSales.sortedWith(
                compareByDescending {
                    when (sortingType) {
                        SortingType.ByNumberSold -> it.amountSold
                        else -> it.totalRevenue
                    }
                }
            )
        }
    }

    private fun getProductStatsList(
        productSales: List<ProductSale>,
        sortingType: SortingType = _uiState.value.sortingType
    ): List<ProductWithStatistic> {

        return productSales.map { productSale ->
            if (sortingType == SortingType.ByNumberSold) {
                ProductWithStatistic(
                    productSale.product,
                    productSale.amountSold.toString()
                )
            } else {
                ProductWithStatistic(
                    productSale.product,
                    formatPrice(productSale.totalRevenue)
                )
            }
        }
    }

    private fun formatPrice(price: Double): String {
        return NumberFormat.getCurrencyInstance().format(price)
    }
}

data class HomeUiState(
    val outOfStockCount: Int = 0,
    val lowOnStockCount: Int = 0,
    val productSales: List<ProductSale> = listOf(),
    val productStats: List<ProductWithStatistic> = listOf(),
    val sortAscending: Boolean = false,
    val sortingType: SortingType = SortingType.ByNumberSold,
)

data class ProductWithStatistic(
    val product: Product?,
    val statistic: String
)