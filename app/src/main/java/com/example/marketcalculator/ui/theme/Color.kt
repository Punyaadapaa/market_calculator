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

// ═══════════════════════════════════════════════
// Error — Merah lembut, buat potongan & peringatan
// ═══════════════════════════════════════════════
val ErrorRed       = Color(0xFFF85149)  // Merah error (GitHub red)
val ErrorContainer = Color(0xFF3D1418)  // Container error
val OnErrorDark    = Color(0xFF2A0608)  // Teks gelap di atas merah

// ═══════════════════════════════════════════════
// Tertiary — Biru muda, buat aksen info
// ═══════════════════════════════════════════════
val AccentBlue = Color(0xFF58A6FF)  // Link / highlight info