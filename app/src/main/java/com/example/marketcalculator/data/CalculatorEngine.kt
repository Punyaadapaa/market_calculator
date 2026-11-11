package com.example.marketcalculator.data

import kotlin.math.roundToLong

/** Seller Order Processing Fee, flat per completed order. */
const val FLAT_FEE = 1250L

/** Promo XTRA default rate, fixed and not user editable. */
const val PROMO_XTRA_DEFAULT_PERCENT = 4.5

/** Promo XTRA fee cap per transaction. */
const val PROMO_XTRA_MAX_FEE = 60_000L

/**
 * PPh 22 / WHT 22, always deducted by Shopee at 0.5% of product price.
 * Shown as "Tax - WHT 22" in Shopee Income Details.
 * Verified against 4 real transactions (1,000,000 to 5,000; 1,100,000 to 5,500;
 * 734,000 to 3,670; 937,500 to 4,688).
 *
 * Field names stay `premiumFee`/`PREMIUM_PERCENT` to keep saved history JSON
 * compatible. The UI label uses "PPh 22".
 */
const val PREMIUM_PERCENT = 0.5

/** Flat Shipping Fee Saver fee per order, Rp350 per real Income Details. */
const val SHIPPING_SAVER_FEE = 350L

data class HasilKalkulasi(
    val hargaWajibPasang: Long? = null,       // set in Target Harga / Target Bersih modes
    val hargaSetelahDiskon: Long? = null,     // set when a discount applies
    val targetBersih: Long? = null,           // set in Target Bersih mode
    val diskonPersenDipakai: Int = 0,         // discount used for hargaSetelahDiskon
    val persenFeeAktif: Double = KategoriFee.default.persen,
    val commissionFee: Long = 0,              // category admin fee
    val prosesFee: Long = 0,                  // flat order processing fee
    val promoXtraFee: Long = 0,               // Promo XTRA service fee
    val shippingSaverFee: Long = 0,           // Shipping Fee Saver (toggle)
    val premiumFee: Long = 0,                 // PPh 22 / WHT 22, always 0.5%

    /** commissionFee + prosesFee */
    val platformFee: Long = 0,

    /** platformFee + promoXtra + shippingSaver + pph22 */
    val totalPotongan: Long = 0,

    val penghasilanBersih: Long = 0
)

object CalculatorEngine {

