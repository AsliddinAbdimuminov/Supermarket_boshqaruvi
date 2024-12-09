package com.mobile_programming.sari_sari_inventory_app.ui.receipt

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.product.formattedPrice
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProduct
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProductDetails
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.BarcodeScanner
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.BarcodeScannerSearchBar
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.BarcodeScannerTopAppBar
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.BottomSheetProductDetailsRow
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.NewProductDialog
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ProductAmountBottomSheetState
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ProductWithAmount
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ScannerUiState
import kotlinx.coroutines.launch

@Composable
fun ReceiptScannerScreen(
    modifier: Modifier = Modifier,
    scannerViewModel: ReceiptScannerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    receiptViewModel: ReceiptViewModel = viewModel(factory = AppViewModelProvider.Factory),
    canNavigateBack: Boolean,
    onNavigateUp: () -> Unit,
    navigateToProductEntry: (String) -> Unit,
) {
    val uiState = scannerViewModel.uiState.collectAsState()

    Box(
        modifier = modifier
    ) {
        ReceiptScannerBody(
            uiState = uiState,
            onResultClick = {
                scannerViewModel.updateProductWithAmount(
                    productDetails = it.toProductDetails()
                )
                scannerViewModel.toggleBottomSheet(true)
                scannerViewModel.toggleSearchBar(false)
            },
            addToReceiptList = { receiptViewModel.addProductToReceipt(it) },
            onDialogDismiss = scannerViewModel::clearBarcodeScanned,
            navigateToProductEntry = {
                navigateToProductEntry(it)
                scannerViewModel.clearBarcodeScanned()
            },
        )

        if (!uiState.value.searchBarState.isActive) {
            BarcodeScannerTopAppBar(
                scannerCameraState = uiState.value.scannerCameraState,
                canNavigateBack = canNavigateBack,
                onNavigateUp = onNavigateUp,
                navigateToProductEntry = {
                    navigateToProductEntry("")
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(10f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScannerBody(
    modifier: Modifier = Modifier,
    uiState: State<ScannerUiState>,
    addToReceiptList: (ProductWithAmount) -> Unit,
    onResultClick: (Product) -> Unit,
    onDialogDismiss: () -> Unit,
    navigateToProductEntry: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = modifier.fillMaxSize()) {
        if (!uiState.value.searchBarState.isActive) {
            BarcodeScanner(
                scannerCameraState = uiState.value.scannerCameraState
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            if (!uiState.value.searchBarState.isActive) {
                Text(
                    text = stringResource(R.string.search_manually),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }

            BarcodeScannerSearchBar(
                searchBarState = uiState.value.searchBarState,
                onResultClick = onResultClick,
                navigateToProductEntry = { navigateToProductEntry("") },
                modifier = Modifier.then(
                    if (!uiState.value.searchBarState.isActive) {
                        Modifier.padding(
                            bottom = dimensionResource(R.dimen.padding_small),
                            start = dimensionResource(R.dimen.padding_small),
                            end = dimensionResource(R.dimen.padding_small)
                        )
                    } else {
                        Modifier
                    }
                )
            )
        }

        if (uiState.value.bottomSheetState.isShowing) {
            ReceiptScannerBottomSheet(
                sheetState = sheetState,
                bottomSheetState = uiState.value.bottomSheetState,
                addToReceiptList = addToReceiptList
            )
        }

        if (uiState.value.isNewProduct) {
            NewProductDialog(
                onDismissRequest = onDialogDismiss,
                navigateToProductEntry = {
                    navigateToProductEntry(
                        uiState.value.scannedBarcode
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReceiptScannerBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    bottomSheetState: ProductAmountBottomSheetState,
    addToReceiptList: (ProductWithAmount) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = { bottomSheetState.onDismissRequest() },
        sheetState = sheetState,
        windowInsets = WindowInsets.ime,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.padding_medium)
            ),
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.padding_extra_large)
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.padding_small)
                )
            ) {
                BottomSheetProductDetailsRow(
                    labelResId = R.string.product_number,
                    productDetail = bottomSheetState.productWithAmount
                        .productDetails.productNumber,
                    modifier = Modifier.fillMaxWidth()
                )

                BottomSheetProductDetailsRow(
                    labelResId = R.string.product_name,
                    productDetail = bottomSheetState.productWithAmount
                        .productDetails.productName,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.padding_large)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BottomSheetProductDetailsRow(
                        labelResId = R.string.product_price,
                        productDetail = bottomSheetState.productWithAmount
                            .productDetails
                            .toProduct()
                            .formattedPrice()
                    )

                    BottomSheetProductDetailsRow(
                        labelResId = R.string.product_stock,
                        productDetail = bottomSheetState.productWithAmount
                            .productDetails.stock,
                    )
                }
            }

            OutlinedTextField(
                value = bottomSheetState.productWithAmount.amount.toString(),
                onValueChange = { bottomSheetState.onValueChange(it) },
                label = {
                    Text(
                        stringResource(
                            R.string.amount
                        )
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )

            Button(
                onClick = {
                    scope.launch {
                        addToReceiptList(
                            bottomSheetState.productWithAmount
                        )

                        bottomSheetState.onConfirmClick()
                        Toast.makeText(
                            context,
                            getString(context, R.string.product_added_to_receipt),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                enabled = bottomSheetState.isInputValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(
                            R.dimen.padding_extra_large
                        )
                    )
                    .clip(MaterialTheme.shapes.large)
            ) {
                Text(
                    text = stringResource(R.string.add_to_receipt)
                )
            }

            Spacer(
                modifier = Modifier.windowInsetsBottomHeight(
                    WindowInsets.navigationBarsIgnoringVisibility
                )
            )
        }
    }
}