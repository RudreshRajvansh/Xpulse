package com.rudresh.xpulse.core.data.remote

import android.content.Context
import android.net.Uri
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun readAsBase64(uriString: String): String = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(Uri.parse(uriString))?.use { it.readBytes() }
            ?: throw IOException("Could not open that file")
        if (bytes.size > MAX_BYTES) {
            throw IOException("File is larger than the 8 MB limit")
        }
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private companion object {
        const val MAX_BYTES = 8 * 1024 * 1024
    }
}
