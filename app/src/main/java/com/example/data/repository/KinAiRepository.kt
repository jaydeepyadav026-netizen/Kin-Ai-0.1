package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.automation.AndroidAutomationBridge
import com.example.data.automation.AutomationResult
import com.example.data.automation.BatteryStatus
import com.example.data.automation.NetworkStatus
import com.example.data.local.KinAiDatabase
import com.example.data.model.AssistantLanguage
import com.example.data.model.AssistantPersonality
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMode
import com.example.data.model.Conversation
import com.example.data.model.MemoryCategory
import com.example.data.model.MessageRole
import com.example.data.model.NeuralMemory
import com.example.data.model.TaskProgressInfo
import com.example.data.model.TaskStep
import com.example.data.remote.AiResponse
import com.example.data.remote.GeminiApiClient
import com.example.service.VoiceAssistantManager
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class KinAiRepository(
  private val context: Context,
  private val database: KinAiDatabase,
  val voiceManager: VoiceAssistantManager,
  val automationBridge: AndroidAutomationBridge,
  private val apiClient: GeminiApiClient
) {

  private val prefs: SharedPreferences = context.getSharedPreferences("kin_ai_settings", Context.MODE_PRIVATE)

  // Settings StateFlows
  private val _assistantName = MutableStateFlow(prefs.getString("assistant_name", "KIN AI") ?: "KIN AI")
  val assistantName: StateFlow<String> = _assistantName.asStateFlow()

  private val _userName = MutableStateFlow(prefs.getString("user_name", "Commander") ?: "Commander")
  val userName: StateFlow<String> = _userName.asStateFlow()

  private val _personality = MutableStateFlow(
    try {
      AssistantPersonality.valueOf(prefs.getString("personality", "JARVIS") ?: "JARVIS")
    } catch (e: Exception) {
      AssistantPersonality.JARVIS
    }
  )
  val personality: StateFlow<AssistantPersonality> = _personality.asStateFlow()

  private val _language = MutableStateFlow(
    try {
      AssistantLanguage.valueOf(prefs.getString("language", "ENGLISH") ?: "ENGLISH")
    } catch (e: Exception) {
      AssistantLanguage.ENGLISH
    }
  )
  val language: StateFlow<AssistantLanguage> = _language.asStateFlow()

  private val _themeMode = MutableStateFlow(
    try {
      AppThemeMode.valueOf(prefs.getString("theme_mode", "LIGHT") ?: "LIGHT")
    } catch (e: Exception) {
      AppThemeMode.LIGHT
    }
  )
  val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

  private val _geminiApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
  val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

  private val _selectedModel = MutableStateFlow(prefs.getString("selected_model", "gemini-3.5-flash") ?: "gemini-3.5-flash")
  val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

  private val _floatingOrbEnabled = MutableStateFlow(prefs.getBoolean("floating_orb_enabled", true))
  val floatingOrbEnabled: StateFlow<Boolean> = _floatingOrbEnabled.asStateFlow()

  private val _speechRate = MutableStateFlow(prefs.getFloat("speech_rate", 1.0f))
  val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

  private val _speechPitch = MutableStateFlow(prefs.getFloat("speech_pitch", 1.0f))
  val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

  // Real-time active tasks state
  private val _activeTask = MutableStateFlow<TaskProgressInfo?>(null)
  val activeTask: StateFlow<TaskProgressInfo?> = _activeTask.asStateFlow()

  init {
    voiceManager.setLanguage(_language.value)
    voiceManager.setPitch(_speechPitch.value)
    voiceManager.setRate(_speechRate.value)
  }

  // Database access
  val conversations: Flow<List<Conversation>> = database.conversationDao().getAllConversations()
  val memories: Flow<List<NeuralMemory>> = database.memoryDao().getAllMemories()

  fun getMessages(conversationId: String): Flow<List<ChatMessage>> {
    return database.messageDao().getMessagesForConversation(conversationId)
  }

  suspend fun createNewConversation(title: String = "New Conversation", mode: ChatMode = ChatMode.CHAT): String {
    val id = UUID.randomUUID().toString()
    val conv = Conversation(
      id = id,
      title = title,
      mode = mode,
      lastUpdated = System.currentTimeMillis()
    )
    database.conversationDao().insertConversation(conv)
    return id
  }

  suspend fun deleteConversation(id: String) {
    database.messageDao().deleteMessagesForConversation(id)
    database.conversationDao().deleteConversation(id)
  }

  suspend fun clearAllConversations() {
    database.messageDao().clearAll()
    database.conversationDao().clearAll()
  }

  suspend fun sendMessage(
    conversationId: String,
    prompt: String,
    mode: ChatMode,
    currentHistory: List<ChatMessage>,
    currentMemories: List<NeuralMemory>
  ): AiResponse {
    // Save user message
    val userMsg = ChatMessage(
      conversationId = conversationId,
      role = MessageRole.USER,
      content = prompt,
      mode = mode
    )
    database.messageDao().insertMessage(userMsg)

    // Update conversation metadata
    val snippet = if (prompt.length > 50) prompt.take(50) + "..." else prompt
    val existingConv = database.conversationDao().getConversationById(conversationId)
    val updatedConv = existingConv?.copy(
      snippet = snippet,
      lastUpdated = System.currentTimeMillis(),
      messageCount = existingConv.messageCount + 1
    ) ?: Conversation(
      id = conversationId,
      title = snippet,
      mode = mode,
      lastUpdated = System.currentTimeMillis(),
      messageCount = 1,
      snippet = snippet
    )
    database.conversationDao().insertConversation(updatedConv)

    // Call API
    val response = apiClient.generateResponse(
      prompt = prompt,
      history = currentHistory,
      mode = mode,
      personality = _personality.value,
      language = _language.value,
      memories = currentMemories,
      customApiKey = _geminiApiKey.value,
      selectedModel = _selectedModel.value
    )

    when (response) {
      is AiResponse.Success -> {
        val assistantMsg = ChatMessage(
          conversationId = conversationId,
          role = MessageRole.ASSISTANT,
          content = response.text,
          mode = mode,
          isError = false,
          modelUsed = response.model
        )
        database.messageDao().insertMessage(assistantMsg)
      }
      is AiResponse.Error -> {
        val errorMsg = ChatMessage(
          conversationId = conversationId,
          role = MessageRole.ASSISTANT,
          content = "⚠️ ${response.message}",
          mode = mode,
          isError = true,
          modelUsed = _selectedModel.value
        )
        database.messageDao().insertMessage(errorMsg)
      }
    }

    return response
  }

  suspend fun addMemory(key: String, value: String, category: MemoryCategory = MemoryCategory.PREFERENCE) {
    val memory = NeuralMemory(
      key = key.trim(),
      value = value.trim(),
      category = category
    )
    database.memoryDao().insertMemory(memory)
  }

  suspend fun deleteMemory(memory: NeuralMemory) {
    database.memoryDao().deleteMemory(memory)
  }

  suspend fun clearAllMemories() {
    database.memoryDao().clearAll()
  }

  fun setAssistantName(name: String) {
    _assistantName.value = name
    prefs.edit().putString("assistant_name", name).apply()
  }

  fun setUserName(name: String) {
    _userName.value = name
    prefs.edit().putString("user_name", name).apply()
  }

  fun setPersonality(p: AssistantPersonality) {
    _personality.value = p
    prefs.edit().putString("personality", p.name).apply()
  }

  fun setLanguage(lang: AssistantLanguage) {
    _language.value = lang
    prefs.edit().putString("language", lang.name).apply()
    voiceManager.setLanguage(lang)
  }

  fun setThemeMode(theme: AppThemeMode) {
    _themeMode.value = theme
    prefs.edit().putString("theme_mode", theme.name).apply()
  }

  fun setGeminiApiKey(key: String) {
    _geminiApiKey.value = key
    prefs.edit().putString("custom_api_key", key).apply()
  }

  fun setSelectedModel(model: String) {
    _selectedModel.value = model
    prefs.edit().putString("selected_model", model).apply()
  }

  fun setSpeechRate(rate: Float) {
    _speechRate.value = rate
    prefs.edit().putFloat("speech_rate", rate).apply()
    voiceManager.setRate(rate)
  }

  fun setSpeechPitch(pitch: Float) {
    _speechPitch.value = pitch
    prefs.edit().putFloat("speech_pitch", pitch).apply()
    voiceManager.setPitch(pitch)
  }

  fun setFloatingOrbEnabled(enabled: Boolean) {
    _floatingOrbEnabled.value = enabled
    prefs.edit().putBoolean("floating_orb_enabled", enabled).apply()
  }

  suspend fun testApiConnection(): AiResponse {
    return apiClient.testConnection(_geminiApiKey.value, _selectedModel.value)
  }

  fun getBatteryStatus(): BatteryStatus = automationBridge.getBatteryStatus()
  fun getNetworkStatus(): NetworkStatus = automationBridge.getNetworkStatus()

  // Trackable task runner
  fun runSampleTask(title: String, stepsCount: Int = 3, onStep: (Int) -> Unit) {
    val steps = (1..stepsCount).map { TaskStep("Step $it: Executing module", false) }
    _activeTask.value = TaskProgressInfo(
      taskId = UUID.randomUUID().toString(),
      title = title,
      currentOperation = "Initializing neural bridge...",
      steps = steps,
      status = "IN_PROGRESS"
    )
  }

  fun clearActiveTask() {
    _activeTask.value = null
  }
}
