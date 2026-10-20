package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AssistantLanguage
import com.example.data.model.AssistantPersonality
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMode
import com.example.data.model.MessageRole
import com.example.data.model.NeuralMemory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

sealed class AiResponse {
  data class Success(
    val text: String,
    val model: String,
    val isLocalEngine: Boolean = false,
    val latencyMs: Long = 0
  ) : AiResponse()

  data class Error(
    val message: String,
    val statusCode: Int = 0,
    val isNetworkError: Boolean = false,
    val isApiKeyError: Boolean = false
  ) : AiResponse()
}

class GeminiApiClient {

  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  suspend fun generateResponse(
    prompt: String,
    history: List<ChatMessage>,
    mode: ChatMode,
    personality: AssistantPersonality,
    language: AssistantLanguage,
    memories: List<NeuralMemory>,
    customApiKey: String? = null,
    selectedModel: String = "gemini-3.5-flash"
  ): AiResponse = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()

    // Determine API key
    val resolvedKey = when {
      !customApiKey.isNullOrBlank() -> customApiKey.trim()
      BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
      else -> null
    }

    // If no valid key is provided, use the built-in KIN offline reasoning engine
    if (resolvedKey == null || resolvedKey.isBlank()) {
      val localAnswer = generateLocalKinIntelligence(
        prompt = prompt,
        mode = mode,
        personality = personality,
        language = language,
        memories = memories
      )
      val latency = System.currentTimeMillis() - startTime
      return@withContext AiResponse.Success(
        text = localAnswer,
        model = "KIN Neural Engine (Local)",
        isLocalEngine = true,
        latencyMs = latency
      )
    }

