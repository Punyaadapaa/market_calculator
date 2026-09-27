package com.example.marketcalculator.ui.util

/**
 * Format angka jadi Rupiah dengan pemisah ribuan titik.
 *
 * Satu-satunya sumber format Rupiah di app (menghindari duplikasi algoritma
 * pemisah ribuan di beberapa tempat).
 *
 * @param amount nilai rupiah (boleh negatif)
 * @param tanda  true = beri awalan "- " untuk nilai positif (dipakai di baris
 *               rincian potongan supaya jelas ini pengurang). Nilai negatif
 *               selalu tampil "-Rp...".
 *
 * Contoh:
 *   formatRupiah(1500000)            -> "Rp1.500.000"
 *   formatRupiah(1250, tanda = true) -> "- Rp1.250"
 *   formatRupiah(-5000)              -> "-Rp5.000"
 */
fun formatRupiah(amount: Long, tanda: Boolean = false): String {
    val teks = amount.toString().removePrefix("-")
    val sb = StringBuilder()
    for ((index, char) in teks.reversed().withIndex()) {
        if (index != 0 && index % 3 == 0) sb.append('.')
        sb.append(char)
    }
    val nominal = sb.reverse().toString()
    return when {
        amount < 0 -> "-Rp$nominal"
        tanda -> "- Rp$nominal"
        else -> "Rp$nominal"
    }
}
