package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DiscordBlurple,
    onPrimary = Color.White,
    primaryContainer = DiscordBlurpleDark,
    onPrimaryContainer = Color.White,
    secondary = DiscordGreen,
    onSecondary = Color.Black,
    secondaryContainer = DiscordGreenDark,
    onSecondaryContainer = Color.White,
    tertiary = DiscordYellow,
    onTertiary = Color.Black,
    error = DiscordRed,
    onError = Color.White,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = DiscordBlurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE1FD),
    onPrimaryContainer = DiscordBlurpleDark,
    secondary = DiscordGreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4F8DE),
    onSecondaryContainer = Color(0xFF0F441F),
    tertiary = DiscordFuchsia,
    onTertiary = Color.White,
    error = DiscordRed,
    onError = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceMuted,
    outline = LightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our polished Discord palette by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
