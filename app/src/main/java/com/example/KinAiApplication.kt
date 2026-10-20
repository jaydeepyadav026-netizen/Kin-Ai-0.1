package com.example

import android.app.Application
import com.example.data.automation.AndroidAutomationBridge
import com.example.data.local.KinAiDatabase
import com.example.data.remote.GeminiApiClient
import com.example.data.repository.KinAiRepository
import com.example.service.VoiceAssistantManager

class KinAiApplication : Application() {

  lateinit var repository: KinAiRepository
    private set

  override fun onCreate() {
    super.onCreate()
    val database = KinAiDatabase.getInstance(this)
    val voiceManager = VoiceAssistantManager(this)
    val automationBridge = AndroidAutomationBridge(this)
    val apiClient = GeminiApiClient()

    repository = KinAiRepository(
      context = this,
      database = database,
      voiceManager = voiceManager,
      automationBridge = automationBridge,
      apiClient = apiClient
    )
  }
}
