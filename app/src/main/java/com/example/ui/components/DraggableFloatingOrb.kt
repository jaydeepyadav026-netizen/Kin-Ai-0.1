package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PeachCoral
import com.example.ui.theme.SunsetGold
import kotlin.math.roundToInt

@Composable
fun DraggableFloatingOrb(
  visible: Boolean,
  onOrbClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var offsetX by remember { mutableFloatStateOf(20f) }
  var offsetY by remember { mutableFloatStateOf(450f) }

  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + scaleIn(),
    exit = fadeOut() + scaleOut()
  ) {
    Box(
      modifier = modifier
        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        .pointerInput(Unit) {
          detectDragGestures { change, dragAmount ->
            change.consume()
            offsetX = (offsetX + dragAmount.x).coerceIn(0f, 800f)
            offsetY = (offsetY + dragAmount.y).coerceIn(100f, 1600f)
          }
        }
        .size(60.dp)
        .shadow(12.dp, CircleShape, ambientColor = PeachCoral, spotColor = SunsetGold)
        .clip(CircleShape)
        .background(PeachCoral)
        .border(2.dp, SunsetGold, CircleShape)
        .clickable(onClick = onOrbClick)
        .testTag("floating_avatar_orb"),
      contentAlignment = Alignment.Center
    ) {
      AnimatedKinOrb(
        size = 54.dp,
        isActive = true,
        isListening = false
      )
      Icon(
        imageVector = Icons.Filled.Mic,
        contentDescription = "Tap to speak with KIN AI",
        tint = Color.White.copy(alpha = 0.9f),
        modifier = Modifier.size(20.dp)
      )
    }
  }
}
