package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
  LIGHT,
  DARK,
  SUNSET
}

val LightColorScheme = lightColorScheme(
  primary = PeachCoral,
  onPrimary = Color.White,
  primaryContainer = PeachGlow.copy(alpha = 0.35f),
  onPrimaryContainer = DarkPlumText,
  secondary = SunsetOrange,
  onSecondary = Color.White,
  secondaryContainer = SunsetGold.copy(alpha = 0.3f),
  onSecondaryContainer = DarkPlumText,
  tertiary = SoftPurpleSecondary,
  onTertiary = Color.White,
  background = WarmIvoryBg,
  onBackground = DarkPlumText,
  surface = WarmIvorySurface,
  onSurface = DarkPlumText,
  surfaceVariant = WarmIvorySurfaceVariant,
  onSurfaceVariant = SoftPurpleSecondary,
  outline = WarmIvoryBorder,
  outlineVariant = WarmIvoryBorder.copy(alpha = 0.5f)
)

val DarkColorScheme = darkColorScheme(
  primary = PeachCoral,
  onPrimary = Color.White,
  primaryContainer = PeachCoral.copy(alpha = 0.25f),
  onPrimaryContainer = PeachLight,
  secondary = SunsetOrange,
  onSecondary = Color.White,
  secondaryContainer = SunsetOrange.copy(alpha = 0.25f),
  onSecondaryContainer = SunsetGold,
  tertiary = SoftPurpleMuted,
  onTertiary = DarkPlumBg,
  background = DarkPlumBg,
  onBackground = DarkPlumTextPrimary,
  surface = DarkPlumSurface,
  onSurface = DarkPlumTextPrimary,
  surfaceVariant = DarkPlumSurfaceVariant,
  onSurfaceVariant = DarkPlumTextSecondary,
  outline = DarkPlumBorder,
  outlineVariant = DarkPlumBorder.copy(alpha = 0.5f)
)

val SunsetColorScheme = darkColorScheme(
  primary = SunsetOrange,
  onPrimary = Color.White,
  primaryContainer = SunsetOrange.copy(alpha = 0.3f),
  onPrimaryContainer = SunsetGold,
  secondary = PeachCoral,
  onSecondary = Color.White,
  secondaryContainer = PeachCoral.copy(alpha = 0.3f),
  onSecondaryContainer = PeachLight,
  tertiary = SoftPurpleSecondary,
  onTertiary = Color.White,
  background = SunsetBg,
  onBackground = DarkPlumTextPrimary,
  surface = SunsetSurface,
  onSurface = DarkPlumTextPrimary,
  surfaceVariant = SunsetSurfaceVariant,
  onSurfaceVariant = DarkPlumTextSecondary,
  outline = SunsetBorder,
  outlineVariant = SunsetBorder.copy(alpha = 0.5f)
)

@Composable
fun MyApplicationTheme(
  themeMode: AppThemeMode = AppThemeMode.LIGHT,
  content: @Composable () -> Unit
) {
  val colorScheme: ColorScheme = when (themeMode) {
    AppThemeMode.LIGHT -> LightColorScheme
    AppThemeMode.DARK -> DarkColorScheme
    AppThemeMode.SUNSET -> SunsetColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
