package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class MessageRole {
  USER,
  ASSISTANT,
  SYSTEM
}

enum class ChatMode {
  CHAT,
  CODE
}

@Entity(tableName = "messages")
data class ChatMessage(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val conversationId: String,
  val role: MessageRole,
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val mode: ChatMode = ChatMode.CHAT,
  val isError: Boolean = false,
  val modelUsed: String = "gemini-3.5-flash"
)

@Entity(tableName = "conversations")
data class Conversation(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val title: String,
  val mode: ChatMode = ChatMode.CHAT,
  val lastUpdated: Long = System.currentTimeMillis(),
  val messageCount: Int = 0,
  val snippet: String = ""
)

enum class MemoryCategory {
  PREFERENCE,
  FACT,
  USER_HABIT,
  SYSTEM
}

@Entity(tableName = "neural_memories")
data class NeuralMemory(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val key: String,
  val value: String,
  val category: MemoryCategory = MemoryCategory.PREFERENCE,
  val timestamp: Long = System.currentTimeMillis(),
  val isPinned: Boolean = false
)

enum class AssistantPersonality(val displayName: String, val promptInstruction: String) {
  JARVIS(
    "JARVIS AI",
    "You are KIN AI, an advanced JARVIS-inspired personal artificial intelligence assistant. Address the user with composed elegance, calm confidence, supreme intellect, and concise execution. Provide actionable solutions and assist with device tasks, computing, reasoning, and planning."
  ),
  PROFESSIONAL(
    "Professional",
    "You are KIN AI, a highly competent, formal, and professional executive AI assistant. Provide structured, precise, and thorough assistance."
  ),
  FRIENDLY(
    "Friendly & Warm",
    "You are KIN AI, a cheerful, warm, empathetic, and encouraging personal companion. Keep responses natural, conversational, and delightful."
  ),
  TECHNICAL(
    "Technical & Architect",
    "You are KIN AI, a senior software architect, engineer, and technical systems specialist. Prioritize clean code, architecture patterns, performance, and rigorous technical clarity."
  ),
  CONCISE(
    "Concise & Direct",
    "You are KIN AI. Give the shortest, clearest, most direct answer possible. No fluff, no introductory padding, pure signal."
  )
}

enum class AssistantLanguage(val displayName: String, val code: String) {
  ENGLISH("English (US)", "en-US"),
  HINDI("Hindi (हिंदी)", "hi-IN"),
  HINGLISH("Hinglish", "hi-Latn")
}

data class QuickActionItem(
  val id: String,
  val label: String,
  val subtitle: String,
  val category: String,
  val query: String
)

data class TaskStep(
  val title: String,
  val completed: Boolean = false,
  val failed: Boolean = false
)

data class TaskProgressInfo(
  val taskId: String,
  val title: String,
  val currentOperation: String,
  val steps: List<TaskStep>,
  val status: String, // "IN_PROGRESS", "COMPLETED", "FAILED"
  val canRetry: Boolean = false
)
