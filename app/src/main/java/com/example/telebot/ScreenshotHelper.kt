package com.example.telebot

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.util.DisplayMetrics
import android.view.WindowManager
import java.io.File
import java.io.FileOutputStream

object ScreenshotHelper {

    fun captureScreenshot(context: Context): File? {
        var imageReader: ImageReader? = null
        var mediaProjection: MediaProjection? = null
        var virtualDisplay: VirtualDisplay? = null

        return try {
            // 1. Get screen metrics
            val metrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getRealMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val density = metrics.densityDpi

            // 2. Setup ImageReader
            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)

            // 3. Get MediaProjection
            val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val projectionData = MainActivity.mediaProjectionData ?: return null
            val resultCode = MainActivity.mediaProjectionResultCode
            mediaProjection = projectionManager.getMediaProjection(resultCode, projectionData)

            // 4. Create virtual display
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface, null, null
            )

            // 5. Wait for the frame to arrive
            Thread.sleep(500) // Slight delay, not too long

            val image = imageReader.acquireLatestImage() ?: return null

            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width

            val bitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride,
                height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            image.close()

            // 6. Save bitmap to file
            val screenshotsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Screenshots")
            if (!screenshotsDir.exists()) screenshotsDir.mkdirs()

            val file = File(screenshotsDir, "screenshot_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            file

        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            virtualDisplay?.release()
            mediaProjection?.stop()
            imageReader?.close()
        }
    }
}
