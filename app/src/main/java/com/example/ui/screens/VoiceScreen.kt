package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AssistantLanguage
import com.example.data.model.ChatMessage
import com.example.data.model.MessageRole
import com.example.ui.components.AnimatedKinOrb
import com.example.ui.components.GlassCard
import com.example.ui.theme.PeachCoral
import com.example.ui.theme.PeachGlow
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.SunsetOrange

@Composable
fun VoiceScreen(
  isListening: Boolean,
  isSpeaking: Boolean,
  isMuted: Boolean,
  recognizedText: String,
  speechError: String?,
  recentMessages: List<ChatMessage>,
  currentLanguage: AssistantLanguage,
  speechRate: Float,
  speechPitch: Float,
  onToggleListening: () -> Unit,
  onSpeakText: (String) -> Unit,
  onStopSpeaking: () -> Unit,
  onToggleMute: () -> Unit,
  onLanguageSelected: (AssistantLanguage) -> Unit,
  onSpeechRateChange: (Float) -> Unit,
  onSpeechPitchChange: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var hasMicPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
      ) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasMicPermission = isGranted
    if (isGranted) {
      onToggleListening()
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "VoicePulse")
  val micPulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (isListening) 1.25f else 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (isListening) 550 else 1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "MicScale"
  )

  val lastAssistantMessage = recentMessages.lastOrNull { it.role == MessageRole.ASSISTANT }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    Spacer(modifier = Modifier.height(6.dp))

    // Header Title
    Text(
      text = "JARVIS VOICE CORE",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = PeachCoral,
      letterSpacing = 1.5.sp
    )
    Text(
      text = if (isListening) "Listening for speech..." else if (isSpeaking) "Synthesizing voice response..." else "Tap microphone to speak",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground,
      textAlign = TextAlign.Center
    )

    // Language Selector Chips
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      AssistantLanguage.values().forEach { lang ->
        FilterChip(
          selected = currentLanguage == lang,
          onClick = { onLanguageSelected(lang) },
          label = { Text(lang.displayName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PeachCoral,
            selectedLabelColor = Color.White
          )
        )
      }
    }

    // Central Glowing Animated Reactor / Microphone
    Box(
      modifier = Modifier
        .size(240.dp)
        .padding(vertical = 12.dp),
      contentAlignment = Alignment.Center
    ) {
      AnimatedKinOrb(
        size = 230.dp,
        isActive = true,
        isListening = isListening
      )

      // Central Mic Tap Button
      Box(
        modifier = Modifier
          .scale(micPulseScale)
          .size(90.dp)
          .shadow(16.dp, CircleShape, ambientColor = PeachCoral, spotColor = SunsetGold)
          .clip(CircleShape)
          .background(
            brush = Brush.radialGradient(
              listOf(SunsetGold, PeachCoral, SunsetOrange)
            )
          )
          .border(2.5.dp, Color.White, CircleShape)
          .clickable {
            if (hasMicPermission) {
              onToggleListening()
            } else {
              permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
          }
          .testTag("voice_screen_mic_btn"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
          contentDescription = "Voice Input Button",
          tint = Color.White,
          modifier = Modifier.size(44.dp)
        )
      }
    }

    // Live Speech Transcription / Error Card
    if (speechError != null) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
      ) {
        Text(
          text = "Speech note: $speechError",
          color = MaterialTheme.colorScheme.onErrorContainer,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(14.dp)
        )
      }
    }

    if (recognizedText.isNotBlank()) {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "HEARD SPEECH PREVIEW",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PeachCoral,
            fontSize = 10.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "\"$recognizedText\"",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    // Spoken AI Response Card & Controls
    if (lastAssistantMessage != null) {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (isSpeaking) StatusGreen else PeachCoral)
              )
              Text(
                text = if (isSpeaking) "SPEAKING NOW" else "ASSISTANT VOCALIZATION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PeachCoral,
                fontSize = 10.sp
              )
            }

            Row {
              IconButton(
                onClick = onToggleMute,
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = if (isMuted) Icons.Filled.VolumeMute else Icons.Filled.VolumeUp,
                  contentDescription = "Mute audio",
                  tint = if (isMuted) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }

              if (isSpeaking) {
                IconButton(
                  onClick = onStopSpeaking,
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = "Stop speaking",
                    tint = Color.Red,
                    modifier = Modifier.size(18.dp)
                  )
                }
              } else {
                IconButton(
                  onClick = { onSpeakText(lastAssistantMessage.content) },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Replay audio",
                    tint = PeachCoral,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = lastAssistantMessage.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 4
          )
        }
      }
    }

    // Voice Pitch & Speed Adjusters
    GlassCard(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "VOICE ENGINE TUNING",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = PeachCoral,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Speech Rate", style = MaterialTheme.typography.bodySmall)
          Text("${String.format("%.1f", speechRate)}x", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Slider(
          value = speechRate,
          onValueChange = onSpeechRateChange,
          valueRange = 0.6f..1.6f,
          colors = SliderDefaults.colors(
            thumbColor = PeachCoral,
            activeTrackColor = PeachCoral
          )
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Voice Pitch", style = MaterialTheme.typography.bodySmall)
          Text("${String.format("%.1f", speechPitch)}x", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Slider(
          value = speechPitch,
          onValueChange = onSpeechPitchChange,
          valueRange = 0.7f..1.4f,
          colors = SliderDefaults.colors(
            thumbColor = SunsetOrange,
            activeTrackColor = SunsetOrange
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(90.dp))
  }
}