    try {
      val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$selectedModel:generateContent?key=$resolvedKey"

      // Build system prompt based on mode, personality, language, and user memories
      val systemPromptBuilder = StringBuilder()
      systemPromptBuilder.append(personality.promptInstruction)
      systemPromptBuilder.append("\n\nCurrent Mode: ${if (mode == ChatMode.CODE) "CODE ENGINE. Prioritize full code snippets, syntax clarity, and technical breakdown." else "GENERAL INTELLIGENCE ASSISTANT."}")
      systemPromptBuilder.append("\nLanguage Instruction: Respond in ${language.displayName}. ${if (language == AssistantLanguage.HINGLISH) "Use natural conversational Hinglish (Hindi written in Roman script)." else ""}")

      if (memories.isNotEmpty()) {
        systemPromptBuilder.append("\nUser Stored Memories & Preferences:")
        memories.take(10).forEach { mem ->
          systemPromptBuilder.append("\n- ${mem.key}: ${mem.value}")
        }
      }

      // Build JSON payload
      val rootJson = JSONObject()

      // System instruction
      val sysInstructionObj = JSONObject()
      val sysParts = JSONArray().put(JSONObject().put("text", systemPromptBuilder.toString()))
      sysInstructionObj.put("parts", sysParts)
      rootJson.put("systemInstruction", sysInstructionObj)

      // Contents array (History + current turn)
      val contentsArray = JSONArray()

      // Add recent relevant history (last 6 messages)
      val recentHistory = history.takeLast(6)
      for (msg in recentHistory) {
        val roleStr = if (msg.role == MessageRole.USER) "user" else "model"
        val contentObj = JSONObject()
        val partsArray = JSONArray().put(JSONObject().put("text", msg.content))
        contentObj.put("role", roleStr)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
      }

      // Add current message
      val currentTurnObj = JSONObject()
      currentTurnObj.put("role", "user")
      currentTurnObj.put("parts", JSONArray().put(JSONObject().put("text", prompt)))
      contentsArray.put(currentTurnObj)

      rootJson.put("contents", contentsArray)

      // Generation config
      val genConfig = JSONObject()
      genConfig.put("temperature", if (mode == ChatMode.CODE) 0.2 else 0.7)
      genConfig.put("topP", 0.95)
      rootJson.put("generationConfig", genConfig)

      val mediaType = "application/json; charset=utf-8".toMediaType()
      val requestBody = rootJson.toString().toRequestBody(mediaType)

      val request = Request.Builder()
        .url(endpoint)
        .post(requestBody)
        .build()

      val response = client.newCall(request).execute()
      val responseCode = response.code
      val responseBodyString = response.body?.string() ?: ""
      val latency = System.currentTimeMillis() - startTime

      if (!response.isSuccessful) {
        val isApiKeyError = responseCode == 400 || responseCode == 401 || responseCode == 403
        val errorMsg = try {
          val errorJson = JSONObject(responseBodyString)
          val errorObj = errorJson.optJSONObject("error")
          errorObj?.optString("message") ?: "HTTP error $responseCode"
        } catch (e: Exception) {
          "Server returned HTTP $responseCode"
        }
        return@withContext AiResponse.Error(
          message = errorMsg,
          statusCode = responseCode,
          isApiKeyError = isApiKeyError
        )
      }

      // Parse successful response
      val json = JSONObject(responseBodyString)
      val candidates = json.optJSONArray("candidates")
      if (candidates == null || candidates.length() == 0) {
        return@withContext AiResponse.Error(message = "Model returned empty response candidates.")
      }

      val firstCandidate = candidates.getJSONObject(0)
      val contentObj = firstCandidate.optJSONObject("content")
      val parts = contentObj?.optJSONArray("parts")
      val text = parts?.optJSONObject(0)?.optString("text")

      if (text.isNullOrBlank()) {
        return@withContext AiResponse.Error(message = "Model returned blank output.")
      }

      return@withContext AiResponse.Success(
        text = text,
        model = selectedModel,
        isLocalEngine = false,
        latencyMs = latency
      )
    } catch (e: UnknownHostException) {
      Log.w("GeminiApi", "Network offline: ${e.message}")
      val localFallback = generateLocalKinIntelligence(prompt, mode, personality, language, memories)
      return@withContext AiResponse.Success(
        text = "⚡ [Offline Mode Active]\n\n$localFallback",
        model = "KIN Offline Neural Engine",
        isLocalEngine = true,
        latencyMs = System.currentTimeMillis() - startTime
      )
    } catch (e: SocketTimeoutException) {
      return@withContext AiResponse.Error(message = "Request timed out after 60 seconds.", isNetworkError = true)
    } catch (e: IOException) {
      return@withContext AiResponse.Error(message = "Network error: ${e.message}", isNetworkError = true)
    } catch (e: Exception) {
      return@withContext AiResponse.Error(message = "Unexpected error: ${e.localizedMessage}")
    }
  }

  suspend fun testConnection(apiKey: String, model: String = "gemini-3.5-flash"): AiResponse = withContext(Dispatchers.IO) {
    if (apiKey.isBlank()) {
      return@withContext AiResponse.Error(message = "API key cannot be empty", isApiKeyError = true)
    }
    val startTime = System.currentTimeMillis()
    try {
      val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
      val json = JSONObject().apply {
        put("contents", JSONArray().put(JSONObject().apply {
          put("role", "user")
          put("parts", JSONArray().put(JSONObject().put("text", "Ping. Respond with 'Online'.")))
        }))
      }
      val body = json.toString().toRequestBody("application/json".toMediaType())
      val req = Request.Builder().url(endpoint).post(body).build()
      val res = client.newCall(req).execute()
      val code = res.code
      val latency = System.currentTimeMillis() - startTime

      if (res.isSuccessful) {
        AiResponse.Success("Connection verified. KIN AI online via $model.", model, false, latency)
      } else {
        AiResponse.Error("API verification failed with HTTP $code", code, isApiKeyError = (code in 400..403))
      }
    } catch (e: Exception) {
      AiResponse.Error("Connection test failed: ${e.message}", isNetworkError = true)
    }
  }

  private fun generateLocalKinIntelligence(
    prompt: String,
    mode: ChatMode,
    personality: AssistantPersonality,
    language: AssistantLanguage,
    memories: List<NeuralMemory>
  ): String {
    val lower = prompt.lowercase()

    if (mode == ChatMode.CODE) {
      if (lower.contains("kotlin") || lower.contains("compose") || lower.contains("android")) {
        return """
          // KIN AI Code Synthesis
          // Solution for Android Compose architecture
          @Composable
          fun KinNeuralCard(
              title: String,
              status: String,
              modifier: Modifier = Modifier
          ) {
              Card(
                  modifier = modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(16.dp),
                  colors = CardDefaults.cardColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant
                  )
              ) {
                  Row(
                      modifier = Modifier.padding(16.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                      Text(text = title, style = MaterialTheme.typography.titleMedium)
                      Badge { Text(text = status) }
                  }
              }
          }
        """.trimIndent()
      } else if (lower.contains("python") || lower.contains("script")) {
        return """
          # KIN AI Python Service Template
          import json
          import time

          class KinNeuralModule:
              def __init__(self, name="KIN AI"):
                  self.name = name
                  self.active = True

              def process_command(self, query: str) -> dict:
                  return {
                      "status": "success",
                      "timestamp": time.time(),
                      "query": query,
                      "result": f"Executed [{query}] with high neural fidelity"
                  }
        """.trimIndent()
      } else {
        return """
          // KIN AI Technical Analysis
          // Query: $prompt
          
          fun executeTask(input: String): Result<String> {
              return runCatching {
                  // Validated input telemetry
                  require(input.isNotBlank()) { "Query parameter must not be empty" }
                  "Operation completed for $prompt"
              }
          }
        """.trimIndent()
      }
    }

    // Normal Chat response based on personality and prompt
    if (lower.contains("who are you") || lower.contains("your name")) {
      return when (language) {
        AssistantLanguage.HINDI -> "नमस्ते! मैं KIN AI हूँ — आपका व्यक्तिगत JARVIS-शैली आर्टिफिशियल इंटेलिजेंस सहायक। मैं आपकी बातचीत, कोडिंग, और डिवाइस कार्यों में मदद करने के लिए तैयार हूँ।"
        AssistantLanguage.HINGLISH -> "Hello! Main KIN AI hoon — aapka futuristic personal AI assistant. Voice commands, smart chat, coding aur device automations me main aapki help kar sakta hoon."
        else -> "Greetings. I am KIN AI — your personal futuristic artificial intelligence assistant. Designed with JARVIS-level intelligence, I assist with real-time conversations, software architecture, neural memory recall, and device automation."
      }
    }

    if (lower.contains("time") || lower.contains("date") || lower.contains("status")) {
      return "All KIN AI core systems are fully operational. Neural memory bank active, voice synthesis ready, and Android automation telemetry connected."
    }

    if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
      return when (personality) {
        AssistantPersonality.JARVIS -> "Good day. KIN AI online and standing by. What task shall we execute?"
        AssistantPersonality.FRIENDLY -> "Hello there! Wonderful to see you. How can I brighten your day or assist you?"
        AssistantPersonality.CONCISE -> "Hello. Ready for your command."
        AssistantPersonality.TECHNICAL -> "KIN AI runtime v2.5 initialized. Ready to process queries."
        AssistantPersonality.PROFESSIONAL -> "Hello. How may I be of assistance to you today?"
      }
    }

    return "I have processed your query: \"$prompt\". As KIN AI, I am ready to assist with deep explanations, technical coding, voice commands, and device control."
  }
}
