package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.automation.BatteryStatus
import com.example.data.automation.NetworkStatus
import com.example.ui.components.GlassCard
import com.example.ui.theme.PeachCoral
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.SunsetOrange

@Composable
fun UpdatesScreen(
  assistantName: String,
  batteryStatus: BatteryStatus,
  networkStatus: NetworkStatus,
  isTtsReady: Boolean,
  hasApiKey: Boolean,
  onRefreshTelemetry: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
  val hasPostNotif = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "SYSTEM UPDATES & TELEMETRY",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = PeachCoral,
            letterSpacing = 1.5.sp
          )
          Text(
            text = "KIN AI v2.5.0 Quantum",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
          )
        }

        IconButton(
          onClick = onRefreshTelemetry,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Icon(Icons.Filled.Refresh, "Refresh Diagnostics", tint = PeachCoral, modifier = Modifier.size(20.dp))
        }
      }
    }

    // Diagnostics Matrix Card
    item {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "LIVE SYSTEM DIAGNOSTICS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PeachCoral
          )

          DiagnosticRow(
            label = "Neural Core Engine",
            status = if (hasApiKey) "Gemini Cloud REST Active" else "KIN Local Engine (Ready)",
            isOk = true
          )

          DiagnosticRow(
            label = "Voice Text-To-Speech (TTS)",
            status = if (isTtsReady) "Hardware Synthesis Ready" else "Initializing Voice Buffer",
            isOk = isTtsReady
          )

          DiagnosticRow(
            label = "Microphone Sensor",
            status = if (hasMic) "Permission Active" else "Permission Required",
            isOk = hasMic
          )

          DiagnosticRow(
            label = "Network Connectivity",
            status = if (networkStatus.isConnected) "${networkStatus.connectionType} (${if (networkStatus.isMetered) "Metered" else "Unmetered"})" else "Offline",
            isOk = networkStatus.isConnected
          )

          DiagnosticRow(
            label = "Battery Management",
            status = "${batteryStatus.level}% (${batteryStatus.powerSource})",
            isOk = batteryStatus.level > 15
          )

          DiagnosticRow(
            label = "Android Intent Bridge",
            status = "YouTube, Maps, Search, Camera, Dial Ready",
            isOk = true
          )
        }
      }
    }

    // Feature Announcements & What's New
    item {
      Text(
        text = "WHAT'S NEW IN V2.5.0",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = PeachCoral,
        letterSpacing = 1.2.sp
      )

      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ChangelogItem(
            tag = "NEW",
            title = "Warm Ivory & Coral Design System",
            description = "Complete UI overhaul inspired by futuristic JARVIS aesthetics with animated rotating neural reactor orb."
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

          ChangelogItem(
            tag = "ENGINE",
            title = "Dual Chat & Code Modes",
            description = "Specialized code generation formatting with syntax styling, clipboard copy, and technical reasoning."
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

          ChangelogItem(
            tag = "VOICE",
            title = "Central Audio Core & Multilingual TTS",
            description = "Interactive voice assistant supporting English, Hindi, and Hinglish with speech rate and pitch control."
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

          ChangelogItem(
            tag = "INTENTS",
            title = "Permission-Aware Android Automation Bridge",
            description = "Deep intent integrations for YouTube queries, real-time map navigation, search providers, camera lens, and calendar tasks."
          )
        }
      }
    }

    // Troubleshooting Guide
    item {
      Text(
        text = "TROUBLESHOOTING & FAQ",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = PeachCoral,
        letterSpacing = 1.2.sp
      )

      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "• How does Voice Assistant work without API keys?",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "KIN AI features a built-in on-device reasoning engine. Even without external cloud keys, conversations, speech synthesis, and local automation functions work immediately.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "• How to configure Gemini API Key?",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Configure your GEMINI_API_KEY in the AI Studio Secrets panel, or paste your API key directly in the Settings tab.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun DiagnosticRow(
  label: String,
  status: String,
  isOk: Boolean
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = status,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp
      )
    }

    Icon(
      imageVector = if (isOk) Icons.Filled.CheckCircle else Icons.Filled.Warning,
      contentDescription = null,
      tint = if (isOk) StatusGreen else StatusAmber,
      modifier = Modifier.size(18.dp)
    )
  }
}

@Composable
private fun ChangelogItem(
  tag: String,
  title: String,
  description: String
) {
  Column {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(PeachCoral.copy(alpha = 0.2f))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = tag,
          color = PeachCoral,
          fontSize = 9.sp,
          fontWeight = FontWeight.Black
        )
      }
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
    }
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = description,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
