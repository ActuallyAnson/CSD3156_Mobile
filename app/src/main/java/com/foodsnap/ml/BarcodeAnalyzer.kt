package com.foodsnap.ml

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import javax.inject.Inject

/**
 * Camera image analyzer for barcode scanning using ML Kit.
 *
 * Detects various barcode formats including:
 * - EAN-13, EAN-8 (product barcodes)
 * - UPC-A, UPC-E
 * - QR codes
 * - Code 128, Code 39
 *
 * @property onBarcodeDetected Callback when a barcode is successfully detected
 * @property onError Callback when an error occurs
 */
class BarcodeAnalyzer @Inject constructor() : ImageAnalysis.Analyzer {

    private var onBarcodeDetected: ((AnalyzerResult.BarcodeResult) -> Unit)? = null
    private var onError: ((AnalyzerResult.Error) -> Unit)? = null

    private val scanner = BarcodeScanning.getClient()

    /**
     * Sets the callback for barcode detection.
     *
     * @param callback Function to call when a barcode is detected
     */
    fun setOnBarcodeDetectedListener(callback: (AnalyzerResult.BarcodeResult) -> Unit) {
        onBarcodeDetected = callback
    }

    /**
     * Sets the callback for errors.
     *
     * @param callback Function to call when an error occurs
     */
    fun setOnErrorListener(callback: (AnalyzerResult.Error) -> Unit) {
        onError = callback
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcode = barcodes.first()
                    barcode.rawValue?.let { value ->
                        val format = getBarcodeFormatName(barcode.format)
                        Log.d(TAG, "Barcode detected: $value (format: $format)")

                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        val (uprightWidth, uprightHeight) = if (
                            rotationDegrees == 90 || rotationDegrees == 270
                        ) {
                            imageProxy.height to imageProxy.width
                        } else {
                            imageProxy.width to imageProxy.height
                        }

                        val boundingBox = barcode.boundingBox?.let { rect ->
                            BoundingBox(
                                left = rect.left,
                                top = rect.top,
                                right = rect.right,
                                bottom = rect.bottom
                            )
                        }

                        onBarcodeDetected?.invoke(
                            AnalyzerResult.BarcodeResult(
                                barcode = value,
                                format = format,
                                boundingBox = boundingBox,
                                imageWidth = uprightWidth,
                                imageHeight = uprightHeight,
                                rotationDegrees = rotationDegrees
                            )
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Barcode scanning failed", exception)
                onError?.invoke(
                    AnalyzerResult.Error(
                        message = "Failed to scan barcode: ${exception.message}",
                        exception = exception as? Exception
                    )
                )
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    /**
     * Converts barcode format constant to human-readable name.
     */
    private fun getBarcodeFormatName(format: Int): String {
        return when (format) {
            Barcode.FORMAT_EAN_13 -> "EAN_13"
            Barcode.FORMAT_EAN_8 -> "EAN_8"
            Barcode.FORMAT_UPC_A -> "UPC_A"
            Barcode.FORMAT_UPC_E -> "UPC_E"
            Barcode.FORMAT_QR_CODE -> "QR_CODE"
            Barcode.FORMAT_CODE_128 -> "CODE_128"
            Barcode.FORMAT_CODE_39 -> "CODE_39"
            Barcode.FORMAT_CODE_93 -> "CODE_93"
            Barcode.FORMAT_CODABAR -> "CODABAR"
            Barcode.FORMAT_ITF -> "ITF"
            Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
            Barcode.FORMAT_PDF417 -> "PDF417"
            Barcode.FORMAT_AZTEC -> "AZTEC"
            else -> "UNKNOWN"
        }
    }

    /**
     * Releases scanner resources.
     */
    fun close() {
        scanner.close()
    }

    companion object {
        private const val TAG = "BarcodeAnalyzer"
    }
}
