package com.mobile_programming.sari_sari_inventory_app.ui.inventory

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mobile_programming.sari_sari_inventory_app.MainActivity
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentInventoryBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.product.ProductDetails
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProduct
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProductDetails
import com.mobile_programming.sari_sari_inventory_app.utils.SortingType
import kotlinx.coroutines.launch

class InventoryFragment : Fragment() {
    private var _binding : FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private val viewModel: InventoryViewModel by viewModels { AppViewModelProvider.Factory }

    private var originalSoftInputMode: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentInventoryBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        navController = (activity as MainActivity).findNavController(R.id.main_nav_host_fragment)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = InventoryListAdapter(
            onItemClick = { productId ->
                navController.navigate(
                    InventoryFragmentDirections.inventoryToProductDetails(
                        productId
                    )
                )
            },
            onDeleteClick = { product ->
                lifecycleScope.launch {
                    showDeleteConfirmationDialog(
                        context = requireContext(),
                        productDetails = product.toProductDetails()
                    )
                }
            }
        )

        adapter.submitList(viewModel.uiState.value.productList)

        viewModel.uiState.asLiveData().observe(
            viewLifecycleOwner
        ) {
            adapter.submitList(viewModel.uiState.value.productList)
        }

        binding.apply {
            inventoryRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.inventoryRecyclerView.adapter = adapter
            binding.inventoryRecyclerView.itemAnimator = null

            topAppToolbar.setNavigationOnClickListener {
                navController.popBackStack()
            }

            botNavBar.selectedItemId = R.id.inventoryFragment
            botNavBar.setOnItemSelectedListener { onNavItemClick(it) }

            sortMenuButton.setOnClickListener { hostView: View ->
                showPopupMenu(hostView, R.menu.sorting_type_menu)
            }

            addProductFAB.setOnClickListener {
                navController.navigate(
                    InventoryFragmentDirections.inventoryToStockUpScanner()
                )
            }
        }

        val searchBar = binding.searchBar.editText

        searchBar?.doAfterTextChanged {
            viewModel.uiState.value.searchBarState.onQueryChange(it.toString())
        }

        searchBar?.setOnKeyListener { _, keyCode, event ->
            if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                viewModel.uiState.value.searchBarState.onSearch(
                    viewModel.uiState.value.searchBarState.searchQuery
                )
                true
            } else {
                false
            }
        }
    }

    override fun onResume() {
        originalSoftInputMode = activity?.window?.getSoftInputMode()
        activity?.window?.attributes?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        super.onResume()
    }

    override fun onPause() {
        activity?.window?.attributes?.softInputMode = originalSoftInputMode
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.apply {
            botNavBar.setOnItemSelectedListener(null)
            inventoryRecyclerView.adapter = null
            sortMenuButton.setOnClickListener(null)
            addProductFAB.setOnClickListener(null)
            searchBar.editText?.removeTextChangedListener(null)
            searchBar.editText?.keyListener = null
            viewModel = null
            lifecycleOwner = null
        }

        viewModel.uiState.asLiveData().removeObservers(this)
        _binding = null
    }

    private fun onNavItemClick(navItem: MenuItem) : Boolean {
        return when(navItem.itemId) {
            R.id.homeFragment -> {
                navController.navigate(InventoryFragmentDirections.inventoryToHome())
                false
            }

            R.id.inventoryFragment -> true

            R.id.receiptFragment -> {
                navController.navigate(InventoryFragmentDirections.inventoryToReceipt())
                false
            }

            R.id.settingsFragment -> {
                navController.navigate(InventoryFragmentDirections.inventoryToSettings())
                false
            }

            else -> false
        }
    }

    private fun showPopupMenu(view: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when(menuRes) {
                R.menu.sorting_type_menu -> changeSortingType(menuItem)
                else -> false
            }
        }
        popup.setOnDismissListener {
            popup.dismiss()
        }
        // Show the popup menu.
        popup.show()
    }

    private fun changeSortingType(menuItem: MenuItem) : Boolean {
        val ascending = !viewModel.uiState.value.isSortAscending

        return when(menuItem.itemId) {
            R.id.sortByProductName -> {
                viewModel.changeSortingType(
                    sortingType = SortingType.ByProductName,
                    ascending = ascending
                )
                true
            }

            R.id.sortByProductNumber -> {
                viewModel.changeSortingType(
                    sortingType = SortingType.ByProductNumber,
                    ascending = ascending
                )
                true
            }

            R.id.sortByProductPrice -> {
                viewModel.changeSortingType(
                    sortingType = SortingType.ByProductName,
                    ascending = ascending
                )
                true
            }

            R.id.sortByProductStock -> {
                viewModel.changeSortingType(
                    sortingType = SortingType.ByStock,
                    ascending = ascending
                )
                true
            }

            else -> false
        }
    }

    private fun showDeleteConfirmationDialog(
        context: Context,
        productDetails: ProductDetails
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.delete_confirmation_title)
            .setMessage(R.string.delete_confirmation_message)
            .setNeutralButton(R.string.cancel, null)
            .setPositiveButton(R.string.confirm) { _, _ ->
                deleteSelectedProduct(context, productDetails)
            }
            .show()
    }

    private fun showProductDeletedToast(context: Context) {
        Toast.makeText(
            context,
            context.getString(R.string.product_deleted),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun deleteSelectedProduct(
        context: Context,
        productDetails: ProductDetails
    ) {
        lifecycleScope.launch {
            productDetails.imageUri?.let { uri ->
                context.contentResolver.delete(
                    uri,
                    null,
                    null
                )
            }

            viewModel.deleteProduct(productDetails.toProduct())
            showProductDeletedToast(context)
        }
    }
}

fun Window.getSoftInputMode() : Int {
    return attributes.softInputMode
}