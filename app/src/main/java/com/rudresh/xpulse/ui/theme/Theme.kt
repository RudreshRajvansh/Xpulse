package com.rudresh.xpulse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = DustyGrape,
    onPrimary = Seashell,
    secondary = LilacAsh,
    onSecondary = SpaceIndigo,
    tertiary = Coral,
    onTertiary = SpaceIndigo,
    background = Seashell,
    onBackground = SpaceIndigo,
    surface = Color.White,
    onSurface = SpaceIndigo,
    surfaceVariant = AlmondSilk,
    onSurfaceVariant = SpaceIndigo,
    outline = LilacAsh,
    error = DangerRed,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = AlmondSilk,
    onPrimary = SpaceIndigo,
    secondary = LilacAsh,
    onSecondary = SpaceIndigo,
    tertiary = Coral,
    onTertiary = SpaceIndigo,
    background = SpaceIndigo,
    onBackground = Seashell,
    surface = DustyGrape,
    onSurface = Seashell,
    surfaceVariant = DustyGrape,
    onSurfaceVariant = LilacAsh,
    outline = LilacAsh,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun XpulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
