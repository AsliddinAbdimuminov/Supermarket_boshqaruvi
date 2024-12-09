package com.mobile_programming.sari_sari_inventory_app.ui.receipt

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentReceiptBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ProductWithAmount
import kotlinx.coroutines.launch
import java.text.NumberFormat

class ReceiptFragment : Fragment() {
    private var _binding: FragmentReceiptBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    private val viewModel: ReceiptViewModel by viewModels(
        ownerProducer = { requireActivity() },
        factoryProducer = { AppViewModelProvider.Factory }
    )

    companion object {
        const val MODAL_BOTTOM_SHEET_TAG = "ReceiptModalBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReceiptBinding.inflate(layoutInflater)
        navController = (activity as MainActivity).findNavController(R.id.main_nav_host_fragment)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context
        val adapter = ReceiptListAdapter(
            onItemClick = { productWithAmount ->
                toggleBottomSheet(
                    productWithAmount = productWithAmount,
                    isVisible = true
                )
            },
            onRemoveClick = { productDetails ->
                viewModel.removeProductFromReceipt(productDetails)
            }
        )

        binding.apply {
            receiptRecyclerView.layoutManager = LinearLayoutManager(context)
            receiptRecyclerView.adapter = adapter
            receiptRecyclerView.itemAnimator = null

            topAppToolbar.setNavigationOnClickListener {
                navController.popBackStack()
            }

            topAppToolbar.setOnMenuItemClickListener { menuItem ->
                onTopAppBarMenuItemClick(menuItem, context)
            }

            addProductFAB.setOnClickListener {
                navController.navigate(
                    ReceiptFragmentDirections.receiptToReceiptScanner()
                )
            }

            confirmButton.isEnabled = this@ReceiptFragment.viewModel
                .uiState.value.receiptMap.isNotEmpty()
            confirmButton.setOnClickListener {
                showSaleConfirmationDialog(context)
            }
        }

        adapter.submitList(viewModel.uiState.value.receiptMap.toList())

        viewModel.uiState.asLiveData().observe(
            viewLifecycleOwner
        ) {
            adapter.submitList(viewModel.uiState.value.receiptMap.toList())

            val total: String = getString(
                R.string.receipt_total,
                formatPrice(viewModel.uiState.value.total)
            )
            val styledTotal: Spanned = Html.fromHtml(total)

            binding.receiptTotal.text = styledTotal
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.apply {
            topAppToolbar.setOnMenuItemClickListener(null)
            topAppToolbar.setNavigationOnClickListener(null)
            receiptRecyclerView.adapter = null
            addProductFAB.setOnClickListener(null)
            confirmButton.setOnClickListener(null)

            uiState = null
            lifecycleOwner = null
        }

        viewModel.uiState.asLiveData().removeObservers(this)
        _binding = null
    }

    private fun onTopAppBarMenuItemClick(
        menuItem: MenuItem,
        context: Context
    ): Boolean {

        return when (menuItem.itemId) {

            R.id.clear_receipt -> {
                showClearReceiptDialog(context)
                true
            }

            else -> false
        }
    }

    private fun formatPrice(price: Double): String {
        return NumberFormat.getCurrencyInstance().format(price)
    }

    private fun toggleBottomSheet(
        productWithAmount: ProductWithAmount,
        isVisible: Boolean
    ) {
        viewModel.toggleBottomSheet(
            productWithAmount = productWithAmount,
            isVisible = isVisible
        )

        val modalBottomSheet = ReceiptModalBottomSheetFragment()
        modalBottomSheet.show(
            childFragmentManager,
            MODAL_BOTTOM_SHEET_TAG
        )
    }

    private fun showClearReceiptDialog(
        context: Context,
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.clear_receipt_dialog_title)
            .setMessage(R.string.clear_receipt_dialog_message)
            .setPositiveButton(R.string.clear) { _, _ ->
                viewModel.clearReceipt()
            }
            .setNeutralButton(R.string.cancel) { _, _ -> }
            .show()
    }

    private fun showSaleConfirmationDialog(
        context: Context
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.confirm_sale_dialog_title)
            .setMessage(R.string.confirm_sale_dialog_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                lifecycleScope.launch {
                    viewModel.confirmSale()
                    showSaleConfirmedToast(context)
                }
            }
            .setNegativeButton(R.string.clear) { _, _ ->
                viewModel.clearReceipt()
            }
            .setNeutralButton(R.string.cancel) { _, _ -> }
            .show()
    }

    private fun showSaleConfirmedToast(
        context: Context
    ) {
        Toast.makeText(
            context,
            context.getString(R.string.sale_confirmed),
            Toast.LENGTH_LONG
        ).show()
    }
}