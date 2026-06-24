package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryRed,
    secondary = AccentTeal,
    tertiary = SuccessGreen,
    background = BackgroundDark,
    surface = CardDark,
    onPrimary = TextWhite,
    onSecondary = BackgroundDark,
    onBackground = TextWhite,
    onSurface = TextWhite
  )

private val LightColorScheme = DarkColorScheme // Forced Dark Theme as specified by "Dark Premium Theme"

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme
  dynamicColor: Boolean = false, // Force brand colors
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
