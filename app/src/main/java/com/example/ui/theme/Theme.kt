package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ArSportsGlassColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = GlassWhiteStrong,
    onPrimaryContainer = Color.White,
    secondary = GlassSilver,
    onSecondary = Color.Black,
    secondaryContainer = GlassWhiteMedium,
    onSecondaryContainer = Color.White,
    tertiary = GlassIceBlue,
    onTertiary = Color.Black,
    tertiaryContainer = GlassWhiteSubtle,
    background = GlassMidnight,
    onBackground = Color.White,
    surface = GlassObsidian,
    onSurface = Color.White,
    surfaceVariant = GlassSurface,
    onSurfaceVariant = GlassTextSecondary,
    outline = GlassBorderSilver,
    outlineVariant = GlassBorderSubtle,
    error = GlassLiveRed,
    onError = Color.White
)

@Composable
fun ArSportsTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = ArSportsGlassColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = GlassMidnight.toArgb()
                window.navigationBarColor = GlassMidnight.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for backward compatibility
@Composable
fun StreamedTheme(content: @Composable () -> Unit) = ArSportsTheme(content)
