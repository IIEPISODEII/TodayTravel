package com.sb.todaytravel.feature.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

private val TodayTravelDarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    inversePrimary = Blue40,
    secondary = DarkBlue80,
    onSecondary = DarkBlue20,
    secondaryContainer = DarkBlue30,
    onSecondaryContainer = DarkBlue90,
    tertiary = Yellow80,
    onTertiary = Yellow20,
    tertiaryContainer = Yellow30,
    onTertiaryContainer = Yellow90,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = Grey10,
    onBackground = Grey90,
    surface = Grey10,
    onSurface = Grey80,
    inverseSurface = Grey90,
    inverseOnSurface = Grey20,
    surfaceVariant = BlueGrey30,
    onSurfaceVariant = BlueGrey80,
    outline = BlueGrey60
)

private val TodayTravelLightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    inversePrimary = Blue80,
    secondary = DarkBlue40,
    onSecondary = Color.White,
    secondaryContainer = DarkBlue90,
    onSecondaryContainer = DarkBlue10,
    tertiary = Yellow40,
    onTertiary = Color.White,
    tertiaryContainer = Yellow90,
    onTertiaryContainer = Yellow10,
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Grey99,
    onBackground = Grey10,
    surface = Grey99,
    onSurface = Grey10,
    inverseSurface = Grey20,
    inverseOnSurface = Grey95,
    surfaceVariant = BlueGrey90,
    onSurfaceVariant = BlueGrey30,
    outline = BlueGrey50
)

private val DefaultTextStyle = TextStyle(
    fontFamily = pretendard,
    fontWeight = FontWeight.Light
)

private val TodayTravelTypography = Typography(
    displayLarge = DefaultTextStyle,
    displayMedium = DefaultTextStyle,
    displaySmall = DefaultTextStyle,
    headlineLarge = DefaultTextStyle,
    headlineMedium = DefaultTextStyle,
    headlineSmall = DefaultTextStyle,
    titleLarge = DefaultTextStyle,
    titleMedium = DefaultTextStyle,
    titleSmall = DefaultTextStyle,
    bodyLarge = DefaultTextStyle,
    bodyMedium = DefaultTextStyle,
    bodySmall = DefaultTextStyle,
    labelLarge = DefaultTextStyle,
    labelMedium = DefaultTextStyle,
    labelSmall = DefaultTextStyle
)

@Composable
fun TodayTravelTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorsPalette = if (isDarkTheme) TodayTravelDarkColorScheme else TodayTravelLightColorScheme

    MaterialTheme(
        colorScheme = colorsPalette,
        content = content,
        typography = TodayTravelTypography
    )
}