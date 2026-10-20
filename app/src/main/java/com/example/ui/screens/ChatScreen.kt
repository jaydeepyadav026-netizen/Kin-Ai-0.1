package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMode
import com.example.data.model.MessageRole
import com.example.ui.components.GlassCard
import com.example.ui.theme.CodeBlockBg
import com.example.ui.theme.PeachCoral
import com.example.ui.theme.PeachGlow
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.SunsetOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
  activeMode: ChatMode,
  messages: List<ChatMessage>,
  isGenerating: Boolean,
  onModeChanged: (ChatMode) -> Unit,
  onSendMessage: (String) -> Unit,
  onStopGeneration: () -> Unit,
  onStartNewChat: (ChatMode) -> Unit,
  onClearHistory: () -> Unit,
  onSpeakMessage: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var inputText by remember { mutableStateOf("") }
  var searchQuery by remember { mutableStateOf("") }
  var isSearchVisible by remember { mutableStateOf(false) }
  var showClearConfirmDialog by remember { mutableStateOf(false) }

  val listState = rememberLazyListState()

  // Auto-scroll on new message
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  val filteredMessages = if (searchQuery.isBlank()) {
    messages
  } else {
    messages.filter { it.content.contains(searchQuery, ignoreCase = true) }
  }

  // Clear confirmation dialog
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = { Text("Clear Conversation?") },
      text = { Text("This will permanently remove all messages in the current conversation.") },
      confirmButton = {
        Button(
          onClick = {
            showClearConfirmDialog = false
            onClearHistory()
          },
          colors = ButtonDefaults.buttonColors(containerColor = PeachCoral)
        ) {
          Text("Clear All")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    // Top Bar: Dual Mode Switcher & Tools
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        FilterChip(
          selected = activeMode == ChatMode.CHAT,
          onClick = { onModeChanged(ChatMode.CHAT) },
          label = { Text("KIN AI CHAT", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
          leadingIcon = { Icon(Icons.Filled.Forum, null, modifier = Modifier.size(14.dp)) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PeachCoral,
            selectedLabelColor = Color.White
          ),
          modifier = Modifier.testTag("chat_mode_chat_chip")
        )

        FilterChip(
          selected = activeMode == ChatMode.CODE,
          onClick = { onModeChanged(ChatMode.CODE) },
          label = { Text("KIN AI CODE", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
          leadingIcon = { Icon(Icons.Filled.Code, null, modifier = Modifier.size(14.dp)) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SunsetOrange,
            selectedLabelColor = Color.White
          ),
          modifier = Modifier.testTag("chat_mode_code_chip")
        )
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { isSearchVisible = !isSearchVisible },
          modifier = Modifier.size(34.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search messages",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        IconButton(
          onClick = { onStartNewChat(activeMode) },
          modifier = Modifier.size(34.dp),
          colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
            contentColor = PeachCoral
          )
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "New Chat",
            modifier = Modifier.size(20.dp)
          )
        }

        IconButton(
          onClick = { showClearConfirmDialog = true },
          modifier = Modifier.size(34.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.DeleteOutline,
            contentDescription = "Clear Conversation",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // Search bar (collapsible)
    AnimatedVisibility(visible = isSearchVisible) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp),
        placeholder = { Text("Filter conversation messages...", fontSize = 13.sp) },
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )
    }

    // Message List Area
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      if (filteredMessages.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
          ) {
            Box(
              modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(PeachCoral.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (activeMode == ChatMode.CODE) Icons.Filled.Code else Icons.Filled.Forum,
                contentDescription = null,
                tint = PeachCoral,
                modifier = Modifier.size(32.dp)
              )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = if (activeMode == ChatMode.CODE) "KIN AI CODE ENGINE" else "KIN AI CHAT SESSION",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (activeMode == ChatMode.CODE)
                "Ask architecture questions, request Kotlin/Compose solutions, or debug algorithms."
              else
                "Ask anything, brainstorm plans, or request personal assistance.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      } else {
        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          items(filteredMessages, key = { it.id }) { msg ->
            ChatMessageBubble(
              message = msg,
              onCopy = { text ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("KIN AI Message", text))
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
              },
              onRetry = {
                onSendMessage(msg.content)
              },
              onSpeak = {
                onSpeakMessage(msg.content)
              }
            )
          }
        }
      }
    }

    // Input Bar Area
    GlassCard(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 100.dp),
      shape = RoundedCornerShape(24.dp),
      elevation = 4.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = {
            Text(
              text = if (activeMode == ChatMode.CODE) "Ask a coding question or paste snippet..." else "Ask KIN AI anything...",
              fontSize = 14.sp
            )
          },
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp, max = 130.dp)
            .testTag("chat_input_textfield"),
          shape = RoundedCornerShape(18.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PeachCoral,
            unfocusedBorderColor = Color.Transparent
          )
        )

        if (isGenerating) {
          IconButton(
            onClick = onStopGeneration,
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.error)
              .testTag("chat_stop_btn")
          ) {
            Icon(
              imageVector = Icons.Filled.Stop,
              contentDescription = "Stop response",
              tint = Color.White
            )
          }
        } else {
          IconButton(
            onClick = {
              if (inputText.isNotBlank()) {
                val toSend = inputText
                inputText = ""
                onSendMessage(toSend)
              }
            },
            enabled = inputText.isNotBlank(),
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(if (inputText.isNotBlank()) PeachCoral else MaterialTheme.colorScheme.surfaceVariant)
              .testTag("chat_send_btn")
          ) {
            Icon(
              imageVector = Icons.Filled.Send,
              contentDescription = "Send message",
              tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

@Composable
fun ChatMessageBubble(
  message: ChatMessage,
  onCopy: (String) -> Unit,
  onRetry: () -> Unit,
  onSpeak: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isUser = message.role == MessageRole.USER
  val timeFormatted = remember(message.timestamp) {
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    // Role badge & timestamp
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
      Text(
        text = if (isUser) "You" else "KIN AI (${message.modelUsed})",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = if (isUser) PeachCoral else SunsetOrange,
        fontSize = 11.sp
      )
      Text(
        text = timeFormatted,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        fontSize = 10.sp
      )
    }

    // Message Bubble Content
    if (isUser) {
      Box(
        modifier = Modifier
          .fillMaxWidth(0.85f)
          .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
          .background(
            brush = Brush.linearGradient(
              listOf(SunsetOrange, PeachCoral)
            )
          )
          .padding(14.dp)
      ) {
        SelectionContainer {
          Text(
            text = message.content,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
          )
        }
      }
    } else {
      // Assistant Bubble with code block parsing
      Card(
        modifier = Modifier.fillMaxWidth(0.92f),
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (message.isError)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
          else
            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (message.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Parse code blocks if any
          val content = message.content
          if (content.contains("```")) {
            FormattedCodeMessage(content = content, onCopyCode = onCopy)
          } else {
            SelectionContainer {
              Text(
                text = content,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Assistant actions (Copy, Speak, Retry)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = onSpeak,
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.VolumeUp,
                contentDescription = "Speak message",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            }

            IconButton(
              onClick = { onCopy(message.content) },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "Copy message",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            }

            if (message.isError) {
              IconButton(
                onClick = onRetry,
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = Icons.Filled.Refresh,
                  contentDescription = "Retry request",
                  tint = PeachCoral,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun FormattedCodeMessage(
  content: String,
  onCopyCode: (String) -> Unit
) {
  val parts = content.split("```")
  for (i in parts.indices) {
    val part = parts[i]
    if (i % 2 == 1) {
      // Code block
      val lines = part.trim().lines()
      val lang = lines.firstOrNull()?.trim() ?: "code"
      val codeText = if (lines.size > 1) lines.drop(1).joinToString("\n") else part.trim()

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CodeBlockBg)
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = lang.uppercase(),
              color = SunsetGold,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
            IconButton(
              onClick = { onCopyCode(codeText) },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "Copy code",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          SelectionContainer {
            Text(
              text = codeText,
              color = Color(0xFFF0F0F0),
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              lineHeight = 18.sp
            )
          }
        }
      }
    } else {
      if (part.isNotBlank()) {
        SelectionContainer {
          Text(
            text = part.trim(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp
          )
        }
      }
    }
  }
}
