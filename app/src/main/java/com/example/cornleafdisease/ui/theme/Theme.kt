package com.example.cornleafdisease.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    secondary = GreenDark,
    background = Color.White,
    surface = Color.White
)

@Composable
fun CornLeafTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}