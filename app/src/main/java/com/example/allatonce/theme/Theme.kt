package com.example.allatonce.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = AmberAccent,
    onPrimary = PanelBackground,
    secondary = SecondaryBlue,
    onSecondary = PanelBackground,
    tertiary = GreenActive,
    onTertiary = PanelBackground,
    error = AlertRed,
    onError = TextPrimary,
    background = PanelBackground,
    onBackground = TextPrimary,
    surface = PanelSurface,
    onSurface = TextPrimary,
    surfaceVariant = PanelBorder,
    onSurfaceVariant = TextDim,
    outline = PanelBorder,
    outlineVariant = BorderDormant
)

private val InstrumentShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

@Composable
fun AllAtOnceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        shapes = InstrumentShapes,
        content = content
    )
}
