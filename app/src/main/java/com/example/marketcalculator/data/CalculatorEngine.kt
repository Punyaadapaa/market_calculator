package com.example.marketcalculator.data

import kotlin.math.roundToLong

private const val FEE_NORMAL_PERCENT = 12.75
private const val FEE_INSURANCE_PERCENT = 13.25
const val FLAT_FEE = 1250L

data class HasilKalkulasi(
    val hargaWajibPasang: Long? = null,      // keisi kalau mode Target Harga
    val hargaSetelahDiskon: Long? = null,     // keisi kalau mode Harga Jual + diskon > 0
    val persenFeeAktif: Double = FEE_NORMAL_PERCENT,
    val serviceFee: Long = 0,
    val prosesFee: Long = FLAT_FEE,
    val totalPotongan: Long = 0,
    val penghasilanBersih: Long = 0
)

object CalculatorEngine {

    /**
     * Mode "Harga Jual": seller udah tau mau pasang harga berapa (before),
     * tinggal dihitung berapa yang bakal cair.
     * Potongan dihitung langsung dari harga before, ini sesuai kesepakatan awal.
     *
     * Kalau ada diskon, harga setelah diskon (yang dibayar pembeli) juga dihitung
     * dan ditampilin di UI. Tapi potongan Shopee tetap dihitung dari before.
     *
     * Return null kalau diskon >= 100%.
     */
    fun hitungDariHargaAwal(
        hargaAwal: Long,
        diskonPersen: Int,
        pakaiAsuransi: Boolean
    ): HasilKalkulasi? {
        if (hargaAwal <= 0) return HasilKalkulasi()
        if (diskonPersen >= 100) return null

        val feePercent = if (pakaiAsuransi) FEE_INSURANCE_PERCENT else FEE_NORMAL_PERCENT
        val serviceFee = (hargaAwal * feePercent / 100.0).roundToLong()
        val totalPotongan = serviceFee + FLAT_FEE
        val netProfit = hargaAwal - totalPotongan

        // Hitung harga setelah diskon (what buyer pays), cuma info doang
        val hargaSetelahDiskon = if (diskonPersen > 0) {
            (hargaAwal * (100 - diskonPersen) / 100.0).roundToLong()
        } else {
            null
        }

        return HasilKalkulasi(
            hargaWajibPasang = null,
            hargaSetelahDiskon = hargaSetelahDiskon,
            persenFeeAktif = feePercent,
            serviceFee = serviceFee,
            prosesFee = FLAT_FEE,
            totalPotongan = totalPotongan,
            penghasilanBersih = netProfit
        )
    }

    /**
     * Mode "Target Harga": seller mau pembeli bayar sekian (after diskon),
     * jadi harus dicari dulu harga before-nya, baru dari situ dihitung potongan & net profit.
     *
     * PENTING: potongan admin dihitung dari hargaWajibPasang (before), BUKAN dari target (after).
     * Diskonnya udah otomatis "kepotong" lewat pembagian di bawah, jadi netProfit
     * TIDAK boleh dikurangi diskon sekali lagi. Ini bug yang ada di versi HTML kemarin.
     *
     * Return null kalau diskon >= 100%, soalnya secara matematis gak masuk akal (pembagi jadi 0 atau minus).
     */
    fun hitungDariTargetHarga(
        targetHarga: Long,
        diskonPersen: Int,
        pakaiAsuransi: Boolean
    ): HasilKalkulasi? {
        if (targetHarga <= 0) return HasilKalkulasi(hargaWajibPasang = 0)
        if (diskonPersen >= 100) return null

        val feePercent = if (pakaiAsuransi) FEE_INSURANCE_PERCENT else FEE_NORMAL_PERCENT
        val multiplier = (100 - diskonPersen) / 100.0
        val hargaWajibPasang = (targetHarga / multiplier).roundToLong()

        val serviceFee = (hargaWajibPasang * feePercent / 100.0).roundToLong()
        val totalPotongan = serviceFee + FLAT_FEE
        val netProfit = hargaWajibPasang - totalPotongan

        return HasilKalkulasi(
            hargaWajibPasang = hargaWajibPasang,
            hargaSetelahDiskon = null,
            persenFeeAktif = feePercent,
            serviceFee = serviceFee,
            prosesFee = FLAT_FEE,
            totalPotongan = totalPotongan,
            penghasilanBersih = netProfit
        )
    }
}
