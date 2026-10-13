package com.dlcode.chelasapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DarkGreen,
    onPrimary = White,
    background = LightBg,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    outline = Line,
    secondary = Amber,
    onSecondary = Ink
)

@Composable
fun ChelasAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography, // Will define this next
        content = content
    )
}
