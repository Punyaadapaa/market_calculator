package com.example.marketcalculator.data

import kotlinx.serialization.Serializable

/**
 * Satu catatan perhitungan yang disimpan ke riwayat.
 *
 * Menyimpan input + hasil pada saat dihitung, supaya bisa ditampilkan lengkap
 * dan dimuat ulang ke kalkulator kapan saja (tanpa menghitung ulang dari tarif
 * yang mungkin sudah berubah).
 */
@Serializable
data class RiwayatEntri(
    /** Epoch millis saat entri dibuat (dipakai juga sebagai id unik). */
    val id: Long,
    val mode: String,                 // CalcMode.name
    val kategoriNama: String,         // KategoriFee.label

    // Input yang relevan (disimpan sebagai teks apa adanya)
    val hargaAwal: Long,
    val diskonPersen: Int,
    val promoXtraPersen: Double,
    val promoXtraAktif: Boolean,
    val pakaiShippingSaver: Boolean = false,

    // Ringkasan hasil saat dihitung
    val persenFee: Double,
    val commissionFee: Long,
    val prosesFee: Long,
    val promoXtraFee: Long,
    val shippingSaverFee: Long,
    val premiumFee: Long,
    val totalPotongan: Long,
    val penghasilanBersih: Long,
    val hargaWajibPasang: Long? = null,
    val hargaSetelahDiskon: Long? = null
)
