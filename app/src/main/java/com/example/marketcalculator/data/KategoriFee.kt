package com.example.marketcalculator.data

/**
 * 9 tier Biaya Administrasi Shopee 2026 (Penjual Non-Star & Star/Star+).
 *
 * Sumber: seller.shopee.co.id/edu/article/26511 (via Kontan.co.id).
 * Dasar perhitungan: harga SEBELUM diskon (harga before), karena voucher yang
 * ditanggung Shopee tidak mengurangi harga produk dari sisi penjual.
 *
 * Catatan: Shopee Mall punya skema terpisah. Tier di sini berlaku untuk
 * penjual reguler. Persen bisa berubah sewaktu-waktu — sesuaikan bila ada update.
 */
enum class KategoriFee(
    val label: String,
    val persen: Double
) {
    KHUSUS(
        "E-Money / Tiket / Voucher",
        2.50
    ),
    LOGAM_MULIA(
        "Logam Mulia / Perhiasan Berharga",
        4.25
    ),
    ELEKTRONIK_HE(
        "Elektronik High-End",
        5.25
    ),
    SUPLEMEN(
        "Vitamin & Suplemen Bayi",
        6.50
    ),
    SUSU_FORMULA(
        "Susu Formula & Makanan Bayi",
        6.75
    ),
    FASHION_BAWAH(
        "Atasan / Mukena / Aksesoris",
        8.25
    ),
    TAS_JAM(
        "Tas / Jam / Perawatan Diri",
        9.00
    ),
    KESEHATAN(
        "Obat / Permen / Keamanan Bayi",
        9.50
    ),
    FASHION_ATAS(
        "Pakaian Muslim / Snack / Mainan",
        10.00
    );

    /** Contoh label singkat untuk ditampilkan: "Atasan / Mukena / Aksesoris — 8.25%" */
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
