package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.widget.Toast
import androidx.annotation.StringRes
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProductDetails
import com.mobile_programming.sari_sari_inventory_app.ui.theme.SariSariInventoryAppTheme
import kotlinx.coroutines.launch

@Composable
fun StockUpScannerScreen(
    modifier: Modifier = Modifier,
    viewModel: StockUpScannerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    canNavigateBack: Boolean,
    onNavigateUp: () -> Unit,
    navigateToProductEntry: (String) -> Unit,
) {
    val uiState = viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
    ) {
        StockUpScannerBody(
            uiState = uiState,
            onResultClick = {
                viewModel.updateProductWithAmount(
                    productDetails = it.toProductDetails()
                )
                viewModel.toggleBottomSheet(true)
                viewModel.toggleSearchBar(false)
            },
            onDialogDismiss = viewModel::clearBarcodeScanned,
            navigateToProductEntry = {
                navigateToProductEntry(it)
                viewModel.clearBarcodeScanned()
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
fun StockUpScannerBody(
    modifier: Modifier = Modifier,
    uiState: State<ScannerUiState>,
    onResultClick: (Product) -> Unit,
    onDialogDismiss: () -> Unit,
    navigateToProductEntry: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = modifier.fillMaxSize()) {
        if (!uiState.value.searchBarState.isActive) {
            BarcodeScanner(
                scannerCameraState = uiState.value.scannerCameraState,
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
            StockUpBottomSheet(
                sheetState = sheetState,
                bottomSheetState = uiState.value.bottomSheetState
            )
        }

        if (uiState.value.isNewProduct) {
            NewProductDialog(
                onDismissRequest = onDialogDismiss,
                navigateToProductEntry = {
                    navigateToProductEntry(uiState.value.scannedBarcode)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StockUpBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    bottomSheetState: ProductAmountBottomSheetState,
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
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.padding_small)
                )
            ) {
                BottomSheetProductDetailsRow(
                    labelResId = R.string.product_number,
                    productDetail = bottomSheetState.productWithAmount.productDetails.productNumber,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensionResource(
                                R.dimen.padding_extra_large
                            )
                        )
                )

                BottomSheetProductDetailsRow(
                    labelResId = R.string.product_name,
                    productDetail = bottomSheetState.productWithAmount.productDetails.productName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensionResource(
                                R.dimen.padding_extra_large
                            )
                        )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(
                            R.dimen.padding_extra_large
                        )
                    )
            ) {
                BottomSheetProductDetailsRow(
                    labelResId = R.string.product_stock,
                    productDetail = bottomSheetState.productWithAmount.productDetails.stock,
                )

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(
                            R.dimen.padding_large
                        )
                    )
                )

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
            }

            Button(
                onClick = {
                    scope.launch {
                        bottomSheetState.onConfirmClick()
                        Toast.makeText(
                            context,
                            getString(context, R.string.product_stock_updated),
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
                    text = stringResource(R.string.add_to_stock)
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

@Composable
fun BottomSheetProductDetailsRow(
    @StringRes labelResId: Int,
    productDetail: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        Text(
            text = stringResource(labelResId),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = productDetail,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
        )
    }
}

@Composable
fun NewProductDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    navigateToProductEntry: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Card {
            Column(
                modifier = modifier
                    .padding(
                        dimensionResource(R.dimen.padding_large)
                    ),
            ) {
                Text(
                    text = stringResource(R.string.new_product_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(
                    Modifier.padding(
                        bottom = dimensionResource(R.dimen.padding_large)
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }

                    TextButton(
                        onClick = navigateToProductEntry
                    ) {
                        Text(text = stringResource(R.string.confirm))
                    }
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BarcodeSearchResultListPreview() {
    val products = listOf(
        Product(
            id = 0,
            productNumber = "123131312",
            productName = "Pomidor sousidagi Young's Town sardinalari / issiq chili 155 g",
            price = 75.0,
            stock = 100,
            imageUri = null
        ),
        Product(
            id = 0,
            productNumber = "123131312",
            productName = "Pomidor sousidagi Young's Town sardinalari / issiq chili 155 g",
            price = 175.0,
            stock = 10,
            imageUri = null
        )
    )

    SariSariInventoryAppTheme {
        BarcodeSearchResultsList(
            products = products,
            onResultClick = { },
        )
    }
}