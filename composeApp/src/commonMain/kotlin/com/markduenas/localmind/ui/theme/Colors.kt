package com.markduenas.localmind.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Synthwave palette derived from the app icon
// Primary: green from the floppy disk body
// Secondary: magenta/pink from the sunset circle
// Tertiary: cyan from the stripe accents

val Green40 = Color(0xFF2E7D32)
val Green80 = Color(0xFF81C784)
val Green90 = Color(0xFFC8E6C9)
val GreenContainer = Color(0xFFC8E6C9)
val OnGreenContainer = Color(0xFF002204)

val Magenta40 = Color(0xFFC2185B)
val Magenta80 = Color(0xFFF48FB1)
val Magenta90 = Color(0xFFFCE4EC)

val Cyan40 = Color(0xFF00838F)
val Cyan80 = Color(0xFF4DD0E1)
val Cyan90 = Color(0xFFB2EBF2)

val Neutral10 = Color(0xFF1A1A2E)
val Neutral20 = Color(0xFF2A2A3E)
val Neutral90 = Color(0xFFE3E2E6)
val Neutral95 = Color(0xFFF2F0F4)
val Neutral99 = Color(0xFFFDFBFF)

val Error40 = Color(0xFFBA1A1A)
val Error80 = Color(0xFFFFB4AB)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410002)

val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = GreenContainer,
    onPrimaryContainer = OnGreenContainer,
    secondary = Magenta40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE4EC),
    onSecondaryContainer = Color(0xFF3E0021),
    tertiary = Cyan40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2EBF2),
    onTertiaryContainer = Color(0xFF001F24),
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
    primary = Green80,
    onPrimary = Color(0xFF003A09),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Green90,
    secondary = Magenta80,
    onSecondary = Color(0xFF5C0033),
    secondaryContainer = Color(0xFF880E4F),
    onSecondaryContainer = Magenta90,
    tertiary = Cyan80,
    onTertiary = Color(0xFF003640),
    tertiaryContainer = Color(0xFF006064),
    onTertiaryContainer = Cyan90,
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
