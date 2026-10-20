package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PeachCoral
import com.example.ui.theme.PeachGlow
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.SunsetOrange
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedKinOrb(
  modifier: Modifier = Modifier,
  size: Dp = 160.dp,
  isActive: Boolean = false,
  isListening: Boolean = false
) {
  val infiniteTransition = rememberInfiniteTransition(label = "KinOrbTransition")

  // Pulse animation for the core
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = if (isListening) 0.85f else 0.95f,
    targetValue = if (isListening) 1.25f else 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (isListening) 650 else 1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "PulseScale"
  )

  // Continuous rotation for outer cybernetic ring
  val rotationRing1 by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (isListening) 4000 else 12000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "RotationRing1"
  )

  // Counter-rotation for inner neural ring
  val rotationRing2 by infiniteTransition.animateFloat(
    initialValue = 360f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (isListening) 3000 else 8000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "RotationRing2"
  )

  // Glow shimmer
  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = if (isListening) 0.9f else 0.65f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (isListening) 500 else 1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "GlowAlpha"
  )

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size)) {
      val center = Offset(this.size.width / 2f, this.size.height / 2f)
      val radius = (this.size.minDimension / 2f) * 0.9f

      // 1. Outer Holographic Glow Aura
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            PeachCoral.copy(alpha = glowAlpha * 0.45f),
            SunsetGold.copy(alpha = glowAlpha * 0.2f),
            Color.Transparent
          ),
          center = center,
          radius = radius * 1.15f * pulseScale
        ),
        radius = radius * 1.15f * pulseScale,
        center = center
      )

      // 2. Outer Segmented Cybernetic Ring (Rotating clockwise)
      rotate(rotationRing1, pivot = center) {
        val outerRadius = radius * 0.95f
        drawCircle(
          brush = Brush.sweepGradient(
            colors = listOf(
              PeachCoral.copy(alpha = 0.85f),
              SunsetGold.copy(alpha = 0.9f),
              PeachGlow.copy(alpha = 0.3f),
              PeachCoral.copy(alpha = 0.85f)
            ),
            center = center
          ),
          radius = outerRadius,
          center = center,
          style = Stroke(width = 3.dp.toPx())
        )

        // Draw orbital neural nodes on the outer ring
        val nodeCount = 6
        for (i in 0 until nodeCount) {
          val angle = (i * (360.0 / nodeCount)) * (Math.PI / 180.0)
          val nx = center.x + outerRadius * cos(angle).toFloat()
          val ny = center.y + outerRadius * sin(angle).toFloat()
          drawCircle(
            color = SunsetGold,
            radius = 3.5.dp.toPx(),
            center = Offset(nx, ny)
          )
        }
      }

      // 3. Middle Hex/Dotted Orbit Ring (Rotating counter-clockwise)
      rotate(rotationRing2, pivot = center) {
        val midRadius = radius * 0.75f
        drawCircle(
          color = SunsetOrange.copy(alpha = 0.45f),
          radius = midRadius,
          center = center,
          style = Stroke(
            width = 1.5.dp.toPx()
          )
        )

        // Geometric cross ticks
        val tickCount = 8
        for (i in 0 until tickCount) {
          val angle = (i * (360.0 / tickCount)) * (Math.PI / 180.0)
          val t1x = center.x + (midRadius - 4.dp.toPx()) * cos(angle).toFloat()
          val t1y = center.y + (midRadius - 4.dp.toPx()) * sin(angle).toFloat()
          val t2x = center.x + (midRadius + 4.dp.toPx()) * cos(angle).toFloat()
          val t2y = center.y + (midRadius + 4.dp.toPx()) * sin(angle).toFloat()
          drawLine(
            color = PeachCoral.copy(alpha = 0.7f),
            start = Offset(t1x, t1y),
            end = Offset(t2x, t2y),
            strokeWidth = 2.dp.toPx()
          )
        }
      }

      // 4. Inner Luminous Core
      val coreRadius = radius * 0.45f * pulseScale
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.White,
            PeachGlow,
            PeachCoral,
            SunsetOrange
          ),
          center = center,
          radius = coreRadius
        ),
        radius = coreRadius,
        center = center
      )

      // 5. Central Reactor Diamond/Cross Highlight
      val diamondSize = coreRadius * 0.5f
      drawLine(
        color = Color.White.copy(alpha = 0.95f),
        start = Offset(center.x - diamondSize, center.y),
        end = Offset(center.x + diamondSize, center.y),
        strokeWidth = 2.dp.toPx()
      )
      drawLine(
        color = Color.White.copy(alpha = 0.95f),
        start = Offset(center.x, center.y - diamondSize),
        end = Offset(center.x, center.y + diamondSize),
        strokeWidth = 2.dp.toPx()
      )
    }
  }
}
