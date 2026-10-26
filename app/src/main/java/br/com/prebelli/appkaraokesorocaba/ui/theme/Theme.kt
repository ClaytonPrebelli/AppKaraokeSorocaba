package br.com.prebelli.appkaraokesorocaba.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KaraokeColorScheme = lightColorScheme(
    primary = Brand700,
    onPrimary = Color.White,
    primaryContainer = Brand100,
    onPrimaryContainer = Brand700,
    secondary = Brand600,
    onSecondary = Color.White,
    secondaryContainer = Brand100,
    onSecondaryContainer = Brand700,
    background = Surface,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = Muted,
    outline = Border,
    outlineVariant = Border,
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun KaraokeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KaraokeColorScheme,
        typography = AppTypography,
        content = content
    )
}
