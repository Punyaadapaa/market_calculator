package com.example.marketcalculator

import com.example.marketcalculator.ui.util.formatRupiah
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatRupiahTest {

    @Test
    fun `nilai positif tanpa tanda`() {
        assertEquals("Rp1.500.000", formatRupiah(1_500_000))
    }

    @Test
    fun `nilai nol`() {
        assertEquals("Rp0", formatRupiah(0))
    }

    @Test
    fun `nilai positif dengan awalan minus pengurang`() {
        assertEquals("- Rp1.250", formatRupiah(1_250, tanda = true))
    }

    @Test
    fun `nilai negatif selalu -Rp tanpa dobel minus`() {
        assertEquals("-Rp5.000", formatRupiah(-5_000))
        assertEquals("-Rp5.000", formatRupiah(-5_000, tanda = true))
    }

    @Test
    fun `pemisah ribuan di posisi benar`() {
        assertEquals("Rp100", formatRupiah(100))
        assertEquals("Rp1.000", formatRupiah(1_000))
        assertEquals("Rp12.345.678", formatRupiah(12_345_678))
    }
}
