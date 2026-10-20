package com.example

import com.example.data.local.Converters
import com.example.data.model.AssistantLanguage
import com.example.data.model.AssistantPersonality
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMode
import com.example.data.model.MemoryCategory
import com.example.data.model.MessageRole
import com.example.data.model.NeuralMemory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testChatMessageCreation() {
    val message = ChatMessage(
      conversationId = "conv-123",
      role = MessageRole.USER,
      content = "Activate KIN AI JARVIS protocol",
      mode = ChatMode.CHAT
    )
    assertEquals("conv-123", message.conversationId)
    assertEquals(MessageRole.USER, message.role)
    assertEquals(ChatMode.CHAT, message.mode)
    assertTrue(message.content.contains("JARVIS"))
  }

  @Test
  fun testPersonalityInstructions() {
    val jarvis = AssistantPersonality.JARVIS
    assertTrue(jarvis.promptInstruction.contains("JARVIS"))
    assertEquals("JARVIS AI", jarvis.displayName)

    val technical = AssistantPersonality.TECHNICAL
    assertTrue(technical.promptInstruction.contains("senior software architect"))
  }

  @Test
  fun testNeuralMemoryModel() {
    val memory = NeuralMemory(
      key = "Primary Stack",
      value = "Kotlin + Jetpack Compose",
      category = MemoryCategory.PREFERENCE,
      isPinned = true
    )
    assertEquals("Primary Stack", memory.key)
    assertEquals("Kotlin + Jetpack Compose", memory.value)
    assertTrue(memory.isPinned)
  }

  @Test
  fun testTypeConverters() {
    val converters = Converters()
    assertEquals(MessageRole.ASSISTANT, converters.toMessageRole("ASSISTANT"))
    assertEquals("CODE", converters.fromChatMode(ChatMode.CODE))
    assertEquals(MemoryCategory.FACT, converters.toMemoryCategory("FACT"))
  }

  @Test
  fun testLanguagesSupport() {
    val languages = AssistantLanguage.values()
    assertTrue(languages.any { it.name == "ENGLISH" })
    assertTrue(languages.any { it.name == "HINDI" })
    assertTrue(languages.any { it.name == "HINGLISH" })
  }
}
