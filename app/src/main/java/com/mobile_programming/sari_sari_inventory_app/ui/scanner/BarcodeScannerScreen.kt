package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.Manifest
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.Size
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.compose.ui.graphics.Color as composeColor

@Composable
fun BarcodeScanner(
    modifier: Modifier = Modifier,
    scannerCameraState: ScannerCameraState,
) {

    val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        scannerCameraState.onPermissionResult(isGranted)
    }

    LaunchedEffect(scannerCameraState.isCameraFacingBack) {
        cameraPermissionResultLauncher.launch(
            Manifest.permission.CAMERA
        )
    }

    if (scannerCameraState.hasCameraAccess) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            BarcodeScannerPreviewView(
                isCameraFacingBack = scannerCameraState.isCameraFacingBack,
                onBarcodeScanned = scannerCameraState.onBarcodeScanned
            )
        }
    } else {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            Text(
                text = stringResource(id = R.string.scanner_no_camera_access),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun BarcodeScannerPreviewView(
    modifier: Modifier = Modifier,
    isCameraFacingBack: Boolean,
    onBarcodeScanned: (String) -> Unit,
) {
    val lensFacing =
        if (isCameraFacingBack) CameraSelector.LENS_FACING_BACK
        else CameraSelector.LENS_FACING_FRONT

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current

    val previewView = remember { PreviewView(context) }

    var cameraProvider: ProcessCameraProvider? = null
    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val screenDensity = LocalDensity.current.density

    val overlay =
        ScannerOverlay(
            size = 256 * screenDensity,
            borderRadius = 56f,
            viewWidth = screenWidth.value * screenDensity,
            viewHeight = screenHeight.value * screenDensity
        )
    val imageResolution = Size(1280, 720)

    LaunchedEffect(lensFacing) {
        cameraProvider = context.getCameraProvider()
        val preview = Preview.Builder().build()
        val imageAnalysis = createImageAnalysis(
            context,
            imageResolution,
            overlay,
        ) {
            onBarcodeScanned(it)
        }

        cameraProvider?.unbindAll()
        cameraProvider?.bindToLifecycle(
            lifecycleOwner,
            cameraxSelector,
            preview,
            imageAnalysis
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    DisposableEffect(cameraProvider) {
        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(factory = {
            previewView.apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                setBackgroundColor(Color.BLACK)
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        })


        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPath(
                path = overlay.path,
                color = composeColor.White,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                ),
            )
        }

        Text(
            text = stringResource(R.string.scan_barcode),
            style = MaterialTheme.typography.headlineSmall,
            color = composeColor.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(
                    y = -(overlay.size / Resources.getSystem().displayMetrics.density / 1.5).dp
                )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerSearchBar(
    modifier: Modifier = Modifier,
    searchBarState: SearchBarState<Product>,
    onResultClick: (Product) -> Unit,
    navigateToProductEntry: () -> Unit
) {

    SearchBar(
        query = searchBarState.searchQuery,
        onQueryChange = { searchBarState.onQueryChange(it) },
        onSearch = { searchBarState.onQueryChange(it) },
        active = searchBarState.isActive,
        onActiveChange = { searchBarState.onActiveChange(it) },
        placeholder = { Text("Mahsulot raqami yoki nomini kiriting") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            if (searchBarState.isActive) {
                IconButton(onClick = { searchBarState.onActiveChange(false) }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null
                    )
                }
            }
        },
        modifier = modifier,
    ) {
        BarcodeSearchResultsList(
            products = searchBarState.result,
            onResultClick = onResultClick,
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    dimensionResource(R.dimen.padding_small)
                )
        )

        OutlinedButton(
            onClick = navigateToProductEntry,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Text(
                    text = stringResource(R.string.add_new_product)
                )
            }
        }
    }
}

@Composable
fun BarcodeSearchResultsList(
    products: List<Product>,
    modifier: Modifier = Modifier,
    onResultClick: (Product) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        items(products) {
            BarcodeSearchResultsItem(
                product = it,
                modifier = Modifier.clickable { onResultClick(it) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(
                    horizontal = dimensionResource(R.dimen.padding_small)
                )
            )
        }
    }
}

@Composable
fun BarcodeSearchResultsItem(
    modifier: Modifier = Modifier,
    product: Product
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                dimensionResource(R.dimen.padding_small)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = product.productName,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = product.productNumber ?: "",
                style = MaterialTheme.typography.labelSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerTopAppBar(
    modifier: Modifier = Modifier,
    scannerCameraState: ScannerCameraState,
    canNavigateBack: Boolean,
    onNavigateUp: () -> Unit,
    navigateToProductEntry: () -> Unit
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateUp
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(
                            id = R.string.top_app_bar_navigate_up_content_desc
                        )
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = navigateToProductEntry
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(
                        R.string.add_new_product
                    )
                )
            }

            if (scannerCameraState.hasCameraAccess) {
                IconButton(
                    onClick = scannerCameraState.switchCamera
                ) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.round_flip_camera_android_24
                        ),
                        contentDescription = stringResource(
                            id = R.string.flip_camera_content_description
                        )
                    )
                }
            }
        },
        colors = if (scannerCameraState.hasCameraAccess) {
            transparentTopAppBarColors()
        } else {
            transparentTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            )
        },
        modifier = modifier
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun transparentTopAppBarColors(
    containerColor: composeColor = composeColor.Transparent,
    scrolledContainerColor: composeColor = composeColor.Transparent,
    navigationIconContentColor: composeColor? = null,
    titleContentColor: composeColor? = null,
    actionIconContentColor: composeColor? = null
): TopAppBarColors {
    return TopAppBarDefaults.topAppBarColors(
        containerColor = containerColor,
        scrolledContainerColor = scrolledContainerColor,
        navigationIconContentColor = navigationIconContentColor
            ?: if (containerColor == composeColor.Transparent) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        titleContentColor = titleContentColor
            ?: if (containerColor == composeColor.Transparent) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        actionIconContentColor = actionIconContentColor
            ?: if (containerColor == composeColor.Transparent) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.onSurface
            },
    )
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

private fun createImageAnalysis(
    context: Context,
    imageResolution: Size,
    overlay: ScannerOverlay,
    onBarcodeScanned: (String) -> Unit
): ImageAnalysis {

    val resolutionStrategy = ResolutionStrategy(
        imageResolution,
        ResolutionStrategy.FALLBACK_RULE_NONE
    )
    val resolutionSelector = ResolutionSelector.Builder()
        .setResolutionStrategy(resolutionStrategy)
        .build()

    val imageAnalysis = ImageAnalysis.Builder()
        .setResolutionSelector(resolutionSelector)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    imageAnalysis.setAnalyzer(
        ContextCompat.getMainExecutor(context),
        BarcodeAnalyzer(overlay) {
            onBarcodeScanned(it)
        }
    )

    return imageAnalysis
}