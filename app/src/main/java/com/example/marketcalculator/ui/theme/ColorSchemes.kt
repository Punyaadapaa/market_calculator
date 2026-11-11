package com.example.marketcalculator.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

/** Dark theme palette — GitHub-style dark with warm amber accent. */
val KalkulatorDarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,

    secondary = ProfitTeal,
    onSecondary = OnProfitTeal,
    secondaryContainer = ProfitTealContainer,
    onSecondaryContainer = OnProfitTealContainer,

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

/** Light theme palette — neutral bright with the same warm accent, WCAG AA contrast. */
val KalkulatorLightColorScheme = lightColorScheme(
    primary = PrimaryOrangeLightTheme,
    onPrimary = OnPrimaryLightTheme,
    primaryContainer = PrimaryOrangeLightThemeCont,
    onPrimaryContainer = OnPrimaryContainerLight,

    secondary = ProfitTealLight,
    onSecondary = OnProfitTealLight,
    secondaryContainer = ProfitTealContainerLight,
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
