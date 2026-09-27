package com.example.marketcalculator.data

/**
 * 9 tier Biaya Administrasi Shopee (Penjual Non-Star, Star & Star+).
 *
 * Sumber RESMI: seller.shopee.co.id/edu/article/15965
 * ("Rincian Biaya Penjual Shopee per Kategori Produk", versi 30-07-2026).
 * Tarif final: 10,00% / 9,50% / 9,00% / 8,25% / 6,75% / 6,50% / 5,25% / 4,25% / 2,50%.
 *
 * Dasar perhitungan = harga SEBELUM diskon (harga before) saat voucher
 * ditanggung Shopee, karena voucher itu tidak mengurangi harga dari sisi
 * penjual. (Kalau voucher ditanggung PENJUAL, barulah dikurangi dulu.)
 *
 * Catatan: Shopee Mall punya skema terpisah (2,50%–11,70%). Tier di sini untuk
 * penjual reguler. Persen bisa berubah sewaktu-waktu — sesuaikan bila ada update.
 */
enum class KategoriFee(
    val label: String,
    val contoh: String,
    val persen: Double
) {
    KHUSUS(
        "Otomotif / Kendaraan",
        "Mobil, Sepeda Motor",
        2.50
    ),
    LOGAM_MULIA(
        "Logam Mulia & Perhiasan",
        "Emas, Perak, Berlian, Perhiasan Berharga",
        4.25
    ),
    ELEKTRONIK_HE(
        "Elektronik High-End",
        "Handphone, Desktop PC, Proyektor",
        5.25
    ),
    SUPLEMEN(
        "Kesehatan Bayi & Elektronik Besar",
        "Vitamin Bayi, Mesin Cuci, AC, Susu & Olahan",
        6.50
    ),
    SUSU_FORMULA(
        "Audio, Komputer & Bahan Makanan",
        "Earphone/Headset, Aksesoris Laptop, Susu Formula, Baking",
        6.75
    ),
    FASHION_BAWAH(
        "Fashion & Ibu Bayi",
        "Atasan, Mukena, E-Money, Alat Kecantikan, Perlengkapan Bayi",
        8.25
    ),
    TAS_JAM(
        "Tas, Jam, Sepatu & Audio",
        "Tas, Jam Tangan, Sepatu, Makanan Instan, Speaker",
        9.00
    ),
    KESEHATAN(
        "Kesehatan, Buku & Hobi",
        "Obat, Snack, Buku Bacaan, Kamera, Gaming, Hewan",
        9.50
    ),
    FASHION_ATAS(
        "Pakaian Muslim, Perkakas & Buku Majalah",
        "Pakaian Muslim, Kaos Kaki, Hand Sanitizer, Kelistrikan, Perkakas",
        10.00
    );

    /** Label + contoh produk untuk ditampilkan di dropdown. */
    val labelLengkap: String
        get() = "$label — ${formatPersen(persen)}"

    companion object {
        /** Tier default (paling netral di tengah range). */
        val default: KategoriFee = FASHION_BAWAH

        /** Ambil kategori dari label; fallback ke [default] bila tidak ketemu. */
        fun dariLabel(label: String): KategoriFee =
            entries.firstOrNull { it.label == label } ?: default
    }
}

/** Format persen ala Indonesia: 8.25 -> "8,25%; 10.00 -> "10%" */
internal fun formatPersen(persen: Double): String {
    val teks = if (persen % 1.0 == 0.0) {
        persen.toInt().toString()
    } else {
        persen.toString().replace('.', ',')
    }
    return "$teks%"
}
