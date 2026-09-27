---
name: Kalkulator Seller
description: Kalkulator fee & penghasilan bersih seller Shopee — Material 3, dark GitHub-style
colors:
  background: "#0D1117"
  surface: "#161B22"
  surface-high: "#21262D"
  surface-bright: "#30363D"
  surface-lowest: "#010409"
  outline: "#484F58"
  outline-variant: "#30363D"
  on-surface: "#E6EDF3"
  on-surface-variant: "#8B949E"
  primary: "#FF8C42"
  primary-light: "#FFB77A"
  primary-container: "#5C2D00"
  on-primary: "#3D1900"
  secondary: "#3FB950"
  on-secondary: "#0A2E10"
  tertiary: "#58A6FF"
  error: "#F85149"
  error-container: "#3D1418"
typography:
  display:
    fontSize: "28sp"
    fontWeight: 700
    lineHeight: "34sp"
    letterSpacing: "-0.5sp"
  headline:
    fontSize: "18sp"
    fontWeight: 600
    lineHeight: "26sp"
    letterSpacing: "-0.2sp"
  title:
    fontSize: "20sp"
    fontWeight: 600
    lineHeight: "28sp"
  body:
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: "20sp"
    letterSpacing: "0.15sp"
  label:
    fontSize: "12sp"
    fontWeight: 500
    lineHeight: "16sp"
    letterSpacing: "0.3sp"
rounded:
  sm: "10dp"
  md: "12dp"
  lg: "14dp"
  xl: "16dp"
spacing:
  xs: "4dp"
  sm: "8dp"
  md: "12dp"
  lg: "16dp"
  xl: "24dp"
components:
  card:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.xl}"
    padding: "16dp"
  field:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.md}"
  tab-selected:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.md}"
---

# Kalkulator Seller — Design System

> Status: **sistem visual saat ini (incumbent)**, direkam sebagai acuan.
> Beberapa pola di bawah ditandai sebagai calon perbaikan (lihat Do's and Don'ts).

## Overview

Aplikasi kalkulator Android (Jetpack Compose, Material 3), **dark-only**,
memakai palet netral ala GitHub-dark dengan satu aksen hangat (amber/orange)
plus hijau untuk angka profit. Cakupan: satu layar (single screen), form di
atas, ringkasan hasil di bawah, scroll vertikal.

Mode surface: **Operate** — pengguna menyelesaikan tugas (menghitung), jadi
keterbacaan, konsistensi, dan kepadatan informasi diutamakan di atas ekspresi.

## Colors

Palet neoral gelap, satu aksen per layar:

- **Background** `#0D1117` — latar utama.
- **Surface** `#161B22` — kartu/container.
- **Primary (amber)** `#FF8C42` — aksen utama: tab aktif, ikon, label penting.
- **Secondary (green)** `#3FB950` — khusus angka "penghasilan bersih".
- **Tertiary (blue)** `#58A6FF` — aksen info (harga yang dibayar pembeli).
- **Error (red)** `#F85149` — potongan biaya & pesan error.

Semua warna diambil dari color role Material 3 (`Color.kt` + `Theme.kt`).
Teks utama `#E6EDF3` (putih lembut), sekunder `#8B949E` (abu).

## Typography

Type scale Material 3 lengkap (`Type.kt`), ukuran dalam `sp`:

- `displayLarge` 28sp/700 — angka utama (harga, penghasilan).
- `headlineMedium` 18sp/600 — judul kartu.
- `titleLarge` 20sp/600 — judul halaman.
- `bodyMedium` 14sp — isi baris rincian.
- `labelSmall` 11sp — label eyebrow/kicker (uppercase).

## Layout

- Padding layar: 16dp horizontal, 8dp vertikal antar blok.
- Jarak antar elemen dalam kartu: 12dp; antar kartu: 14dp.
- Scroll vertikal tunggal; header di paling atas dengan `statusBarsPadding()`.
- Edge-to-edge aktif (`enableEdgeToEdge()` di `MainActivity`).

## Elevation & Depth

- Kartu memakai **surface + border tipis** (0.5dp, `outlineVariant`).
- Beberapa panel penting memakai **border 1dp + background gradasi halus**
  (header, harga wajib pasang, penghasilan bersih) sebagai penanda elevasi.
- Belum memakai tonal surface level Material 3.

## Shapes

- Kartu: `RoundedCornerShape(16dp)`.
- Field/input: 12dp.
- Panel penanda: 14dp.
- Tab pill: 11–14dp. Ikon header: lingkaran (CircleShape).

## Components

- **ModeSelector** — dua tab pill (Harga Jual / Target Harga), tab aktif
  berbackground primary lembut.
- **FormCard** — kartu dengan judul + subtitle + divider + isi.
- **RupiahField** — OutlinedTextField dengan prefix "Rp ", auto `ThousandsSeparatorTransformation`.
- **KategoriDropdown** — ExposedDropdownMenuBox untuk memilih kategori fee.
- **RingkasanCard** — rincian potongan (DetailRow per komponen) + panel
  penghasilan bersih + tombol Reset.
- **InfoFooter** — catatan kecil tentang dasar perhitungan fee.

## Do's and Don'ts

**Do:**
- Pakai color role Material 3, jangan hex mentah di komponen.
- Satu aksen (primary) per layar; hijau khusus profit; merah khusus potongan.
- Pertahankan format Rupiah dan real-time calculation.

**Don't (calon perbaikan — akan dirapikan):**
- **Gradient** dipakai di 4 tempat; baseline UI menyarankan hindari gradient
  dekoratif. → ganti surface tonal solid.
- **Card-in-card**: kotak bergradasi bersarang di dalam Card. → ratakan.
- **Eyebrow/kicker** uppercase ("RINCIAN POTONGAN", dst) di atas konten.
- Angka **belum tabular-nums** → angka bergoyang saat nilai berubah.
- Icon dekoratif `contentDescription = null` (a11y).
- `statusBarColor`/`navigationBarColor` manual (deprecated di AGP baru).