    /**
     * Harga Jual mode: seller already knows the listed price and wants the payout.
     *
     * Admin fee uses the listed price. This is correct because Shopee vouchers
     * are platform funded, so the seller's net base stays the listed price.
     *
     * Returns null when the discount is 100% or more.
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

        // Admin fee from listed price, vouchers are Shopee funded.
        val commissionFee = (hargaAwal * kategoriFee.persen / 100.0).roundToLong()
        val platformFee = commissionFee + FLAT_FEE

        // Promo XTRA: percent of listed price, capped at Rp60,000.
        val promoXtraFeeRaw = if (promoXtraPersen > 0) {
            (hargaAwal * promoXtraPersen / 100.0).roundToLong()
        } else {
            0L
        }
        val promoXtraFee = minOf(promoXtraFeeRaw, PROMO_XTRA_MAX_FEE)

        // PPh 22 / WHT 22, always 0.5% of listed price.
        val premiumFee = (hargaAwal * PREMIUM_PERCENT / 100.0).roundToLong()

        // Shipping Fee Saver, flat, only when enabled.
        val shippingSaver = if (pakaiShippingSaver) SHIPPING_SAVER_FEE else 0L

        val totalPotongan = platformFee + promoXtraFee + shippingSaver + premiumFee
        val netProfit = hargaAwal - totalPotongan

        // Buyer price after discount, display only.
        val hargaSetelahDiskon = if (diskonPersen > 0) {
            (hargaAwal * (100 - diskonPersen) / 100.0).roundToLong()
        } else {
            null
        }

        return HasilKalkulasi(
            hargaWajibPasang = null,
            hargaSetelahDiskon = hargaSetelahDiskon,
            diskonPersenDipakai = diskonPersen,
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
     * Target Harga mode: seller wants the buyer to pay a given amount after
     * discount, so the listed price is derived first, then fees are applied.
     *
     * Admin fee uses the listed price, not the target, for consistency with
     * Harga Jual mode. The discount is already handled by the division, so
     * netProfit is not reduced by it again.
     *
     * Returns null when the discount is 100% or more.
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

        val promoXtraFeeRaw = if (promoXtraPersen > 0) {
            (hargaWajibPasang * promoXtraPersen / 100.0).roundToLong()
        } else {
            0L
        }
        val promoXtraFee = minOf(promoXtraFeeRaw, PROMO_XTRA_MAX_FEE)
        val premiumFee = (hargaWajibPasang * PREMIUM_PERCENT / 100.0).roundToLong()
        val shippingSaver = if (pakaiShippingSaver) SHIPPING_SAVER_FEE else 0L

        val totalPotongan = platformFee + promoXtraFee + shippingSaver + premiumFee
        val netProfit = hargaWajibPasang - totalPotongan

        return HasilKalkulasi(
            hargaWajibPasang = hargaWajibPasang,
            hargaSetelahDiskon = null,
            diskonPersenDipakai = diskonPersen,
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
     * Target Bersih mode: seller sets the desired net income, then this returns
     * the listed price, the buyer price after discount, and the discount used.
     *
     * Net income rises with listed price, so binary search finds the smallest
     * listed price whose net is >= target. The rounding inside matches
     * [hitungDariHargaAwal] so both modes always agree.
     *
     * Rounding can overshoot by up to about Rp2, never below target.
     *
     * Returns null when the discount is 100% or more, or the target is out of range.
     */
    fun hitungDariTargetBersih(
        targetBersih: Long,
        diskonPersen: Int,
        kategoriFee: KategoriFee = KategoriFee.default,
        promoXtraPersen: Double = 0.0,
        pakaiShippingSaver: Boolean = false
    ): HasilKalkulasi? {
        if (targetBersih <= 0) return HasilKalkulasi()
        if (diskonPersen >= 100) return null

        // If total fee percent reaches 100%, no price can ever turn a profit.
        val totalPersen = kategoriFee.persen + promoXtraPersen + PREMIUM_PERCENT
        if (totalPersen >= 100.0) return null

        val shippingSaver = if (pakaiShippingSaver) SHIPPING_SAVER_FEE else 0L

        // Net income at one listed price. Must match hitungDariHargaAwal so both
        // modes give the same result.
        fun bersihPada(harga: Long): Long {
            val commission = (harga * kategoriFee.persen / 100.0).roundToLong()
            val promoRaw = if (promoXtraPersen > 0) {
                (harga * promoXtraPersen / 100.0).roundToLong()
            } else {
                0L
            }
            val promo = minOf(promoRaw, PROMO_XTRA_MAX_FEE)
            val pph = (harga * PREMIUM_PERCENT / 100.0).roundToLong()
            val potongan = commission + FLAT_FEE + promo + shippingSaver + pph
            return harga - potongan
        }

        // Grow an upper bound past the target, then binary search the smallest
        // listed price that meets it.
        var batasAtas = 1_000L
        while (bersihPada(batasAtas) < targetBersih) {
            batasAtas *= 2
            if (batasAtas > 100_000_000_000L) return null
        }
        var batasBawah = 0L
        while (batasBawah + 1 < batasAtas) {
            val tengah = (batasBawah + batasAtas) / 2
            if (bersihPada(tengah) >= targetBersih) batasAtas = tengah else batasBawah = tengah
        }

        val hargaWajibPasang = batasAtas
        val commissionFee = (hargaWajibPasang * kategoriFee.persen / 100.0).roundToLong()
        val platformFee = commissionFee + FLAT_FEE

        val promoXtraFeeRaw = if (promoXtraPersen > 0) {
            (hargaWajibPasang * promoXtraPersen / 100.0).roundToLong()
        } else {
            0L
        }
        val promoXtraFee = minOf(promoXtraFeeRaw, PROMO_XTRA_MAX_FEE)
        val premiumFee = (hargaWajibPasang * PREMIUM_PERCENT / 100.0).roundToLong()

        val totalPotongan = platformFee + promoXtraFee + shippingSaver + premiumFee
        val netProfit = hargaWajibPasang - totalPotongan

        // Buyer price, always set in this mode, including a 0% discount where
        // the buyer pays the listed price.
        val hargaSetelahDiskon =
            (hargaWajibPasang * (100 - diskonPersen) / 100.0).roundToLong()

        return HasilKalkulasi(
            hargaWajibPasang = hargaWajibPasang,
            hargaSetelahDiskon = hargaSetelahDiskon,
            targetBersih = targetBersih,
            diskonPersenDipakai = diskonPersen,
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
