package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.AssistantLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceAssistantManager(private val context: Context) : TextToSpeech.OnInitListener {

  private var tts: TextToSpeech? = null
  private var speechRecognizer: SpeechRecognizer? = null

  private val _isTtsReady = MutableStateFlow(false)
  val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

  private val _isMuted = MutableStateFlow(false)
  val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

  private val _recognizedText = MutableStateFlow("")
  val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

  private val _speechError = MutableStateFlow<String?>(null)
  val speechError: StateFlow<String?> = _speechError.asStateFlow()

  private var currentLanguage = AssistantLanguage.ENGLISH
  private var currentPitch = 1.0f
  private var currentRate = 1.0f

  init {
    try {
      tts = TextToSpeech(context.applicationContext, this)
    } catch (e: Exception) {
      Log.e("VoiceAssistant", "Failed to initialize TTS: ${e.message}")
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      tts?.let { engine ->
        engine.language = Locale.US
        engine.setPitch(currentPitch)
        engine.setSpeechRate(currentRate)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
          override fun onStart(utteranceId: String?) {
            _isSpeaking.value = true
          }

          override fun onDone(utteranceId: String?) {
            _isSpeaking.value = false
          }

          @Deprecated("Deprecated in Java")
          override fun onError(utteranceId: String?) {
            _isSpeaking.value = false
          }
        })
        _isTtsReady.value = true
      }
    } else {
      Log.w("VoiceAssistant", "TTS initialization returned status: $status")
    }
  }

  fun setLanguage(language: AssistantLanguage) {
    currentLanguage = language
    val locale = when (language) {
      AssistantLanguage.ENGLISH -> Locale.US
      AssistantLanguage.HINDI -> Locale("hi", "IN")
      AssistantLanguage.HINGLISH -> Locale("hi", "IN")
    }
    tts?.let { engine ->
      val result = engine.setLanguage(locale)
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        engine.language = Locale.US
      }
    }
  }

  fun setPitch(pitch: Float) {
    currentPitch = pitch.coerceIn(0.5f, 2.0f)
    tts?.setPitch(currentPitch)
  }

  fun setRate(rate: Float) {
    currentRate = rate.coerceIn(0.5f, 2.0f)
    tts?.setSpeechRate(currentRate)
  }

  fun toggleMute() {
    _isMuted.value = !_isMuted.value
    if (_isMuted.value) {
      stopSpeaking()
    }
  }

  fun speak(text: String, onFinished: (() -> Unit)? = null) {
    if (_isMuted.value || text.isBlank()) return
    tts?.let { engine ->
      stopSpeaking()
      _isSpeaking.value = true
      val utteranceId = "kin_ai_${System.currentTimeMillis()}"
      engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }
  }

  fun stopSpeaking() {
    try {
      tts?.stop()
    } catch (e: Exception) {
      // ignore
    }
    _isSpeaking.value = false
  }

  fun startListening(onResult: (String) -> Unit) {
    _speechError.value = null
    _recognizedText.value = ""

    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      _speechError.value = "Speech recognition service is not available on this device."
      return
    }

    try {
      speechRecognizer?.destroy()
      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
        setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
          }

          override fun onBeginningOfSpeech() {}
          override fun onRmsChanged(rmsdB: Float) {}
          override fun onBufferReceived(buffer: ByteArray?) {}
          override fun onEndOfSpeech() {
            _isListening.value = false
          }

          override fun onError(error: Int) {
            _isListening.value = false
            val message = when (error) {
              SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
              SpeechRecognizer.ERROR_CLIENT -> "Client error in recognition"
              SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
              SpeechRecognizer.ERROR_NETWORK -> "Network connection error for speech"
              SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out"
              SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking again."
              SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy"
              SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
              SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
              else -> "Speech recognition error ($error)"
            }
            _speechError.value = message
          }

          override fun onResults(results: Bundle?) {
            _isListening.value = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognized = matches?.firstOrNull() ?: ""
            _recognizedText.value = recognized
            if (recognized.isNotBlank()) {
              onResult(recognized)
            }
          }

          override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            partial?.firstOrNull()?.let {
              _recognizedText.value = it
            }
          }

          override fun onEvent(eventType: Int, params: Bundle?) {}
        })
      }

      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        val lang = when (currentLanguage) {
          AssistantLanguage.ENGLISH -> "en-US"
          AssistantLanguage.HINDI, AssistantLanguage.HINGLISH -> "hi-IN"
        }
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
      }

      speechRecognizer?.startListening(intent)
      _isListening.value = true
    } catch (e: Exception) {
      _isListening.value = false
      _speechError.value = "Failed to start listening: ${e.message}"
    }
  }

  fun stopListening() {
    try {
      speechRecognizer?.stopListening()
    } catch (e: Exception) {
      // ignore
    }
    _isListening.value = false
  }

  fun release() {
    try {
      tts?.stop()
      tts?.shutdown()
      speechRecognizer?.destroy()
    } catch (e: Exception) {
      // ignore
    }
  }
}
