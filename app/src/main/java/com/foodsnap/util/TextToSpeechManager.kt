package com.foodsnap.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Manager class for Text-to-Speech functionality.
 * Handles TTS initialization, speaking, and state management.
 */
class TextToSpeechManager(context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(-1)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                isInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        utteranceId?.toIntOrNull()?.let { index ->
                            _currentStepIndex.value = index
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    /**
     * Speaks the given text.
     * @param text The text to speak
     * @param utteranceId Optional ID to track this utterance
     */
    fun speak(text: String, utteranceId: String = "tts") {
        if (isInitialized) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    /**
     * Speaks a list of steps sequentially.
     * @param steps List of instruction steps
     * @param startIndex Index to start from
     */
    fun speakSteps(steps: List<String>, startIndex: Int = 0) {
        if (!isInitialized || steps.isEmpty()) return

        stop()

        steps.forEachIndexed { index, step ->
            if (index >= startIndex) {
                val prefix = "Step ${index + 1}. "
                val mode = if (index == startIndex) {
                    TextToSpeech.QUEUE_FLUSH
                } else {
                    TextToSpeech.QUEUE_ADD
                }
                textToSpeech?.speak(prefix + step, mode, null, index.toString())
            }
        }
    }

    /**
     * Speaks a single step.
     * @param step The step text
     * @param stepNumber The step number (1-indexed)
     */
    fun speakStep(step: String, stepNumber: Int) {
        if (isInitialized) {
            val text = "Step $stepNumber. $step"
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, (stepNumber - 1).toString())
        }
    }

    /**
     * Stops any ongoing speech.
     */
    fun stop() {
        textToSpeech?.stop()
        _isSpeaking.value = false
        _currentStepIndex.value = -1
    }

    /**
     * Releases TTS resources. Call when done using TTS.
     */
    fun shutdown() {
        stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}
