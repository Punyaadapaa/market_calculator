package com.example.marketcalculator.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════
// Palet warna dark theme — GitHub-style dark
// Lebih netral, mata nyaman, modern minimalis
// ═══════════════════════════════════════════════

// Background & Surface — Gradasi gelap netral (bukan navy kebiruan)
val DarkBackground   = Color(0xFF0D1117)   // Background utama, hampir hitam
val DarkSurface      = Color(0xFF161B22)   // Card & container
val DarkSurfaceLow   = Color(0xFF0D1117)   // Bagian paling dalam
val DarkSurfaceHigh  = Color(0xFF21262D)   // Elevated card
val DarkSurfaceBright = Color(0xFF30363D)  // Hover / pressed state
val DarkSurfaceLowest = Color(0xFF010409)  // Backdrop

// Border & Divider
val DarkOutline        = Color(0xFF484F58)  // Border utama
val DarkOutlineVariant = Color(0xFF30363D)  // Divider halus

// Teks
val DarkOnSurface        = Color(0xFFE6EDF3)  // Teks utama, putih lembut
val DarkOnSurfaceVariant = Color(0xFF8B949E)  // Teks sekunder, abu-abu

// ═══════════════════════════════════════════════
// Primary — Warm amber/orange, signature kalkulator seller
// Lebih muted dari Shopee orange asli biar gak norak di dark mode
// ═══════════════════════════════════════════════
val PrimaryOrange         = Color(0xFFFF8C42)  // Primary utama (warm amber)
val PrimaryOrangeLight    = Color(0xFFFFB77A)  // Primary light (untuk teks di atas gelap)
val OnPrimaryDark         = Color(0xFF3D1900)  // Teks di atas primary
val PrimaryContainerDark  = Color(0xFF5C2D00)  // Container primary

// ═══════════════════════════════════════════════
// Secondary — Emerald hijau, buat angka "penghasilan bersih"
// Warna hijau lebih intuitif buat profit daripada teal
// ═══════════════════════════════════════════════
val ProfitGreen    = Color(0xFF3FB950)  // Hijau profit (GitHub green)
val OnProfitGreen  = Color(0xFF0A2E10)  // Teks gelap di atas hijau
val ProfitGreenContainer = Color(0xFF12351B)     // Background panel penghasilan bersih
val OnProfitGreenContainer = Color(0xFF7EE787)   // Teks/angka di atas container hijau

// ═══════════════════════════════════════════════
// Error — Merah lembut, buat potongan & peringatan
// ═══════════════════════════════════════════════
val ErrorRed       = Color(0xFFF85149)  // Merah error (GitHub red)
val ErrorContainer = Color(0xFF3D1418)  // Container error
val OnErrorContainerDark = Color(0xFFFF9C94)  // Teks/angka di atas container error
val OnErrorDark    = Color(0xFF2A0608)  // Teks gelap di atas merah

// ═══════════════════════════════════════════════
// Tertiary — Biru muda, buat aksen info
// ═══════════════════════════════════════════════
val AccentBlue = Color(0xFF58A6FF)  // Link / highlight info

// ═══════════════════════════════════════════════
// Palet warna LIGHT theme
// Netral terang dengan aksen hangat yang sama, kontras WCAG AA
// ═══════════════════════════════════════════════

// Background & Surface
val LightBackground        = Color(0xFFFCFCFD)  // Latar utama
val LightSurface           = Color(0xFFFFFFFF)  // Card & container
val LightSurfaceLow        = Color(0xFFF6F8FA)  // Bagian dalam
val LightSurfaceHigh       = Color(0xFFF0F2F5)  // Elevated card
val LightSurfaceBright     = Color(0xFFE6E9EC)  // Hover / pressed
val LightSurfaceLowest     = Color(0xFFFFFFFF)  // Backdrop

// Border & Divider
val LightOutline           = Color(0xFFB9C0C8)  // Border utama
val LightOutlineVariant    = Color(0xFFDDE1E5)  // Divider halus

// Teks
val LightOnSurface         = Color(0xFF1B1F24)  // Teks utama, hampir hitam
val LightOnSurfaceVariant  = Color(0xFF57606A)  // Teks sekunder, abu gelap

// Primary — warm amber (lebih gelap agar kontras di latar terang)
val PrimaryOrangeLightTheme      = Color(0xFFB4530A)  // Primary teks/ikon di background
val PrimaryOrangeLightThemeCont  = Color(0xFFFFDCC2)  // Container primary
val OnPrimaryLightTheme          = Color(0xFFFFFFFF)  // Teks di atas primary
val OnPrimaryContainerLight      = Color(0xFF381100)  // Teks di atas container primary

// Secondary — hijau profit (lebih gelap)
val ProfitGreenLight             = Color(0xFF0E6B2E)  // Hijau profit (teks/ikon)
val OnProfitGreenLight           = Color(0xFFFFFFFF)  // Teks di atas hijau
val ProfitGreenContainerLight    = Color(0xFFC8F0CF)  // Container penghasilan bersih
val OnProfitContainerLight       = Color(0xFF04240C)  // Teks di atas container hijau

// Error — merah (lebih gelap)
val ErrorRedLight                = Color(0xFFB3211B)  // Merah error
val OnErrorLight                 = Color(0xFFFFFFFF)  // Teks di atas merah
val ErrorContainerLight          = Color(0xFFFFDAD6)  // Container error
val OnErrorContainerLight        = Color(0xFF410002)  // Teks di atas container error

// Tertiary — biru info (lebih gelap)
val AccentBlueLight              = Color(0xFF0A5BC4)  // Link / highlight info