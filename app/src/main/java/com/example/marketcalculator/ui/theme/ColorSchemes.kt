package com.example.marketcalculator.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

/**
 * Skema warna dark — netral ala GitHub-dark dengan aksen amber hangat.
 */
val KalkulatorDarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = PrimaryOrangeLight,

    secondary = ProfitGreen,
    onSecondary = OnProfitGreen,
    secondaryContainer = ProfitGreenContainer,
    onSecondaryContainer = OnProfitGreenContainer,

    tertiary = AccentBlue,

    background = DarkBackground,
    onBackground = DarkOnSurface,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkSurfaceLowest,
    surfaceContainerLow = DarkSurfaceLow,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = DarkSurfaceBright,

    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,

    error = ErrorRed,
    onError = OnErrorDark,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainerDark
)

/**
 * Skema warna light — netral terang dengan aksen hangat yang sama.
 * Semua pasangan teks/latar memenuhi kontras WCAG AA.
 */
val KalkulatorLightColorScheme = lightColorScheme(
    primary = PrimaryOrangeLightTheme,
    onPrimary = OnPrimaryLightTheme,
    primaryContainer = PrimaryOrangeLightThemeCont,
    onPrimaryContainer = OnPrimaryContainerLight,

    secondary = ProfitGreenLight,
    onSecondary = OnProfitGreenLight,
    secondaryContainer = ProfitGreenContainerLight,
    onSecondaryContainer = OnProfitContainerLight,

    tertiary = AccentBlueLight,

    background = LightBackground,
    onBackground = LightOnSurface,

    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceHigh,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightSurfaceLowest,
    surfaceContainerLow = LightSurfaceLow,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceHigh,
    surfaceContainerHighest = LightSurfaceBright,

    outline = LightOutline,
    outlineVariant = LightOutlineVariant,

    error = ErrorRedLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)
