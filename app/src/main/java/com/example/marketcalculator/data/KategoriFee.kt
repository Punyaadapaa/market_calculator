package com.example.marketcalculator.data

/**
 * Shopee admin fee tiers (non-star, star, and star+ sellers).
 *
 * Official source: seller.shopee.co.id/edu/article/15965, article version 30-07-2026.
 * Final rates: 10.00 / 9.50 / 9.00 / 8.25 / 6.75 / 6.50 / 5.25 / 4.25 / 2.50 percent.
 *
 * Fees use the price before discount when the voucher is Shopee funded, since
 * that voucher does not reduce the seller's price. If the voucher is seller
 * funded, deduct it first.
 *
 * Shopee Mall uses a separate scheme (2.50 to 11.70 percent). These tiers are
 * for regular sellers. Rates can change, update when Shopee does.
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
    SUKU_CADANG(
        "Suku Cadang & Aksesoris Motor",
        "Knalpot, Spion, Busi, Filter, Variasi Motor, Sparepart",
        8.25
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

    /** Label with product examples for the dropdown. */
    val labelLengkap: String
        get() = "$label — ${formatPersen(persen)}"

    companion object {
        /** Default tier, the middle of the range. */
        val default: KategoriFee = FASHION_BAWAH

        /** Look up a category by label, falling back to [default]. */
        fun dariLabel(label: String): KategoriFee =
            entries.firstOrNull { it.label == label } ?: default
    }
}

/** Indonesian percent format: 8.25 becomes "8,25%", 10.00 becomes "10%". */
internal fun formatPersen(persen: Double): String {
    val teks = if (persen % 1.0 == 0.0) {
        persen.toInt().toString()
    } else {
        persen.toString().replace('.', ',')
    }
    return "$teks%"
}
