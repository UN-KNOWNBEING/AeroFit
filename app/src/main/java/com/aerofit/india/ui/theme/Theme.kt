package com.aerofit.india.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AqiGood,
    secondary = AqiModerate,
    tertiary = AqiPoor,
    background = DarkBackground,
    surface = CardBackground,
)

private val LightColorScheme = lightColorScheme(
    primary = AqiGood,
    secondary = AqiModerate,
    tertiary = AqiPoor
)

@Composable
fun AeroFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
