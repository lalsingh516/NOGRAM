package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NogramBlue,
    onPrimary = NogramTextPrimaryDark,
    primaryContainer = NogramMessageMyDark,
    onPrimaryContainer = NogramTextPrimaryDark,
    secondary = NogramBlueDark,
    onSecondary = NogramTextPrimaryDark,
    background = NogramBackgroundDark,
    onBackground = NogramTextPrimaryDark,
    surface = NogramSurfaceDark,
    onSurface = NogramTextPrimaryDark,
    surfaceVariant = NogramSurfaceDarkVariant,
    onSurfaceVariant = NogramTextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = NogramBlue,
    onPrimary = NogramSurfaceLight,
    primaryContainer = NogramAlphaBlue,
    onPrimaryContainer = NogramBlue,
    secondary = NogramBlueDark,
    onSecondary = NogramSurfaceLight,
    background = NogramBackgroundLight,
    onBackground = NogramTextPrimaryLight,
    surface = NogramSurfaceLight,
    onSurface = NogramTextPrimaryLight,
    surfaceVariant = NogramBackgroundLight,
    onSurfaceVariant = NogramTextSecondaryLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors by default so NOGRAM identity is pristine and consistent!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
