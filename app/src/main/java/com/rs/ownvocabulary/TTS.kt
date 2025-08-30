package com.rs.ownvocabulary
import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object TTSManager {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val initializationListeners = mutableListOf<(Boolean) -> Unit>()

    fun initialize(context: Context, onInitListener: (Boolean) -> Unit = {}) {
        if (tts != null) {
            onInitListener(isInitialized)
            return
        }

        tts = TextToSpeech(context.applicationContext) { status ->
            isInitialized = status == TextToSpeech.SUCCESS
            if (isInitialized) {
                setLanguage(Locale.US)
            }
            onInitListener(isInitialized)
            initializationListeners.forEach { listener ->
                listener(isInitialized)
            }
            initializationListeners.clear()
        }
    }

    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (isInitialized && tts != null) {
            tts?.speak(text, queueMode, null, null)
        } else {
            // Queue the speech if not initialized yet
            initializationListeners.add { success ->
                if (success) {
                    tts?.speak(text, queueMode, null, null)
                }
            }
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun setLanguage(locale: Locale): Int {
        return if (isInitialized && tts != null) {
            tts?.setLanguage(locale) ?: TextToSpeech.LANG_MISSING_DATA
        } else {
            TextToSpeech.LANG_MISSING_DATA
        }
    }

    fun setSpeechRate(rate: Float) {
        if (isInitialized && tts != null) {
            tts?.setSpeechRate(rate)
        }
    }

    fun setPitch(pitch: Float) {
        if (isInitialized && tts != null) {
            tts?.setPitch(pitch)
        }
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking ?: false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    fun getStatus(): String {
        return if (isInitialized) "Initialized" else "Not Initialized"
    }

    fun speakOnlyEnglish(text: String){
        val closingIndex = text.indexOf("(")
        if (closingIndex != -1) {
             val textToSpeak = text.substring(0, closingIndex)
            speak(textToSpeak)
        } else {
            speak(text)
        }
    }
}