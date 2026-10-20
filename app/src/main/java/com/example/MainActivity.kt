package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.KinAiViewModel
import com.example.ui.Screen
import com.example.ui.components.DraggableFloatingOrb
import com.example.ui.components.KinAiBottomNavBar
import com.example.ui.components.KinAiTopBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UpdatesScreen
import com.example.ui.screens.VoiceScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val app = application as KinAiApplication
    val factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return KinAiViewModel(app.repository) as T
      }
    }

    setContent {
      val viewModel: KinAiViewModel = viewModel(factory = factory)
      val themeMode by viewModel.themeMode.collectAsState()

      MyApplicationTheme(themeMode = themeMode) {
        KinAiApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun KinAiApp(viewModel: KinAiViewModel) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val currentScreen by viewModel.currentScreen.collectAsState()
  val assistantName by viewModel.assistantName.collectAsState()
  val userName by viewModel.userName.collectAsState()
  val personality by viewModel.personality.collectAsState()
  val language by viewModel.language.collectAsState()
  val themeMode by viewModel.themeMode.collectAsState()
  val geminiApiKey by viewModel.geminiApiKey.collectAsState()
  val selectedModel by viewModel.selectedModel.collectAsState()
  val floatingOrbEnabled by viewModel.floatingOrbEnabled.collectAsState()

  val batteryStatus by viewModel.batteryStatus.collectAsState()
  val networkStatus by viewModel.networkStatus.collectAsState()

  val conversations by viewModel.conversations.collectAsState()
  val activeMessages by viewModel.activeMessages.collectAsState()
  val activeChatMode by viewModel.activeChatMode.collectAsState()
  val isGenerating by viewModel.isGenerating.collectAsState()

  val isListening by viewModel.isListening.collectAsState()
  val isSpeaking by viewModel.isSpeaking.collectAsState()
  val isMuted by viewModel.isMuted.collectAsState()
  val recognizedText by viewModel.recognizedText.collectAsState()
  val speechError by viewModel.speechError.collectAsState()
  val speechRate by viewModel.speechRate.collectAsState()
  val speechPitch by viewModel.speechPitch.collectAsState()

  val memories by viewModel.memories.collectAsState()
  val activeTask by viewModel.activeTask.collectAsState()
  val statusMessage by viewModel.statusMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }

  // Handle back button smoothly
  BackHandler(enabled = currentScreen != Screen.HOME) {
    viewModel.navigateTo(Screen.HOME)
  }

  // Display status feedback toast/snackbar
  LaunchedEffect(statusMessage) {
    statusMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearFeedback()
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      if (currentScreen != Screen.MEMORY && currentScreen != Screen.SEARCH) {
        KinAiTopBar(
          assistantName = assistantName,
          batteryStatus = batteryStatus,
          networkStatus = networkStatus,
          hasApiKey = geminiApiKey.isNotBlank(),
          onNavigateToMemory = { viewModel.navigateTo(Screen.MEMORY) },
          onNavigateToSearch = { viewModel.navigateTo(Screen.SEARCH) }
        )
      }
    },
    bottomBar = {
      if (currentScreen != Screen.MEMORY && currentScreen != Screen.SEARCH) {
        KinAiBottomNavBar(
          currentScreen = currentScreen,
          onScreenSelected = { screen -> viewModel.navigateTo(screen) }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition"
      ) { screen ->
        when (screen) {
          Screen.HOME -> {
            HomeScreen(
              userName = userName,
              assistantName = assistantName,
              personalityName = personality.displayName,
              batteryStatus = batteryStatus,
              networkStatus = networkStatus,
              conversations = conversations,
              activeTask = activeTask,
              onNavigate = { viewModel.navigateTo(it) },
              onStartNewChat = { mode -> viewModel.createNewConversation(mode = mode) },
              onSelectConversation = { convId -> viewModel.selectConversation(convId) },
              onExecuteAutomation = { action, query -> viewModel.executeAutomation(action, query) },
              onStartSampleTask = { viewModel.startSampleTask("Neural Subsystem Diagnostics") }
            )
          }

          Screen.CHAT -> {
            ChatScreen(
              activeMode = activeChatMode,
              messages = activeMessages,
              isGenerating = isGenerating,
              onModeChanged = { mode -> viewModel.setChatMode(mode) },
              onSendMessage = { text -> viewModel.sendMessage(text) },
              onStopGeneration = { viewModel.stopGeneration() },
              onStartNewChat = { mode -> viewModel.createNewConversation(mode = mode) },
              onClearHistory = { viewModel.clearAllConversations() },
              onSpeakMessage = { text -> viewModel.speakText(text) }
            )
          }

          Screen.VOICE -> {
            VoiceScreen(
              isListening = isListening,
              isSpeaking = isSpeaking,
              isMuted = isMuted,
              recognizedText = recognizedText,
              speechError = speechError,
              recentMessages = activeMessages,
              currentLanguage = language,
              speechRate = speechRate,
              speechPitch = speechPitch,
              onToggleListening = { viewModel.toggleListening() },
              onSpeakText = { text -> viewModel.speakText(text) },
              onStopSpeaking = { viewModel.stopSpeaking() },
              onToggleMute = { viewModel.toggleMute() },
              onLanguageSelected = { lang -> viewModel.updateLanguage(lang) },
              onSpeechRateChange = { rate -> viewModel.updateSpeechRate(rate) },
              onSpeechPitchChange = { pitch -> viewModel.updateSpeechPitch(pitch) }
            )
          }

          Screen.SETTINGS -> {
            SettingsScreen(
              assistantName = assistantName,
              userName = userName,
              personality = personality,
              language = language,
              themeMode = themeMode,
              geminiApiKey = geminiApiKey,
              selectedModel = selectedModel,
              floatingOrbEnabled = floatingOrbEnabled,
              batteryStatus = batteryStatus,
              networkStatus = networkStatus,
              onUpdateAssistantName = { viewModel.updateAssistantName(it) },
              onUpdateUserName = { viewModel.updateUserName(it) },
              onUpdatePersonality = { viewModel.updatePersonality(it) },
              onUpdateLanguage = { viewModel.updateLanguage(it) },
              onUpdateThemeMode = { viewModel.updateThemeMode(it) },
              onUpdateGeminiApiKey = { viewModel.updateGeminiApiKey(it) },
              onUpdateSelectedModel = { viewModel.updateSelectedModel(it) },
              onUpdateFloatingOrb = { viewModel.updateFloatingOrb(it) },
              onTestConnection = { callback -> viewModel.testApiConnection(callback) },
              onClearAllData = {
                viewModel.clearAllConversations()
                viewModel.clearAllMemories()
              },
              onExportData = {
                val sb = StringBuilder()
                sb.append("=== KIN AI Session Export ===\n")
                sb.append("Assistant: $assistantName | User: $userName\n\n")
                conversations.forEach { conv ->
                  sb.append("-- Session: ${conv.title} (${conv.mode}) --\n")
                }
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("KIN AI Export", sb.toString()))
                Toast.makeText(context, "Conversations exported to clipboard", Toast.LENGTH_SHORT).show()
              },
              onNavigateToMemory = { viewModel.navigateTo(Screen.MEMORY) }
            )
          }

          Screen.UPDATES -> {
            UpdatesScreen(
              assistantName = assistantName,
              batteryStatus = batteryStatus,
              networkStatus = networkStatus,
              isTtsReady = true,
              hasApiKey = geminiApiKey.isNotBlank(),
              onRefreshTelemetry = { viewModel.refreshTelemetry() }
            )
          }

          Screen.MEMORY -> {
            MemoryScreen(
              memories = memories,
              onAddMemory = { key, value, cat -> viewModel.addMemory(key, value, cat) },
              onDeleteMemory = { mem -> viewModel.deleteMemory(mem) },
              onClearAll = { viewModel.clearAllMemories() },
              onBack = { viewModel.navigateTo(Screen.HOME) }
            )
          }

          Screen.SEARCH -> {
            SearchScreen(
              networkStatus = networkStatus,
              onPerformSearch = { query, provider ->
                viewModel.executeAutomation("search", query)
              },
              onBack = { viewModel.navigateTo(Screen.HOME) }
            )
          }
        }
      }

      // Optional Floating KIN AI Avatar Orb Overlay
      if (floatingOrbEnabled && currentScreen != Screen.VOICE) {
        DraggableFloatingOrb(
          visible = true,
          onOrbClick = { viewModel.navigateTo(Screen.VOICE) }
        )
      }
    }
  }
}
