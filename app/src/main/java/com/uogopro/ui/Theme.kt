package com.uogopro.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GoProBlue = Color(0xFF00AEEF)
private val Surface = Color(0xFF101214)
private val SurfaceHigh = Color(0xFF1B1F24)
private val RecordingRed = Color(0xFFFF3B30)

private val colors: ColorScheme = darkColorScheme(
    primary = GoProBlue,
    secondary = Color(0xFF8CC63F),
    tertiary = RecordingRed,
    background = Surface,
    surface = Surface,
    surfaceContainer = SurfaceHigh,
    surfaceContainerHigh = Color(0xFF252B31),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFE8EAED),
    onSurface = Color(0xFFE8EAED),
)

@Composable
fun UoGoProTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}
