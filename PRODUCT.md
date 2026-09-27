# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

- **Pengguna utama: pembuat app sendiri**, seorang seller Shopee yang ingin
  menghitung cepat berapa penghasilan bersih dari suatu harga jual — dipakai
  saat menentukan harga produk (sebelum pasang di Shopee), bukan saat survei.
- Sekaligus dipakai sebagai **proyek portofolio** untuk menunjukkan kualitas
  aplikasi Android (Kotlin + Jetpack Compose).

## Product Purpose

Menghitung potongan biaya Shopee dan penghasilan bersih seller dari sebuah
harga jual. Tujuan utama: **mencegah seller salah hitung / rugi** karena
menaksir biaya terlalu rendah.

Dua arah kalkulasi:
- **Harga Jual** — masukkan harga before, lihat penghasilan bersih.
- **Target Harga** — masukkan target penghasilan/uang diterima, lihat harga
  yang wajib dipasang.

## Positioning

Akurat mengikuti **struktur biaya Shopee yang nyata**: fee admin bertingkat per
kategori (9 tier, 2,50%–10,00%), biaya proses pesanan, Premium (selalu, 0,5%
dari harga), serta biaya opsional (Promo XTRA, Shipping Fee Saver). Sebagian
besar kalkulator fee lain hanya memakai satu persen kasar, sehingga hasilnya
meleset.

Fondasi rumus sudah diverifikasi terhadap data *Income Details* Shopee nyata
(selisih Rp0 pada kasus uji). Lihat `## Evidence on Hand`.

## Operating Context

- Dipakai lewat HP sendiri, biasanya **sambil mengatur harga produk di app
  Shopee** (dua app berdampingan) atau saat merencanakan harga jual.
- Offline sepenuhnya saat ini — tidak perlu login, tidak ada data akun Shopee.
- Angka biaya Shopee berubah sewaktu-waktu (terakhir: perubahan tarif
  per 1 Januari 2026); tier fee di-update manual di kode saat ada perubahan.

## Capabilities and Constraints

- Kalkulasi fee admin per kategori (9 tier) dari harga **before**, karena
  voucher diskon yang ditanggung Shopee tidak mengurangi harga dari sisi seller.
- Biaya tambahan: Promo XTRA (persen, bisa di-toggle), Shipping Fee Saver
  (toggle ON/OFF, nominal tetap Rp350), dan **Premium yang selalu dihitung**
  otomatis 0,5% dari harga (tanpa input user).
- Auto-format Rupiah, kalkulasi real-time, validasi input, reset.
- Input disimpan ke `SavedStateHandle` (tahan proses death).
- **Riwayat perhitungan** persisten (DataStore): **tersimpan otomatis** tiap
  perubahan input (melewati duplikat berurutan), daftar expand/collapse, detail
  lengkap, muat ulang ke kalkulator, hapus satu/semua, maksimal 50 entri terbaru.
- Light **dan** dark theme + Dynamic Color (Material You, default aktif di
  Android 12+).
- MVVM (ViewModel + State), Jetpack Compose, Material 3.
- **Terbuka:** mungkin nanti ambil/update tarif fee dari internet; saat ini
  offline penuh dengan tarif hardcode di kode.

## Evidence on Hand

- **Data nyata Shopee** (`Income Details`): harga before Rp1.000.000, voucher
  25% (ditanggung Shopee), fee admin 8,25%, Promo XTRA 4,5%, Shipping Saver
  Rp350, Premium Rp5.000 → **Order Income Rp865.900**.
- **Premium terbukti = 0,5% dari harga** (bukan nominal tetap), diverifikasi
  dari 3 transaksi: 1.000.000→5.000, 1.100.000→5.500, 734.000→3.670 (cocok
  persis). Karena itu Premium dihitung otomatis, tanpa input user.
- Rumus di kode mereproduksi angka ini **persis (selisih Rp0)**, dibuktikan
  oleh unit test `CalculatorEngineTest` (semua lulus).
- Sumber tarif: seller.shopee.co.id/edu/article/26511 (via Kontan.co.id).

## Product Principles

1. **Akurasi mengalahkan kesederhanaan** — lebih baik ada beberapa field
   opsional daripada hasil yang meleset.
2. **Cegah rugi** — tampilkan rincian potongan transparan, bukan cuma angka akhir.
3. **Offline & privat** — tidak butuh akun, tidak mengirim data ke mana pun.
4. **Jujur soal sumber angka** — tarif yang bisa berubah ditandai, bukan
   disajikan sebagai kebenaran mutlak.

## Accessibility & Inclusion

- Target: Material 3 standar (touch target ≥48dp, ikon aksi punya
  contentDescription, pesan error diumumkan via live region).
- Light + dark theme, Dynamic Color opsional (default aktif Android 12+).
- Angka memakai tabular-nums agar stabil saat berubah.
