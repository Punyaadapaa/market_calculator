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
 * @param darkTheme    true = skema gelap. Default mengikuti setelan sistem.
 * @param dynamicColor true = pakai Dynamic Color (Material You) bila didukung
 *                     Android 12+ (API 31). DEFAULT false supaya warna brand
 *                     (Shopee Orange #EE4D2D) konsisten di semua perangkat.
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

    // Atur keterbacaan ikon status/navigation bar. Warna bar tidak di-set manual
    // (deprecated di AGP baru) — edge-to-edge + ikon kontras sudah cukup.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            // Di skema terang, ikon bar harus gelap (appearanceLight = true).
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
