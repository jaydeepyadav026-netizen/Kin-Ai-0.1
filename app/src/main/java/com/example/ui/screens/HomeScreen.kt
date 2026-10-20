package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.automation.BatteryStatus
import com.example.data.automation.NetworkStatus
import com.example.data.model.ChatMode
import com.example.data.model.Conversation
import com.example.data.model.TaskProgressInfo
import com.example.ui.Screen
import com.example.ui.components.AnimatedKinOrb
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkPlumText
import com.example.ui.theme.PeachCoral
import com.example.ui.theme.PeachGlow
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.SunsetOrange

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
  userName: String,
  assistantName: String,
  personalityName: String,
  batteryStatus: BatteryStatus,
  networkStatus: NetworkStatus,
  conversations: List<Conversation>,
  activeTask: TaskProgressInfo?,
  onNavigate: (Screen) -> Unit,
  onStartNewChat: (ChatMode) -> Unit,
  onSelectConversation: (String) -> Unit,
  onExecuteAutomation: (String, String) -> Unit,
  onStartSampleTask: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // 1. Personalized Greeting & Status Header
    item {
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "HELLO, ${userName.uppercase()}",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = PeachCoral,
          letterSpacing = 1.5.sp
        )
        Text(
          text = "I am $assistantName.",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Your personal AI assistant is ready.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 2. Centerpiece: Animated JARVIS Neural Orb with Quick Actions
    item {
      GlassCard(
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.clickable { onNavigate(Screen.VOICE) }
          ) {
            AnimatedKinOrb(
              size = 150.dp,
              isActive = true,
              isListening = false
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(StatusGreen)
            )
            Text(
              text = "$personalityName Mode Active",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onBackground
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Primary Creation / Action Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { onNavigate(Screen.VOICE) },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("home_quick_voice_btn"),
              colors = ButtonDefaults.buttonColors(containerColor = PeachCoral),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Voice Command", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
              onClick = { onStartNewChat(ChatMode.CHAT) },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("home_start_chat_btn"),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(Icons.Filled.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("New Chat", fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }

    // 3. Task Progress Panel (if task is active, or trigger one)
    item {
      GlassCard(
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.SmartToy,
                contentDescription = null,
                tint = SunsetOrange,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = "Task Operations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
              )
            }

            IconButton(
              onClick = onStartSampleTask,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Run task diagnostics",
                tint = PeachCoral,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          if (activeTask != null) {
            Text(
              text = activeTask.title,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onBackground
            )
            Text(
              text = activeTask.currentOperation,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
              progress = { 0.75f },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = PeachCoral,
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
          } else {
            Text(
              text = "System idle. Ready to execute multi-step automations, code compilation, and device telemetry sync.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            FilledTonalButton(
              onClick = onStartSampleTask,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Run System Diagnostics Task")
            }
          }
        }
      }
    }

    // 4. Quick Automation Shortcuts (Android Integrations)
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "DEVICE AUTOMATIONS",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = PeachCoral,
          letterSpacing = 1.2.sp,
          modifier = Modifier.padding(bottom = 10.dp)
        )

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          AutomationQuickCard(
            title = "YouTube",
            icon = Icons.Filled.PlayArrow,
            onClick = { onExecuteAutomation("youtube", "") }
          )
          AutomationQuickCard(
            title = "Web Search",
            icon = Icons.Filled.Search,
            onClick = { onExecuteAutomation("search", "AI news") }
          )
          AutomationQuickCard(
            title = "Maps",
            icon = Icons.Filled.Navigation,
            onClick = { onExecuteAutomation("maps", "") }
          )
          AutomationQuickCard(
            title = "Camera",
            icon = Icons.Filled.PhotoCamera,
            onClick = { onExecuteAutomation("camera", "") }
          )
          AutomationQuickCard(
            title = "Calculator",
            icon = Icons.Filled.Calculate,
            onClick = { onExecuteAutomation("calculator", "") }
          )
          AutomationQuickCard(
            title = "Reminder",
            icon = Icons.Filled.EventNote,
            onClick = { onExecuteAutomation("reminder", "Team review") }
          )
        }
      }
    }

    // 5. Intelligent Prompts & Suggestions
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "INTELLIGENT SUGGESTIONS",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = PeachCoral,
          letterSpacing = 1.2.sp,
          modifier = Modifier.padding(bottom = 10.dp)
        )

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          SuggestionChip(
            onClick = {
              onStartNewChat(ChatMode.CODE)
            },
            label = { Text("Code: Kotlin Coroutines Flow") },
            icon = { Icon(Icons.Filled.Code, null, modifier = Modifier.size(16.dp)) },
            colors = SuggestionChipDefaults.suggestionChipColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
          )

          SuggestionChip(
            onClick = {
              onStartNewChat(ChatMode.CHAT)
            },
            label = { Text("Explain Quantum Supremacy") },
            colors = SuggestionChipDefaults.suggestionChipColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
          )

          SuggestionChip(
            onClick = {
              onStartNewChat(ChatMode.CHAT)
            },
            label = { Text("Plan a 3-Day Tech Conference") },
            colors = SuggestionChipDefaults.suggestionChipColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
          )
        }
      }
    }

    // 6. Recent Conversations Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "RECENT SESSIONS",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = PeachCoral,
          letterSpacing = 1.2.sp
        )
        Text(
          text = "${conversations.size} saved",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    if (conversations.isEmpty()) {
      item {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No previous sessions found. Start a new chat or voice session above!",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(conversations.take(4)) { conv ->
        GlassCard(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              onSelectConversation(conv.id)
              onNavigate(Screen.CHAT)
            }
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(if (conv.mode == ChatMode.CODE) SunsetOrange.copy(alpha = 0.2f) else PeachCoral.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (conv.mode == ChatMode.CODE) Icons.Filled.Code else Icons.Filled.ChatBubbleOutline,
                  contentDescription = null,
                  tint = PeachCoral,
                  modifier = Modifier.size(18.dp)
                )
              }
              Column {
                Text(
                  text = conv.title,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onBackground,
                  maxLines = 1
                )
                Text(
                  text = if (conv.snippet.isNotBlank()) conv.snippet else "Session active",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1
                )
              }
            }
            Icon(
              imageVector = Icons.Filled.ChevronRight,
              contentDescription = "Open session",
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AutomationQuickCard(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .width(102.dp)
      .height(84.dp)
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.Start
    ) {
      Box(
        modifier = Modifier
          .size(30.dp)
          .clip(CircleShape)
          .background(PeachCoral.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = PeachCoral,
          modifier = Modifier.size(16.dp)
        )
      }
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
      )
    }
  }
}
