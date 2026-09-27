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
// Primary — Shopee Orange (brand)
// #EE4D2D adalah warna resmi Shopee. Di dark mode dipakai versi terang
// (#FF6B4A) supaya kontras WCAG AA tetap aman di atas latar gelap.
// ═══════════════════════════════════════════════
val ShopeeOrange          = Color(0xFFEE4D2D)  // Brand utama (referensi)
val PrimaryOrange         = Color(0xFFFF6B4A)  // Primary di dark mode (lebih terang)
val OnPrimaryDark         = Color(0xFFFFFFFF)  // Teks di atas primary
val PrimaryContainerDark  = Color(0xFF5C1F0F)  // Container primary (dark)
val OnPrimaryContainerDark = Color(0xFFFFDAD3) // Teks di atas container primary

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
val LightBackground        = Color(0xFFF5F5F5)  // Latar utama (Background Gray, sesuai spec)
val LightSurface           = Color(0xFFFFFFFF)  // Card & container
val LightSurfaceLow        = Color(0xFFEFEFEF)  // Bagian dalam
val LightSurfaceHigh       = Color(0xFFE8E8E8)  // Elevated card
val LightSurfaceBright     = Color(0xFFE0E0E0)  // Hover / pressed
val LightSurfaceLowest     = Color(0xFFFFFFFF)  // Backdrop

// Border & Divider
val LightOutline           = Color(0xFFB0B0B0)  // Border utama
val LightOutlineVariant    = Color(0xFFDCDCDC)  // Divider halus

// Teks (sesuai spec: Text Primary #222222, Text Secondary #757575)
val LightOnSurface         = Color(0xFF222222)  // Teks utama, hampir hitam
val LightOnSurfaceVariant  = Color(0xFF6B6B6B)  // Teks sekunder, abu

// Primary — Shopee Orange (brand asli). Di light mode dipakai #EE4D2D.
val PrimaryOrangeLightTheme      = Color(0xFFEE4D2D)  // Primary (brand) — tombol/aksen
val PrimaryOrangeLightThemeCont  = Color(0xFFFFDAD3)  // Container primary
val OnPrimaryLightTheme          = Color(0xFFFFFFFF)  // Teks di atas primary
val OnPrimaryContainerLight      = Color(0xFF410000)  // Teks di atas container primary

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