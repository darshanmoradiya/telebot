package com.example.telebot

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CameraHelper {

    private var lastCapturedFile: File? = null

    fun capturePhoto(context: Context): Uri? {
        return try {
            val photoFile = createImageFile(context)
            lastCapturedFile = photoFile

            val photoURI: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Start camera activity in background
            context.startActivity(cameraIntent)

            // Return Uri immediately (you may need to delay file access)
            photoURI
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    fun getFilePathFromUri(uri: Uri): File {
        // Fallback logic: return last captured file or attempt to use uri path (with validation)
        return lastCapturedFile?.takeIf { it.exists() }
            ?: File(uri.path ?: "/dev/null")
    }
}
