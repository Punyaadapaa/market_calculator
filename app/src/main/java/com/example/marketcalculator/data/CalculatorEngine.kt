package com.example.marketcalculator.data

import kotlin.math.roundToLong

/** Biaya proses pesanan (Seller Order Processing Fee) — flat per pesanan selesai. */
const val FLAT_FEE = 1250L

/** Persen default Promo XTRA (opsional, bisa diubah user). */
const val PROMO_XTRA_DEFAULT_PERCENT = 4.5

/**
 * Biaya Premium — SELALU dipotong Shopee, nilainya 0,50% dari harga produk.
 * Terverifikasi dari 3 transaksi Income Details nyata (1.000.000→5.000,
 * 1.100.000→5.500, 734.000→3.670).
 */
const val PREMIUM_PERCENT = 0.5

/**
 * Biaya tetap program Shipping Fee Saver (flat per pesanan).
 * Dari Income Details nyata: Rp350.
 */
const val SHIPPING_SAVER_FEE = 350L

data class HasilKalkulasi(
    val hargaWajibPasang: Long? = null,       // keisi kalau mode Target Harga
    val hargaSetelahDiskon: Long? = null,     // keisi kalau mode Harga Jual + diskon > 0
    val persenFeeAktif: Double = KategoriFee.default.persen,
    val commissionFee: Long = 0,              // fee admin kategori
    val prosesFee: Long = 0,                  // biaya proses pesanan (flat)
    val promoXtraFee: Long = 0,               // biaya layanan Promo XTRA
    val shippingSaverFee: Long = 0,           // Shipping Fee Saver (toggle)
    val premiumFee: Long = 0,                 // biaya Premium (selalu, 0,5%)

    /** commissionFee + prosesFee */
    val platformFee: Long = 0,

    /** platformFee + promoXtra + shippingSaver + premium */
    val totalPotongan: Long = 0,

    val penghasilanBersih: Long = 0
)

object CalculatorEngine {

    /**
     * Mode "Harga Jual": seller sudah tahu mau pasang harga berapa (harga before),
     * tinggal dihitung berapa yang bakal cair.
     *
     * Fee admin dihitung dari HARGA BEFORE. Ini benar karena voucher diskon
     * di Shopee ditanggung platform (bukan penjual), sehingga harga netto dari
     * sisi penjual tetap sama dengan harga before.
     *
     * Return null kalau diskon >= 100%.
     */
    fun hitungDariHargaAwal(
        hargaAwal: Long,
        diskonPersen: Int,
        kategoriFee: KategoriFee = KategoriFee.default,
        promoXtraPersen: Double = 0.0,
        pakaiShippingSaver: Boolean = false
    ): HasilKalkulasi? {
        if (hargaAwal <= 0) return HasilKalkulasi()
        if (diskonPersen >= 100) return null

        // Fee admin — dari harga before (voucher ditanggung Shopee).
        val commissionFee = (hargaAwal * kategoriFee.persen / 100.0).roundToLong()
        val platformFee = commissionFee + FLAT_FEE

        // Promo XTRA — persen dari harga before.
        val promoXtraFee = (hargaAwal * promoXtraPersen / 100.0).roundToLong()

        // Premium — selalu, 0,5% dari harga before.
        val premiumFee = (hargaAwal * PREMIUM_PERCENT / 100.0).roundToLong()

        // Shipping Fee Saver — flat, hanya kalau diaktifkan.
        val shippingSaver = if (pakaiShippingSaver) SHIPPING_SAVER_FEE else 0L

        val totalPotongan = platformFee + promoXtraFee + shippingSaver + premiumFee
        val netProfit = hargaAwal - totalPotongan

        // Harga setelah diskon (yang dibayar pembeli), cuma info display.
        val hargaSetelahDiskon = if (diskonPersen > 0) {
            (hargaAwal * (100 - diskonPersen) / 100.0).roundToLong()
        } else {
            null
        }

        return HasilKalkulasi(
            hargaWajibPasang = null,
            hargaSetelahDiskon = hargaSetelahDiskon,
            persenFeeAktif = kategoriFee.persen,
            commissionFee = commissionFee,
            prosesFee = FLAT_FEE,
            promoXtraFee = promoXtraFee,
            shippingSaverFee = shippingSaver,
            premiumFee = premiumFee,
            platformFee = platformFee,
            totalPotongan = totalPotongan,
            penghasilanBersih = netProfit
        )
    }

    /**
     * Mode "Target Harga": seller mau pembeli bayar sekian (after diskon),
     * jadi dicari dulu harga before-nya, baru dari situ dihitung potongan.
     *
     * PENTING: fee admin dihitung dari hargaWajibPasang (before), BUKAN dari target,
     * supaya konsisten dengan mode Harga Jual (voucher ditanggung Shopee). Diskon
     * sudah otomatis "kepotong" lewat pembagian, jadi netProfit TIDAK dikurangi
     * diskon sekali lagi.
     *
     * Return null kalau diskon >= 100%.
     */
    fun hitungDariTargetHarga(
        targetHarga: Long,
        diskonPersen: Int,
        kategoriFee: KategoriFee = KategoriFee.default,
        promoXtraPersen: Double = 0.0,
        pakaiShippingSaver: Boolean = false
    ): HasilKalkulasi? {
        if (targetHarga <= 0) return HasilKalkulasi(hargaWajibPasang = 0)
        if (diskonPersen >= 100) return null

        val multiplier = (100 - diskonPersen) / 100.0
        val hargaWajibPasang = (targetHarga / multiplier).roundToLong()

        val commissionFee = (hargaWajibPasang * kategoriFee.persen / 100.0).roundToLong()
        val platformFee = commissionFee + FLAT_FEE

        val promoXtraFee = (hargaWajibPasang * promoXtraPersen / 100.0).roundToLong()
        val premiumFee = (hargaWajibPasang * PREMIUM_PERCENT / 100.0).roundToLong()
        val shippingSaver = if (pakaiShippingSaver) SHIPPING_SAVER_FEE else 0L

        val totalPotongan = platformFee + promoXtraFee + shippingSaver + premiumFee
        val netProfit = hargaWajibPasang - totalPotongan

        return HasilKalkulasi(
            hargaWajibPasang = hargaWajibPasang,
            hargaSetelahDiskon = null,
            persenFeeAktif = kategoriFee.persen,
            commissionFee = commissionFee,
            prosesFee = FLAT_FEE,
            promoXtraFee = promoXtraFee,
            shippingSaverFee = shippingSaver,
            premiumFee = premiumFee,
            platformFee = platformFee,
            totalPotongan = totalPotongan,
            penghasilanBersih = netProfit
        )
    }
}
