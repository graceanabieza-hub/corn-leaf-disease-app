package com.example.cornleafdisease.data.local

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object ScanImageStorage {

    fun saveImage(
        context: Context,
        sourceUri: Uri
    ): String? {
        return try {
            val directory = File(
                context.filesDir,
                "scan_images"
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val destination = File(
                directory,
                "${UUID.randomUUID()}.jpg"
            )

            context.contentResolver.openInputStream(
                sourceUri
            )?.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            Uri.fromFile(destination).toString()

        } catch (e: Exception) {
            null
        }
    }
}