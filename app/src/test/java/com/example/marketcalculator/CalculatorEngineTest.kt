package com.example.marketcalculator

import com.example.marketcalculator.data.CalculatorEngine
import com.example.marketcalculator.data.KategoriFee
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit test untuk [CalculatorEngine].
 *
 * Test paling penting: mereproduksi transaksi Shopee nyata (Income Details)
 * dengan harga before Rp1.000.000, fee kategori 8,25%, Promo XTRA 4,5%,
 * Shipping Saver Rp350, Premium Rp5.000 -> Order Income Rp865.900.
 */
class CalculatorEngineTest {

    // ── A. Verifikasi data Shopee nyata (PRIORITAS #1) ──────────────────────

    @Test
    fun `transaksi nyata Shopee - cocok persis Rp865900`() {
        val hasil = CalculatorEngine.hitungDariHargaAwal(
            hargaAwal = 1_000_000,
            diskonPersen = 25,
            kategoriFee = KategoriFee.FASHION_BAWAH, // 8.25%
            promoXtraPersen = 4.5,
            shippingSaver = 350,
            premium = 5_000
        )!!

        assertEquals(82_500, hasil.commissionFee)   // Commission Fee
        assertEquals(1_250, hasil.prosesFee)        // Seller Order Processing Fee
        assertEquals(83_750, hasil.platformFee)     // Platform Fee
        assertEquals(45_000, hasil.promoXtraFee)    // Service Fee Promo XTRA
        assertEquals(5_350, hasil.shippingSaverFee + hasil.premiumFee) // Other Fees
        assertEquals(134_100, hasil.totalPotongan)
        assertEquals(865_900, hasil.penghasilanBersih) // Order Income Shopee
    }

    // ── B. Skenario dasar ──────────────────────────────────────────────────

    @Test
    fun `harga 0 - hasil kosong dan konsisten`() {
        val hasil = CalculatorEngine.hitungDariHargaAwal(
            hargaAwal = 0,
            diskonPersen = 0,
            kategoriFee = KategoriFee.FASHION_BAWAH
        )!!

        assertEquals(0L, hasil.penghasilanBersih)
        assertEquals(0L, hasil.commissionFee)
        // Bug N2: total potongan HARUS konsisten (tidak nampil 1250 sendirian).
        assertEquals(0L, hasil.prosesFee)
        assertEquals(0L, hasil.totalPotongan)
    }

    @Test
    fun `diskon 100 persen - return null`() {
        val hasil = CalculatorEngine.hitungDariHargaAwal(
            hargaAwal = 100_000,
            diskonPersen = 100,
            kategoriFee = KategoriFee.FASHION_BAWAH
        )
        assertNull(hasil)
    }

    @Test
    fun `diskon 101 persen - return null`() {
        val hasil = CalculatorEngine.hitungDariHargaAwal(
            hargaAwal = 100_000,
            diskonPersen = 101,
            kategoriFee = KategoriFee.FASHION_BAWAH
        )
        assertNull(hasil)
    }

    @Test
    fun `dasar - 100rb kategori 10 persen tanpa biaya tambahan`() {
        val hasil = CalculatorEngine.hitungDariHargaAwal(
            hargaAwal = 100_000,
            diskonPersen = 0,
            kategoriFee = KategoriFee.FASHION_ATAS, // 10%
            promoXtraPersen = 0.0
        )!!

        assertEquals(10_000, hasil.commissionFee)
        assertEquals(1_250, hasil.prosesFee)
        assertEquals(11_250, hasil.platformFee)
        assertEquals(0, hasil.promoXtraFee)
        assertEquals(11_250, hasil.totalPotongan)
        assertEquals(88_750, hasil.penghasilanBersih)
    }

    // ── C. Semua 9 tier fee ────────────────────────────────────────────────

    @Test
    fun `semua tier fee dihitung benar dari harga 1 juta`() {
        val harga = 1_000_000L
        val expected = mapOf(
            KategoriFee.KHUSUS to 25_000L,          // 2.50%
            KategoriFee.LOGAM_MULIA to 42_500L,     // 4.25%
            KategoriFee.ELEKTRONIK_HE to 52_500L,   // 5.25%
            KategoriFee.SUPLEMEN to 65_000L,        // 6.50%
            KategoriFee.SUSU_FORMULA to 67_500L,    // 6.75%
            KategoriFee.FASHION_BAWAH to 82_500L,   // 8.25%
            KategoriFee.TAS_JAM to 90_000L,         // 9.00%
            KategoriFee.KESEHATAN to 95_000L,       // 9.50%
            KategoriFee.FASHION_ATAS to 100_000L    // 10.00%
        )

        for ((kategori, expectedFee) in expected) {
            val hasil = CalculatorEngine.hitungDariHargaAwal(
                hargaAwal = harga,
                diskonPersen = 0,
                kategoriFee = kategori,
                promoXtraPersen = 0.0
            )!!
            assertEquals(
                "Gagal di kategori $kategori",
                expectedFee,
                hasil.commissionFee
            )
        }
    }

