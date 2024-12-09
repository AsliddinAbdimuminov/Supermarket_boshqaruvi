package com.mobile_programming.sari_sari_inventory_app.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.SearchBarState
import com.mobile_programming.sari_sari_inventory_app.utils.SortingType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private var _uiState = MutableStateFlow(
        InventoryUiState(
            searchBarState = SearchBarState(
                onQueryChange = ::updateQuery,
                onSearch = ::updateProductList
            )
        )
    )

    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            inventoryRepository.getAllProducts()
                .filterNotNull()
                .collect { productList ->
                    _uiState.update { oldState ->
                        oldState.copy(
                            productList = sortProductList(productList = productList)
                        )
                    }
                }
        }
    }

    private fun updateQuery(query: String) {
        val newSearchBarState = _uiState.value.searchBarState.copy(
            searchQuery = query
        )

        _uiState.update {
            it.copy(
                searchBarState = newSearchBarState
            )
        }
    }

    private fun updateProductList(query: String) {
        viewModelScope.launch {
            val productList =
                if (query.isEmpty()) getAllProducts()
                else getProducts(query)

            val sortedList = sortProductList(productList = productList)

            _uiState.update {
                it.copy(
                    productList = sortedList
                )
            }
        }
    }

    private suspend fun getAllProducts(): List<Product> {
        return inventoryRepository.getAllProducts()
            .filterNotNull()
            .first()
    }

    private suspend fun getProducts(nameOrNumber: String): List<Product> {
        return inventoryRepository.searchForProduct(nameOrNumber)
            .filterNotNull()
            .first()
    }

    suspend fun deleteProduct(product: Product) {
        inventoryRepository.deleteProduct(product)
    }

    fun changeSortingType(sortingType: SortingType, ascending: Boolean) {
        val sortAscending =
            if (sortingType == _uiState.value.sortingType) ascending
            else true

        _uiState.update {
            it.copy(
                sortingType = sortingType,
                isSortAscending = sortAscending
            )
        }

        updateProductList(_uiState.value.searchBarState.searchQuery)
    }

    private fun sortProductList(
        sortingType: SortingType = _uiState.value.sortingType,
        ascending: Boolean = _uiState.value.isSortAscending,
        productList: List<Product>
    ): List<Product> {
        return if (ascending) {
            productList.sortedWith(
                compareBy {
                    when (sortingType) {
                        SortingType.ByProductNumber -> it.productNumber
                        SortingType.ByPrice -> it.price
                        SortingType.ByStock -> it.stock
                        else -> it.productName.lowercase()
                    }
                }
            )
        } else {
            productList.sortedWith(
                compareByDescending {
                    when (sortingType) {
                        SortingType.ByProductNumber -> it.productNumber
                        SortingType.ByPrice -> it.price
                        SortingType.ByStock -> it.stock
                        else -> it.productName.lowercase()
                    }
                }
            )
        }
    }
}

data class InventoryUiState(
    val productList: List<Product> = listOf(),
    val sortingType: SortingType = SortingType.ByProductName,
    val isSortAscending: Boolean = true,
    val searchBarState: SearchBarState<Product> = SearchBarState()
)