package com.example.marketcalculator.data

import kotlin.math.roundToLong

/** Biaya proses pesanan (Seller Order Processing Fee) — flat per pesanan selesai. */
const val FLAT_FEE = 1250L

/** Persen default Promo XTRA (opsional, bisa diubah user). */
const val PROMO_XTRA_DEFAULT_PERCENT = 4.5

data class HasilKalkulasi(
    val hargaWajibPasang: Long? = null,       // keisi kalau mode Target Harga
    val hargaSetelahDiskon: Long? = null,     // keisi kalau mode Harga Jual + diskon > 0
    val persenFeeAktif: Double = KategoriFee.default.persen,
    val commissionFee: Long = 0,              // fee admin kategori
    val prosesFee: Long = 0,                  // biaya proses pesanan (flat)
    val promoXtraFee: Long = 0,               // biaya layanan Promo XTRA
    val shippingSaverFee: Long = 0,           // Shipping Fee Saver (opsional)
    val premiumFee: Long = 0,                 // biaya Premium (opsional)

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
        shippingSaver: Long = 0L,
        premium: Long = 0L
    ): HasilKalkulasi? {
        if (hargaAwal <= 0) return HasilKalkulasi()
        if (diskonPersen >= 100) return null

        // Fee admin — dari harga before (voucher ditanggung Shopee).
        val commissionFee = (hargaAwal * kategoriFee.persen / 100.0).roundToLong()
        val platformFee = commissionFee + FLAT_FEE

        // Promo XTRA — persen dari harga before.
        val promoXtraFee = (hargaAwal * promoXtraPersen / 100.0).roundToLong()

        val totalPotongan = platformFee + promoXtraFee + shippingSaver + premium
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
            premiumFee = premium,
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
        shippingSaver: Long = 0L,
        premium: Long = 0L
    ): HasilKalkulasi? {
        if (targetHarga <= 0) return HasilKalkulasi(hargaWajibPasang = 0)
        if (diskonPersen >= 100) return null

        val multiplier = (100 - diskonPersen) / 100.0
        val hargaWajibPasang = (targetHarga / multiplier).roundToLong()

        val commissionFee = (hargaWajibPasang * kategoriFee.persen / 100.0).roundToLong()
        val platformFee = commissionFee + FLAT_FEE

        val promoXtraFee = (hargaWajibPasang * promoXtraPersen / 100.0).roundToLong()

        val totalPotongan = platformFee + promoXtraFee + shippingSaver + premium
        val netProfit = hargaWajibPasang - totalPotongan

        return HasilKalkulasi(
            hargaWajibPasang = hargaWajibPasang,
            hargaSetelahDiskon = null,
            persenFeeAktif = kategoriFee.persen,
            commissionFee = commissionFee,
            prosesFee = FLAT_FEE,
            promoXtraFee = promoXtraFee,
            shippingSaverFee = shippingSaver,
            premiumFee = premium,
            platformFee = platformFee,
            totalPotongan = totalPotongan,
            penghasilanBersih = netProfit
        )
    }
}
