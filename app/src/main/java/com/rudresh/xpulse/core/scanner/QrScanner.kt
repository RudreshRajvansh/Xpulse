package com.rudresh.xpulse.core.scanner

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

object QrScanner {

    fun scan(
        context: Context,
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()

        GmsBarcodeScanning.getClient(context, options)
            .startScan()
            .addOnSuccessListener { barcode ->
                val value = barcode.rawValue
                if (value.isNullOrBlank()) {
                    onError("That code could not be read")
                } else {
                    onResult(value)
                }
            }
            .addOnCanceledListener { onError("Scan cancelled") }
            .addOnFailureListener { e -> onError(e.message ?: "Scanner unavailable") }
    }
}
