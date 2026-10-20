package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.automation.AutomationResult
import com.example.data.automation.BatteryStatus
import com.example.data.automation.NetworkStatus
import com.example.data.model.AssistantLanguage
import com.example.data.model.AssistantPersonality
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMode
import com.example.data.model.Conversation
import com.example.data.model.MemoryCategory
import com.example.data.model.NeuralMemory
import com.example.data.model.TaskProgressInfo
import com.example.data.remote.AiResponse
import com.example.data.repository.KinAiRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
  HOME,
  CHAT,
  VOICE,
  SETTINGS,
  UPDATES,
  MEMORY,
  SEARCH
}

class KinAiViewModel(private val repository: KinAiRepository) : ViewModel() {

  // Current screen
  private val _currentScreen = MutableStateFlow(Screen.HOME)
  val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

  // Selected conversation ID
  private val _currentConversationId = MutableStateFlow<String?>(null)
  val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

  // Current messages in active conversation
  private val _activeMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val activeMessages: StateFlow<List<ChatMessage>> = _activeMessages.asStateFlow()

  // Chat Mode: CHAT or CODE
  private val _activeChatMode = MutableStateFlow(ChatMode.CHAT)
  val activeChatMode: StateFlow<ChatMode> = _activeChatMode.asStateFlow()

  // Generating status
  private val _isGenerating = MutableStateFlow(false)
  val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

  // Status banner / Toast feedback
  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  // Settings & state delegations
  val assistantName = repository.assistantName
  val userName = repository.userName
  val personality = repository.personality
  val language = repository.language
  val themeMode = repository.themeMode
  val geminiApiKey = repository.geminiApiKey
  val selectedModel = repository.selectedModel
  val floatingOrbEnabled = repository.floatingOrbEnabled
  val speechRate = repository.speechRate
  val speechPitch = repository.speechPitch

  val conversations: StateFlow<List<Conversation>> = repository.conversations
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val memories: StateFlow<List<NeuralMemory>> = repository.memories
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val voiceManager = repository.voiceManager
  val isSpeaking = voiceManager.isSpeaking
  val isListening = voiceManager.isListening
  val isMuted = voiceManager.isMuted
  val recognizedText = voiceManager.recognizedText
  val speechError = voiceManager.speechError

  // Real-time telemetry
  private val _batteryStatus = MutableStateFlow(repository.getBatteryStatus())
  val batteryStatus: StateFlow<BatteryStatus> = _batteryStatus.asStateFlow()

  private val _networkStatus = MutableStateFlow(repository.getNetworkStatus())
  val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

  val activeTask = repository.activeTask

  private var activeMessageJob: Job? = null
  private var messageFlowJob: Job? = null

  init {
    // Refresh telemetry
    refreshTelemetry()
    // Observe conversations to pick default if none selected
    viewModelScope.launch {
      conversations.collect { list ->
        if (_currentConversationId.value == null && list.isNotEmpty()) {
          selectConversation(list.first().id)
        }
      }
    }
  }

  fun navigateTo(screen: Screen) {
    _currentScreen.value = screen
    if (screen == Screen.HOME || screen == Screen.SETTINGS || screen == Screen.UPDATES) {
      refreshTelemetry()
    }
  }

  fun refreshTelemetry() {
    _batteryStatus.value = repository.getBatteryStatus()
    _networkStatus.value = repository.getNetworkStatus()
  }

  fun setChatMode(mode: ChatMode) {
    _activeChatMode.value = mode
  }

  fun selectConversation(convId: String) {
    _currentConversationId.value = convId
    messageFlowJob?.cancel()
    messageFlowJob = viewModelScope.launch {
      repository.getMessages(convId).collect { msgs ->
        _activeMessages.value = msgs
      }
    }
  }

  fun createNewConversation(title: String = "KIN Session", mode: ChatMode = _activeChatMode.value) {
    viewModelScope.launch {
      val newId = repository.createNewConversation(title, mode)
      selectConversation(newId)
      _activeChatMode.value = mode
      _currentScreen.value = Screen.CHAT
    }
  }

  fun deleteConversation(id: String) {
    viewModelScope.launch {
      repository.deleteConversation(id)
      if (_currentConversationId.value == id) {
        _currentConversationId.value = null
        _activeMessages.value = emptyList()
      }
    }
  }

  fun clearAllConversations() {
    viewModelScope.launch {
      repository.clearAllConversations()
      _currentConversationId.value = null
      _activeMessages.value = emptyList()
      showFeedback("Conversation history cleared")
    }
  }

