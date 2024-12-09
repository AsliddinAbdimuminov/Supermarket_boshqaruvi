package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import androidx.lifecycle.ViewModel
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

abstract class BarcodeScannerViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    protected var scannerCameraState = ScannerCameraState()

    abstract fun onBarcodeScanned(productNumber: String)

    open fun onPermissionResult(isGranted: Boolean) {
        scannerCameraState = scannerCameraState.copy(
            hasCameraAccess = isGranted
        )
    }

    open fun switchCamera() {
        scannerCameraState = scannerCameraState.copy(
            isCameraFacingBack = !scannerCameraState.isCameraFacingBack
        )
    }

    protected suspend fun searchForProduct(nameOrNumber: String): List<Product> {

        return if (nameOrNumber.isNotEmpty()) {
            inventoryRepository.searchForProduct(nameOrNumber)
                .filterNotNull()
                .first()
        } else {
            listOf()
        }
    }
}

data class ScannerCameraState(
    val hasCameraAccess: Boolean = false,
    val isCameraFacingBack: Boolean = true,
    val onPermissionResult: (Boolean) -> Unit = { },
    val switchCamera: () -> Unit = { },
    val onBarcodeScanned: (String) -> Unit = { }
)