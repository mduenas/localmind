package com.markduenas.localmind.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Warm productivity palette
val Amber40 = Color(0xFFAB6400)
val Amber80 = Color(0xFFFFB95E)
val Amber90 = Color(0xFFFFDDB3)
val AmberContainer = Color(0xFFFFDDB3)
val OnAmberContainer = Color(0xFF2A1800)

val Teal40 = Color(0xFF006B5E)
val Teal80 = Color(0xFF55DBCA)
val Teal90 = Color(0xFF73F8E6)

val Neutral10 = Color(0xFF1B1B1F)
val Neutral20 = Color(0xFF303034)
val Neutral90 = Color(0xFFE3E2E6)
val Neutral95 = Color(0xFFF2F0F4)
val Neutral99 = Color(0xFFFDFBFF)

val Error40 = Color(0xFFBA1A1A)
val Error80 = Color(0xFFFFB4AB)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410002)

val LightColorScheme = lightColorScheme(
    primary = Amber40,
    onPrimary = Color.White,
    primaryContainer = AmberContainer,
    onPrimaryContainer = OnAmberContainer,
    secondary = Teal40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB5F0E5),
    onSecondaryContainer = Color(0xFF00201B),
    tertiary = Color(0xFF6D5E0F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF8E287),
    onTertiaryContainer = Color(0xFF221B00),
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF7A757F),
    error = Error40,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
)

val DarkColorScheme = darkColorScheme(
    primary = Amber80,
    onPrimary = Color(0xFF4A2800),
    primaryContainer = Color(0xFF6A3C00),
    onPrimaryContainer = Amber90,
    secondary = Teal80,
    onSecondary = Color(0xFF003730),
    secondaryContainer = Color(0xFF005046),
    onSecondaryContainer = Teal90,
    tertiary = Color(0xFFDBC66E),
    onTertiary = Color(0xFF393000),
    tertiaryContainer = Color(0xFF524600),
    onTertiaryContainer = Color(0xFFF8E287),
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral20,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF948F99),
    error = Error80,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = ErrorContainer,
)
