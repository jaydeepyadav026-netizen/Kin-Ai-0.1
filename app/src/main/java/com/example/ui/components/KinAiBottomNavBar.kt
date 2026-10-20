package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.PeachCoral
import com.example.ui.theme.PeachGlow
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.SunsetOrange

@Composable
fun KinAiBottomNavBar(
  currentScreen: Screen,
  onScreenSelected: (Screen) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding(),
    color = Color.Transparent
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      contentAlignment = Alignment.BottomCenter
    ) {
      // Main Bar Container
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(68.dp)
          .shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(28.dp),
            ambientColor = PeachCoral.copy(alpha = 0.25f),
            spotColor = PeachCoral.copy(alpha = 0.35f)
          )
          .clip(RoundedCornerShape(28.dp))
          .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
          .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            shape = RoundedCornerShape(28.dp)
          )
          .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // 1. Home Tab
        NavTabItem(
          label = "Home",
          selected = currentScreen == Screen.HOME,
          selectedIcon = Icons.Filled.Home,
          unselectedIcon = Icons.Outlined.Home,
          tag = "nav_home",
          onClick = { onScreenSelected(Screen.HOME) }
        )

        // 2. Chat Tab
        NavTabItem(
          label = "Chat",
          selected = currentScreen == Screen.CHAT,
          selectedIcon = Icons.Filled.ChatBubble,
          unselectedIcon = Icons.Outlined.ChatBubbleOutline,
          tag = "nav_chat",
          onClick = { onScreenSelected(Screen.CHAT) }
        )

        // Empty space placeholder for central prominent voice button
        Box(modifier = Modifier.size(56.dp))

        // 4. Settings Tab
        NavTabItem(
          label = "Settings",
          selected = currentScreen == Screen.SETTINGS,
          selectedIcon = Icons.Filled.Settings,
          unselectedIcon = Icons.Outlined.Settings,
          tag = "nav_settings",
          onClick = { onScreenSelected(Screen.SETTINGS) }
        )

        // 5. Updates Tab
        NavTabItem(
          label = "Updates",
          selected = currentScreen == Screen.UPDATES,
          selectedIcon = Icons.Filled.AutoAwesome,
          unselectedIcon = Icons.Outlined.SystemUpdate,
          tag = "nav_updates",
          onClick = { onScreenSelected(Screen.UPDATES) }
        )
      }

      // 3. Central Voice Assistant Button (Elevated, Prominent, Larger)
      val isVoiceActive = currentScreen == Screen.VOICE
      val voiceScale by animateFloatAsState(
        targetValue = if (isVoiceActive) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "VoiceButtonScale"
      )

      Box(
        modifier = Modifier
          .offset(y = (-14).dp)
          .scale(voiceScale)
          .size(62.dp)
          .shadow(
            elevation = 14.dp,
            shape = CircleShape,
            ambientColor = SunsetOrange,
            spotColor = PeachCoral
          )
          .clip(CircleShape)
          .background(
            brush = Brush.linearGradient(
              colors = if (isVoiceActive) {
                listOf(PeachCoral, SunsetGold, SunsetOrange)
              } else {
                listOf(SunsetOrange, PeachCoral)
              }
            )
          )
          .border(
            width = if (isVoiceActive) 2.5.dp else 1.5.dp,
            color = if (isVoiceActive) Color.White else PeachGlow.copy(alpha = 0.7f),
            shape = CircleShape
          )
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) {
            onScreenSelected(Screen.VOICE)
          }
          .testTag("nav_voice_central"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Filled.Mic,
          contentDescription = "KIN AI Voice Assistant",
          tint = Color.White,
          modifier = Modifier.size(32.dp)
        )
      }
    }
  }
}

@Composable
private fun NavTabItem(
  label: String,
  selected: Boolean,
  selectedIcon: ImageVector,
  unselectedIcon: ImageVector,
  tag: String,
  onClick: () -> Unit
) {
  val animatedColor by animateColorAsState(
    targetValue = if (selected) PeachCoral else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
    label = "NavColor"
  )

  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(14.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag(tag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = if (selected) selectedIcon else unselectedIcon,
      contentDescription = label,
      tint = animatedColor,
      modifier = Modifier.size(23.dp)
    )
    Text(
      text = label,
      color = animatedColor,
      fontSize = 11.sp,
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(top = 2.dp)
    )
  }
}
