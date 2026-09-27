package com.example.marketcalculator

import com.example.marketcalculator.ui.util.waktuRelatif
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class WaktuRelatifTest {

    private val now = 1_700_000_000_000L

    @Test
    fun `baru saja untuk selisih kurang dari 1 menit`() {
        assertEquals("Baru saja", waktuRelatif(now - 30_000, now))
    }

    @Test
    fun `menit dan jam`() {
        assertEquals("5 menit lalu", waktuRelatif(now - TimeUnit.MINUTES.toMillis(5), now))
        assertEquals("3 jam lalu", waktuRelatif(now - TimeUnit.HOURS.toMillis(3), now))
    }

    @Test
    fun `kemarin dan hari`() {
        assertEquals("Kemarin", waktuRelatif(now - TimeUnit.DAYS.toMillis(1), now))
        assertEquals("3 hari lalu", waktuRelatif(now - TimeUnit.DAYS.toMillis(3), now))
    }

    @Test
    fun `lebih dari seminggu pakai tanggal`() {
        val hasil = waktuRelatif(now - TimeUnit.DAYS.toMillis(30), now)
        // Bukan lagi format relatif.
        assertEquals(false, hasil.contains("lalu"))
    }

    @Test
    fun `waktu di masa depan dianggap baru saja`() {
        assertEquals("Baru saja", waktuRelatif(now + 10_000, now))
    }
}
