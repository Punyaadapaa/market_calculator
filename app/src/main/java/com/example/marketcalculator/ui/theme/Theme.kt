package com.example.marketcalculator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * @param darkTheme    true for the dark scheme, defaults to the system setting.
 * @param dynamicColor true to use Material You dynamic color on Android 12+
 *                     (API 31). Defaults to false so the brand orange stays
 *                     consistent on every device.
 */
@Composable
fun KalkulatorSellerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> KalkulatorDarkColorScheme
        else -> KalkulatorLightColorScheme
    }

    // Keep status and navigation bar icons readable. Bar colors are not set
    // manually (deprecated in newer AGP); edge-to-edge plus contrast icons is enough.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            // Light scheme needs dark bar icons (appearanceLight = true).
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KalkulatorTypography,
        content = content
    )
}
