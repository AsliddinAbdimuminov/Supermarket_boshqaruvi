package com.mobile_programming.sari_sari_inventory_app.ui.receipt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentReceiptBottomModalSheetBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.theme.SariSariInventoryAppTheme

class ReceiptModalBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentReceiptBottomModalSheetBinding
    private val viewModel: ReceiptViewModel by viewModels(
        ownerProducer = { requireActivity() },
        factoryProducer = { AppViewModelProvider.Factory }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReceiptBottomModalSheetBinding.inflate(layoutInflater)
        binding.composeViewContainer.setContent {

            val uiState = viewModel.uiState.collectAsState()
            val bottomSheetState = uiState.value.bottomSheetState.copy(
                onDismissRequest = {
                    dismiss()
                    viewModel.toggleBottomSheet(isVisible = false)
                },
                onConfirmClick = {
                    uiState.value.bottomSheetState.onConfirmClick()
                    dismiss()
                }
            )

            SariSariInventoryAppTheme {
                if(uiState.value.bottomSheetState.isShowing){
                    val sheetState = rememberModalBottomSheetState()

                    ReceiptScannerBottomSheet(
                        sheetState = sheetState,
                        bottomSheetState = bottomSheetState
                    ) { productWithAmount ->
                        viewModel.addProductToReceipt(productWithAmount)
                    }
                }
            }
        }

        return binding.root
    }
}