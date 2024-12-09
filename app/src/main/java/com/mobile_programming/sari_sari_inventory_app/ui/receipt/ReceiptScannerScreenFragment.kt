package com.mobile_programming.sari_sari_inventory_app.ui.receipt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.mobile_programming.sari_sari_inventory_app.MainActivity
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentReceiptScannerBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.theme.SariSariInventoryAppTheme

class ReceiptScannerScreenFragment : Fragment() {
    private lateinit var binding: FragmentReceiptScannerBinding
    private lateinit var navController: NavController

    private val scannerViewModel: ReceiptScannerViewModel by viewModels(
        ownerProducer = { requireActivity() },
        factoryProducer = { AppViewModelProvider.Factory }
    )
    private val receiptViewModel: ReceiptViewModel by viewModels(
        ownerProducer = { requireActivity() },
        factoryProducer = { AppViewModelProvider.Factory }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_receipt_scanner,
            container,
            false
        )

        navController = (activity as MainActivity).findNavController(R.id.main_nav_host_fragment)

        binding.receiptScannerScreenComposeView.setContent {
            SariSariInventoryAppTheme {
                ReceiptScannerScreen(
                    scannerViewModel = scannerViewModel,
                    receiptViewModel = receiptViewModel,
                    canNavigateBack = true,
                    onNavigateUp = { navController.popBackStack() },
                    navigateToProductEntry = {
                        navController.navigate(
                            ReceiptScannerScreenFragmentDirections
                                .receiptScannerToProductEntry(
                                    it
                                )
                        )
                    }
                )
            }
        }

        return binding.root
    }
}