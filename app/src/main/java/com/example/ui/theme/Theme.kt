package com.example.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// Safe Start uses an official high-contrast civic portal color scheme
private val LightColorScheme =
  lightColorScheme(
    primary = TnPrimary,
    onPrimary = Color.White,
    primaryContainer = TnPrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = TnSecondary,
    onSecondary = Color.White,
    secondaryContainer = TnSecondaryContainer,
    onSecondaryContainer = Color(0xFF382900),
    tertiary = TnTertiary,
    onTertiary = Color.White,
    tertiaryContainer = TnTertiaryContainer,
    onTertiaryContainer = Color(0xFFF1F5F9),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF334155),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    surfaceContainerHighest = Color(0xFFCBD5E1),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF94A3B8),
    error = TnError,
    onError = Color.White,
    errorContainer = TnErrorContainer,
    onErrorContainer = Color(0xFF7F1D1D),
  )

@Composable
fun safeStartTextFieldColors() = OutlinedTextFieldDefaults.colors(
  focusedTextColor = Color(0xFF0F172A),
  unfocusedTextColor = Color(0xFF0F172A),
  focusedContainerColor = Color.White,
  unfocusedContainerColor = Color.White,
  disabledContainerColor = Color(0xFFF8FAFC),
  focusedLabelColor = Color(0xFF003528),
  unfocusedLabelColor = Color(0xFF334155),
  focusedPlaceholderColor = Color(0xFF64748B),
  unfocusedPlaceholderColor = Color(0xFF64748B),
  focusedBorderColor = Color(0xFF003528),
  unfocusedBorderColor = Color(0xFF94A3B8),
  focusedLeadingIconColor = Color(0xFF003528),
  unfocusedLeadingIconColor = Color(0xFF475569),
  focusedTrailingIconColor = Color(0xFF003528),
  unfocusedTrailingIconColor = Color(0xFF475569),
  cursorColor = Color(0xFF003528)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalContentColor provides Color(0xFF0F172A)
  ) {
    MaterialTheme(
      colorScheme = LightColorScheme,
      typography = Typography,
      content = content
    )
  }
}
