package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage


class BarcodeAnalyzer(
    private val overlay: ScannerOverlay,
    private val onBarcodeScanned: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()

        .setBarcodeFormats(
            Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_93, Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_EAN_8, Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_UPC_E, Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_ITF, Barcode.FORMAT_CODABAR
        )
        .build()
    private val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if(mediaImage == null) {
            image.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if(barcodes.isNotEmpty()
                    && isInBoundingBox(
                        barcodes.first(),
                        inputImage.width,
                        inputImage.height,
                        inputImage.rotationDegrees
                    )
                ) {
                    Log.d(
                        BarcodeAnalyzer::class.simpleName,
                        "Skanerlangan shtrix-kod raqami: ${barcodes.first().rawValue}"
                    )

                    onBarcodeScanned(barcodes.first().rawValue ?: "")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(
                    BarcodeAnalyzer::class.simpleName,
                    "${BarcodeAnalyzer::class.simpleName}: Nimadir xato ketdi $exception"
                )
            }
            .addOnCompleteListener {
                image.close()
            }
    }

    private fun isInBoundingBox(
        barcode: Barcode,
        imageWidth: Int,
        imageHeight: Int,
        imageRotation: Int,
    ) : Boolean {

        val scaleFactor =
            if(imageRotation == 90 || imageRotation == 270) {
                maxOf(
                    overlay.viewHeight / imageWidth,
                    overlay.viewWidth / imageHeight
                )
            } else {
                maxOf(
                    overlay.viewWidth / imageWidth,
                    overlay.viewHeight / imageHeight
                )
            }

        val barcodeCorners = barcode.cornerPoints ?: return false

        val isInside =
            with(barcodeCorners) {
                get(0).x >= overlay.rect.left / scaleFactor
                    && get(0).y >= overlay.rect.top / scaleFactor
                    && get(2).x <= overlay.rect.right / scaleFactor
                    && get(2).y <= overlay.rect.bottom / scaleFactor
            }

        return isInside
    }

}