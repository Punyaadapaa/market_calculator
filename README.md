# Market Calculator

Kalkulator **penghasilan bersih seller Shopee**. Masukkan harga atau target, aplikasi menghitung semua potongan Shopee secara transparan — tanpa internet, tanpa akun, tanpa permission.

![Platform](https://img.shields.io/badge/Android-24%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Offline](https://img.shields.io/badge/100%25-Offline-26AA99)

## Fitur

| Mode | Input | Output |
|---|---|---|
| **Harga Jual** | Harga pasang + diskon | Penghasilan bersih |
| **Target Harga** | Harga dibayar pembeli + diskon | Harga wajib pasang |
| **Target Bersih** | Penghasilan bersih yang diinginkan | Harga before, harga after, diskon |

- **Biaya admin bertingkat** — 9 tier (2,50%–10,00%) sesuai kategori produk.
- **Biaya proses pesanan** — Rp1.250 flat per pesanan.
- **Promo XTRA** — toggle, 4,5% dengan cap Rp60.000.
- **Shipping Fee Saver** — toggle, Rp350 flat.
- **PPh 22 / WHT 22** — selalu 0,5%.
- **Rincian potongan** transparan dan riwayat otomatis (maks. 50 entri).
- **Light & dark theme**, angka tabular, aksesibel (TalkBack, sentuh 48dp).

Rumus diverifikasi terhadap `Income Details` Shopee asli dengan selisih **Rp 0**.

## Teknologi

Kotlin · Jetpack Compose (Material 3) · ViewModel + StateFlow · DataStore + kotlinx.serialization · Gradle Kotlin DSL

## Build

```bash
git clone https://github.com/Punyaadapaa/market_calculator.git
cd market_calculator
./gradlew assembleDebug
```

Butuh JDK 21 dan Android SDK 36.

## Versi

**v1.1** (`versionCode 2`) — redesign UI sesuai design board + mode Target Bersih.

Nomor versi mengikuti progres setiap update: minor naik tiap rilis fitur, `versionCode` naik satu. Aplikasi belum dirilis di Play Store.
