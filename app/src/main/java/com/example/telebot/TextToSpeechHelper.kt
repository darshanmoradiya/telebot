package com.example.telebot

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import java.util.*

object TextToSpeechHelper {

    private var tts: TextToSpeech? = null

    fun speak(context: Context, text: String) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    tts?.setSpeechRate(1.0f)
                    setMaxVolume(context)
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
                }
            }
        } else {
            setMaxVolume(context)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
        }
    }

    private fun setMaxVolume(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
    }
}
