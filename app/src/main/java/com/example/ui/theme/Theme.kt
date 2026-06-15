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
    primary = CleanPrimaryDark,
    onPrimary = CleanOnPrimaryDark,
    primaryContainer = CleanPrimaryContainerDark,
    onPrimaryContainer = CleanOnPrimaryContainerDark,
    background = CleanBackgroundDark,
    onBackground = CleanOnBackgroundDark,
    surface = CleanSurfaceDark,
    onSurface = CleanOnSurfaceDark,
    surfaceVariant = CleanSurfaceVariantDark,
    onSurfaceVariant = CleanOnSurfaceVariantDark,
    error = CleanError,
    onError = CleanOnError
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CleanPrimary,
    onPrimary = CleanOnPrimary,
    primaryContainer = CleanPrimaryContainer,
    onPrimaryContainer = CleanOnPrimaryContainer,
    secondary = CleanSecondary,
    onSecondary = CleanOnSecondary,
    secondaryContainer = CleanSecondaryContainer,
    onSecondaryContainer = CleanOnSecondaryContainer,
    background = CleanBackground,
    onBackground = CleanOnBackground,
    surface = CleanSurface,
    onSurface = CleanOnSurface,
    surfaceVariant = CleanSurfaceVariant,
    onSurfaceVariant = CleanOnSurfaceVariant,
    outline = CleanOutline,
    outlineVariant = CleanOutlineVariant,
    error = CleanError,
    onError = CleanOnError
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set default dynamicColor to false to respect the specific "Clean Utility / Minimal" brand aesthetic
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