    // ── D. Biaya tambahan opsional ─────────────────────────────────────────

    @Test
    fun `biaya tambahan opsional menambah total potongan`() {
        val base = CalculatorEngine.hitungDariHargaAwal(
            hargaAwal = 100_000,
            diskonPersen = 0,
            kategoriFee = KategoriFee.FASHION_BAWAH,
            promoXtraPersen = 0.0
        )!!
        val withExtra = CalculatorEngine.hitungDariHargaAwal(
            hargaAwal = 100_000,
            diskonPersen = 0,
            kategoriFee = KategoriFee.FASHION_BAWAH,
            promoXtraPersen = 0.0,
            shippingSaver = 350,
            premium = 5_000
        )!!

        assertEquals(base.totalPotongan + 5_350, withExtra.totalPotongan)
        assertEquals(base.penghasilanBersih - 5_350, withExtra.penghasilanBersih)
    }

    // ── E. Mode Target Harga ───────────────────────────────────────────────

    @Test
    fun `target harga 750rb diskon 25 persen - harga wajib pasang 1jt`() {
        val hasil = CalculatorEngine.hitungDariTargetHarga(
            targetHarga = 750_000,
            diskonPersen = 25,
            kategoriFee = KategoriFee.FASHION_BAWAH,
            promoXtraPersen = 0.0
        )!!

        assertEquals(1_000_000L, hasil.hargaWajibPasang)
        assertEquals(82_500L, hasil.commissionFee)
        // net = 1.000.000 - (82.500 + 1.250) = 916.250
        assertEquals(916_250L, hasil.penghasilanBersih)
    }

    @Test
    fun `target harga 0 - hargaWajibPasang 0`() {
        val hasil = CalculatorEngine.hitungDariTargetHarga(
            targetHarga = 0,
            diskonPersen = 25,
            kategoriFee = KategoriFee.FASHION_BAWAH
        )!!
        assertEquals(0L, hasil.hargaWajibPasang)
    }

    // ── F. Edge & invariant ────────────────────────────────────────────────

    @Test
    fun `hargaAwal 1jt diskon 25 persen - hargaSetelahDiskon 750rb`() {
        val hasil = CalculatorEngine.hitungDariHargaAwal(
            hargaAwal = 1_000_000,
            diskonPersen = 25,
            kategoriFee = KategoriFee.FASHION_BAWAH,
            promoXtraPersen = 0.0
        )!!
        assertEquals(750_000L, hasil.hargaSetelahDiskon)
    }

    @Test
    fun `tanpa diskon - hargaSetelahDiskon null`() {
        val hasil = CalculatorEngine.hitungDariHargaAwal(
            hargaAwal = 100_000,
            diskonPersen = 0,
            kategoriFee = KategoriFee.FASHION_BAWAH,
            promoXtraPersen = 0.0
        )!!
        assertNull(hasil.hargaSetelahDiskon)
    }

    @Test
    fun `pembulatan - 33333 x 8_25 persen jadi 2750`() {
        // 33.333 x 8.25% = 2749.9725 -> 2750
        val hasil = CalculatorEngine.hitungDariHargaAwal(
            hargaAwal = 33_333,
            diskonPersen = 0,
            kategoriFee = KategoriFee.FASHION_BAWAH,
            promoXtraPersen = 0.0
        )!!
        assertEquals(2_750, hasil.commissionFee)
    }

    @Test
    fun `invariant - penghasilanBersih selalu hargaAwal dikurang totalPotongan`() {
        val harga = 456_789L
        val hasil = CalculatorEngine.hitungDariHargaAwal(
            hargaAwal = harga,
            diskonPersen = 30,
            kategoriFee = KategoriFee.TAS_JAM,
            promoXtraPersen = 4.5,
            shippingSaver = 350,
            premium = 5_000
        )!!
        assertEquals(harga - hasil.totalPotongan, hasil.penghasilanBersih)
    }
}
