package com.example.marketcalculator.ui.util

/**
 * Formats a number as Rupiah with dot thousand separators.
 *
 * Single source for Rupiah formatting in the app.
 *
 * @param amount Rupiah value, may be negative.
 * @param tanda  true adds a "- " prefix to positive values, used for deduction
 *               rows. Negative values always show as "-Rp...".
 *
 * Examples:
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
