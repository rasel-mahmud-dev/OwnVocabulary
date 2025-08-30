package com.rs.ownvocabulary.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


// Primary Green Palette (Forest Green Base)
val ForestGreen10 = Color(0xFF0B1F0F)  // Deepest green-black
val ForestGreen20 = Color(0xFF163A1F)  // Deep forest
val ForestGreen30 = Color(0xFF215530)  // Rich forest
val ForestGreen40 = Color(0xFF2C7041)  // Medium forest
val ForestGreen50 = Color(0xFF378B52)  // Vibrant forest
val ForestGreen60 = Color(0xFF52A066)  // Bright forest
val ForestGreen70 = Color(0xFF6DB479)  // Light forest
val ForestGreen80 = Color(0xFF88C78C)  // Pale forest
val ForestGreen90 = Color(0xFFA3DA9F)  // Very light forest
val ForestGreen95 = Color(0xFFD1EDCF)  // Mint cream
val ForestGreen99 = Color(0xFFF6FDF5)  // Almost white green

// Secondary Palette (Sage Green)
val SageGreen10 = Color(0xFF0F1F15)
val SageGreen20 = Color(0xFF1E3A28)
val SageGreen30 = Color(0xFF2D553B)
val SageGreen40 = Color(0xFF3C704E)
val SageGreen50 = Color(0xFF4B8B61)
val SageGreen60 = Color(0xFF66A074)
val SageGreen70 = Color(0xFF81B487)
val SageGreen80 = Color(0xFF9CC79A)
val SageGreen90 = Color(0xFFB7DAAD)
val SageGreen95 = Color(0xFFDBEDD6)

// Tertiary Palette (Teal Green)
val TealGreen10 = Color(0xFF0A1F1C)
val TealGreen20 = Color(0xFF153A35)
val TealGreen30 = Color(0xFF20554E)
val TealGreen40 = Color(0xFF2B7067)
val TealGreen50 = Color(0xFF368B80)
val TealGreen60 = Color(0xFF51A094)
val TealGreen70 = Color(0xFF6CB4A8)
val TealGreen80 = Color(0xFF87C7BC)
val TealGreen90 = Color(0xFFA2DAD0)
val TealGreen95 = Color(0xFFD0ECE7)

// Neutral Palette (Cool Gray-Green)
val NeutralGreen10 = Color(0xFF121614)
val NeutralGreen20 = Color(0xFF272B28)
val NeutralGreen30 = Color(0xFF3C403C)
val NeutralGreen40 = Color(0xFF515550)
val NeutralGreen50 = Color(0xFF676A64)
val NeutralGreen60 = Color(0xFF7F827C)
val NeutralGreen70 = Color(0xFF989B94)
val NeutralGreen80 = Color(0xFFB1B4AC)
val NeutralGreen90 = Color(0xFFCACDC4)
val NeutralGreen95 = Color(0xFFE5E7DC)
val NeutralGreen99 = Color(0xFFF8FAF4)

// Accent Colors
val EmberOrange = Color(0xFFFF7043)     // Warm accent
val DeepAmber = Color(0xFFFFB74D)       // Secondary accent
val CoolBlue = Color(0xFF42A5F5)        // Cool accent
val WarmRed = Color(0xFFEF5350)         // Error/warning

// Enhanced Dark Color Scheme
private val EnhancedDarkColorScheme = darkColorScheme(
    // Primary colors - Main brand color
    primary = ForestGreen70,              // Bright, accessible green
    onPrimary = ForestGreen10,            // Dark text on primary
    primaryContainer = ForestGreen30,      // Subtle primary container
    onPrimaryContainer = ForestGreen90,    // Light text on container

    // Secondary colors - Supporting elements
    secondary = SageGreen60,              // Muted secondary
    onSecondary = SageGreen10,            // Dark text on secondary
    secondaryContainer = SageGreen20,      // Dark secondary container
    onSecondaryContainer = SageGreen90,    // Light text on container

    // Tertiary colors - Accent elements
    tertiary = TealGreen60,               // Teal accent
    onTertiary = TealGreen10,             // Dark text on tertiary
    tertiaryContainer = TealGreen20,       // Dark tertiary container
    onTertiaryContainer = TealGreen90,     // Light text on container

    // Background colors
    background = NeutralGreen10,          // Deep dark background
    onBackground = NeutralGreen90,        // Light text on background

    // Surface colors
    surface = NeutralGreen10,             // Card/component surfaces
    onSurface = NeutralGreen90,           // Text on surfaces
    surfaceVariant = NeutralGreen20,      // Alternative surface
    onSurfaceVariant = NeutralGreen80,    // Text on surface variant

    // Container surfaces
    surfaceContainer = NeutralGreen20,     // Default containers
    surfaceContainerHigh = NeutralGreen30, // Elevated containers
    surfaceContainerHighest = NeutralGreen40, // Highest elevation
    surfaceContainerLow = NeutralGreen10,  // Low elevation
    surfaceContainerLowest = ForestGreen10, // Lowest elevation with brand tint

    // Outline colors
    outline = ForestGreen50,              // Borders and dividers
    outlineVariant = NeutralGreen40,      // Subtle borders

    // Inverse colors for high contrast elements
    inverseSurface = NeutralGreen90,      // Light surface for contrast
    inverseOnSurface = NeutralGreen10,    // Dark text on inverse
    inversePrimary = ForestGreen40,       // Dark primary for light surfaces

    // Error colors
    error = WarmRed,                      // Error state
    onError = Color(0xFF690005),          // Text on error
    errorContainer = Color(0xFF93000A),   // Error container
    onErrorContainer = Color(0xFFFFDAD6), // Text on error container

    // Scrim for modal overlays
    scrim = Color(0xFF000000),            // Modal overlay

    // Surface tint for elevated surfaces
    surfaceTint = ForestGreen70           // Tint for elevated surfaces
)

private val EnhancedLightColorScheme = lightColorScheme(
    primary = ForestGreen60,
    onPrimary = Color.White,
    primaryContainer = ForestGreen95,
    onPrimaryContainer = ForestGreen10,

    secondary = SageGreen40,
    onSecondary = Color.White,
    secondaryContainer = SageGreen95,
    onSecondaryContainer = SageGreen10,

    tertiary = TealGreen40,
    onTertiary = Color.White,
    tertiaryContainer = TealGreen95,
    onTertiaryContainer = TealGreen10,

    background = NeutralGreen99,
    onBackground = NeutralGreen10,
    surface = NeutralGreen99,
    onSurface = NeutralGreen10,

    surfaceVariant = NeutralGreen95,
    onSurfaceVariant = NeutralGreen30,
    outline = ForestGreen50,
    outlineVariant = NeutralGreen80,

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun OwnVocabularyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Changed default to showcase custom theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> EnhancedDarkColorScheme  // Your beautiful custom dark theme
        else -> EnhancedLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
