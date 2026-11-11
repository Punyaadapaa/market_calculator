package com.example.marketcalculator.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Converts epoch millis into a short relative time in Indonesian,
 * e.g. "Baru saja", "5 menit lalu", "3 jam lalu", "2 hari lalu".
 * Anything older than 7 days shows as a date, e.g. "12 Feb 2026".
 */
fun waktuRelatif(epochMillis: Long, sekarang: Long = System.currentTimeMillis()): String {
    val selisih = sekarang - epochMillis
    if (selisih < 0) return "Baru saja"

    val menit = TimeUnit.MILLISECONDS.toMinutes(selisih)
    val jam = TimeUnit.MILLISECONDS.toHours(selisih)
    val hari = TimeUnit.MILLISECONDS.toDays(selisih)

    return when {
        menit < 1 -> "Baru saja"
        menit < 60 -> "$menit menit lalu"
        jam < 24 -> "$jam jam lalu"
        hari == 1L -> "Kemarin"
        hari < 7 -> "$hari hari lalu"
        else -> SimpleDateFormat("d MMM yyyy", Locale("id", "ID")).format(Date(epochMillis))
    }
}
