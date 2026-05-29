package com.lyricpresence.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    background = Color.Black,
    surface = Color(0xFF121212),
    primary = Color.White,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun LyricPresenceTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
