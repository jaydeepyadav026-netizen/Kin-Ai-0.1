package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.automation.BatteryStatus
import com.example.data.automation.NetworkStatus
import com.example.data.model.AssistantLanguage
import com.example.data.model.AssistantPersonality
import com.example.ui.Screen
import com.example.ui.components.GlassCard
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.DarkPlumBg
import com.example.ui.theme.PeachCoral
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.SunsetOrange
import com.example.ui.theme.WarmIvoryBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  assistantName: String,
  userName: String,
  personality: AssistantPersonality,
  language: AssistantLanguage,
  themeMode: AppThemeMode,
  geminiApiKey: String,
  selectedModel: String,
  floatingOrbEnabled: Boolean,
  batteryStatus: BatteryStatus,
  networkStatus: NetworkStatus,
  onUpdateAssistantName: (String) -> Unit,
  onUpdateUserName: (String) -> Unit,
  onUpdatePersonality: (AssistantPersonality) -> Unit,
  onUpdateLanguage: (AssistantLanguage) -> Unit,
  onUpdateThemeMode: (AppThemeMode) -> Unit,
  onUpdateGeminiApiKey: (String) -> Unit,
  onUpdateSelectedModel: (String) -> Unit,
  onUpdateFloatingOrb: (Boolean) -> Unit,
  onTestConnection: ((String, Boolean) -> Unit) -> Unit,
  onClearAllData: () -> Unit,
  onExportData: () -> Unit,
  onNavigateToMemory: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  var editUserName by remember(userName) { mutableStateOf(userName) }
  var editAssistantName by remember(assistantName) { mutableStateOf(assistantName) }
  var editApiKey by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }

  var isTestingConnection by remember { mutableStateOf(false) }
  var connectionTestResult by remember { mutableStateOf<String?>(null) }
  var isConnectionSuccess by remember { mutableStateOf(false) }

  var showClearConfirmDialog by remember { mutableStateOf(false) }

  val hasMicPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
  val micPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = { Text("Erase All Local Data?") },
      text = { Text("This will delete all conversations, messages, and stored neural memories from SQLite database.") },
      confirmButton = {
        Button(
          onClick = {
            showClearConfirmDialog = false
            onClearAllData()
            Toast.makeText(context, "All stored data erased", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Erase Everything")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    item {
      Text(
        text = "INTELLIGENCE & SYSTEM SETTINGS",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = PeachCoral,
        letterSpacing = 1.5.sp
      )
      Text(
        text = "Configure KIN AI Core",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground
      )
    }

    // 1. Profile Section
    item {
      SettingsSectionHeader(title = "PROFILE IDENTITY", icon = Icons.Filled.Person)
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          OutlinedTextField(
            value = editAssistantName,
            onValueChange = {
              editAssistantName = it
              onUpdateAssistantName(it)
            },
            label = { Text("Assistant Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = editUserName,
            onValueChange = {
              editUserName = it
              onUpdateUserName(it)
            },
            label = { Text("User Display Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
        }
      }
    }

    // 2. AI Personality & Voice Section
    item {
      SettingsSectionHeader(title = "VOICE & PERSONALITY", icon = Icons.Filled.Psychology)
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = "AI Personality Preset",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistantPersonality.values().forEach { p ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(
                    if (personality == p) PeachCoral.copy(alpha = 0.15f) else Color.Transparent
                  )
                  .clickable { onUpdatePersonality(p) }
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = p.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (personality == p) PeachCoral else MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = p.promptInstruction.take(65) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                  )
                }
                if (personality == p) {
                  Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = PeachCoral,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

          Text(
            text = "Conversation & Voice Language",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            AssistantLanguage.values().forEach { lang ->
              FilterChip(
                selected = language == lang,
                onClick = { onUpdateLanguage(lang) },
                label = { Text(lang.displayName, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PeachCoral,
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }
      }
    }

    // 3. AI Provider & Data Controls
    item {
      SettingsSectionHeader(title = "AI ENGINE & CREDENTIALS", icon = Icons.Filled.Key)
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = "Gemini API Configuration",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          OutlinedTextField(
            value = editApiKey,
            onValueChange = {
              editApiKey = it
              onUpdateGeminiApiKey(it)
            },
            label = { Text("Gemini API Key (Optional Override)") },
            placeholder = { Text("Auto-injected from AI Studio secrets or enter custom") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          // Model Selector
          Text(
            text = "Active AI Model",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf("gemini-3.5-flash", "gemini-3.1-pro-preview").forEach { model ->
              FilterChip(
                selected = selectedModel == model,
                onClick = { onUpdateSelectedModel(model) },
                label = { Text(model, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = SunsetOrange,
                  selectedLabelColor = Color.White
                )
              )
            }
          }

          // Test Connection Button
          Button(
            onClick = {
              isTestingConnection = true
              connectionTestResult = null
              onTestConnection { resultMsg, isSuccess ->
                isTestingConnection = false
                connectionTestResult = resultMsg
                isConnectionSuccess = isSuccess
              }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)
          ) {
            if (isTestingConnection) {
              CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Testing API...")
            } else {
              Text("Test Connection to Cloud")
            }
          }

          if (connectionTestResult != null) {
            Text(
              text = connectionTestResult ?: "",
              style = MaterialTheme.typography.bodySmall,
              color = if (isConnectionSuccess) StatusGreen else MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.SemiBold
            )
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

          // Neural Memory Shortcut
          OutlinedButton(
            onClick = onNavigateToMemory,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Filled.Psychology, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Manage Neural Memory Bank")
          }
        }
      }
    }

    // 4. Android System Telemetry & Permissions
    item {
      SettingsSectionHeader(title = "ANDROID SYSTEM STATUS", icon = Icons.Filled.Android)
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Microphone Permission", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              Text(if (hasMicPerm) "Granted (Voice recognition active)" else "Required for speech input", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!hasMicPerm) {
              Button(
                onClick = { micPermLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PeachCoral)
              ) {
                Text("Grant", fontSize = 12.sp)
              }
            } else {
              Icon(Icons.Filled.Check, null, tint = StatusGreen, modifier = Modifier.size(20.dp))
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Battery Telemetry", style = MaterialTheme.typography.bodySmall)
            Text("${batteryStatus.level}% (${batteryStatus.powerSource})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Network Interface", style = MaterialTheme.typography.bodySmall)
            Text(networkStatus.connectionType, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Floating KIN Avatar Orb", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              Text("In-app draggable assistant overlay", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = floatingOrbEnabled,
              onCheckedChange = onUpdateFloatingOrb,
              colors = SwitchDefaults.colors(checkedThumbColor = PeachCoral)
            )
          }
        }
      }
    }

    // 5. Appearance & Themes
    item {
      SettingsSectionHeader(title = "APPEARANCE & THEME", icon = Icons.Filled.Palette)
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text("Select Theme Environment", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            AppThemeMode.values().forEach { mode ->
              FilterChip(
                selected = themeMode == mode,
                onClick = { onUpdateThemeMode(mode) },
                label = {
                  Text(
                    when (mode) {
                      AppThemeMode.LIGHT -> "Warm Ivory"
                      AppThemeMode.DARK -> "Obsidian Plum"
                      AppThemeMode.SUNSET -> "Sunset Glow"
                    },
                    fontSize = 11.sp
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PeachCoral,
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }
      }
    }

    // 6. Data Storage & Privacy
    item {
      SettingsSectionHeader(title = "LOCAL DATA & PRIVACY", icon = Icons.Filled.Security)
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Data is stored securely on-device using SQLite. No hidden background recording or secret data transfer occurs.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedButton(
            onClick = onExportData,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Export Conversations to Clipboard")
          }

          Button(
            onClick = { showClearConfirmDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
          ) {
            Icon(Icons.Filled.DeleteSweep, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Erase All Local Data")
          }
        }
      }
    }
  }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.padding(vertical = 4.dp)
  ) {
    Icon(icon, null, tint = PeachCoral, modifier = Modifier.size(18.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = PeachCoral,
      letterSpacing = 1.2.sp
    )
  }
}