  fun sendMessage(text: String) {
    if (text.isBlank() || _isGenerating.value) return

    viewModelScope.launch {
      var convId = _currentConversationId.value
      if (convId == null) {
        val titleSnippet = if (text.length > 25) text.take(25) + "..." else text
        convId = repository.createNewConversation(titleSnippet, _activeChatMode.value)
        selectConversation(convId)
      }

      _isGenerating.value = true
      activeMessageJob = launch {
        val result = repository.sendMessage(
          conversationId = convId,
          prompt = text.trim(),
          mode = _activeChatMode.value,
          currentHistory = _activeMessages.value,
          currentMemories = memories.value
        )

        _isGenerating.value = false

        // Automatically speak response if on voice screen or if not muted
        if (result is AiResponse.Success && !isMuted.value && _currentScreen.value == Screen.VOICE) {
          voiceManager.speak(result.text)
        }
      }
    }
  }

  fun stopGeneration() {
    activeMessageJob?.cancel()
    _isGenerating.value = false
    voiceManager.stopSpeaking()
  }

  // Voice Screen handling
  fun toggleListening() {
    if (isListening.value) {
      voiceManager.stopListening()
    } else {
      voiceManager.stopSpeaking()
      voiceManager.startListening { transcribed ->
        if (transcribed.isNotBlank()) {
          sendMessage(transcribed)
        }
      }
    }
  }

  fun speakText(text: String) {
    voiceManager.speak(text)
  }

  fun stopSpeaking() {
    voiceManager.stopSpeaking()
  }

  fun toggleMute() {
    voiceManager.toggleMute()
  }

  // Automation Triggers
  fun executeAutomation(actionType: String, query: String = "") {
    val result: AutomationResult = when (actionType) {
      "youtube" -> repository.automationBridge.openYouTube(query.ifBlank { null })
      "maps" -> repository.automationBridge.openMaps(query.ifBlank { "Current Location" })
      "search" -> repository.automationBridge.openWebSearch(query.ifBlank { "AI News" })
      "camera" -> repository.automationBridge.openCamera()
      "calculator" -> repository.automationBridge.openCalculator()
      "alarms" -> repository.automationBridge.openAlarmOrTimer()
      "settings" -> repository.automationBridge.openDeviceSettings()
      "reminder" -> repository.automationBridge.createCalendarReminder("KIN AI Task", query.ifBlank { "Personal reminder created via KIN AI" })
      "call" -> repository.automationBridge.initiatePhoneCall(query)
      else -> AutomationResult.Failure("Unknown automation action")
    }

    when (result) {
      is AutomationResult.Success -> showFeedback(result.message)
      is AutomationResult.Failure -> showFeedback("Error: ${result.error}")
    }
  }

  // Neural Memory Operations
  fun addMemory(key: String, value: String, category: MemoryCategory = MemoryCategory.PREFERENCE) {
    viewModelScope.launch {
      repository.addMemory(key, value, category)
      showFeedback("Stored in Neural Memory: '$key'")
    }
  }

  fun deleteMemory(memory: NeuralMemory) {
    viewModelScope.launch {
      repository.deleteMemory(memory)
      showFeedback("Memory deleted")
    }
  }

  fun clearAllMemories() {
    viewModelScope.launch {
      repository.clearAllMemories()
      showFeedback("All memories erased")
    }
  }

  // Settings
  fun updateAssistantName(name: String) = repository.setAssistantName(name)
  fun updateUserName(name: String) = repository.setUserName(name)
  fun updatePersonality(p: AssistantPersonality) = repository.setPersonality(p)
  fun updateLanguage(lang: AssistantLanguage) = repository.setLanguage(lang)
  fun updateThemeMode(theme: AppThemeMode) = repository.setThemeMode(theme)
  fun updateGeminiApiKey(key: String) = repository.setGeminiApiKey(key)
  fun updateSelectedModel(model: String) = repository.setSelectedModel(model)
  fun updateSpeechRate(rate: Float) = repository.setSpeechRate(rate)
  fun updateSpeechPitch(pitch: Float) = repository.setSpeechPitch(pitch)
  fun updateFloatingOrb(enabled: Boolean) = repository.setFloatingOrbEnabled(enabled)

  fun testApiConnection(onResult: (String, Boolean) -> Unit) {
    viewModelScope.launch {
      val res = repository.testApiConnection()
      when (res) {
        is AiResponse.Success -> onResult("Success: ${res.text} (Latency: ${res.latencyMs}ms)", true)
        is AiResponse.Error -> onResult("Failed: ${res.message}", false)
      }
    }
  }

  fun startSampleTask(title: String) {
    viewModelScope.launch {
      repository.runSampleTask(title, stepsCount = 4) { step -> }
      // simulate realistic progress steps
      delay(800)
      refreshTelemetry()
    }
  }

  fun showFeedback(msg: String) {
    _statusMessage.value = msg
  }

  fun clearFeedback() {
    _statusMessage.value = null
  }

  override fun onCleared() {
    super.onCleared()
    voiceManager.release()
  }
}
