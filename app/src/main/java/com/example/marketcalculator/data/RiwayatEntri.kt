package com.example.marketcalculator.data

import kotlinx.serialization.Serializable

/**
 * One saved calculation.
 *
 * Stores the input and result at calculation time, so the entry can be shown
 * in full and reloaded into the calculator later even if rates change.
 */
@Serializable
data class RiwayatEntri(
    /** Creation time in epoch millis, also used as the unique id. */
    val id: Long,
    val mode: String,                 // CalcMode.name
    val kategoriNama: String,         // KategoriFee.label

    // Relevant input, stored as-is
    val hargaAwal: Long,
    val diskonPersen: Int,
    val promoXtraPersen: Double,
    val promoXtraAktif: Boolean,
    val pakaiShippingSaver: Boolean = false,

    // Result summary at calculation time
    val persenFee: Double,
    val commissionFee: Long,
    val prosesFee: Long,
    val promoXtraFee: Long,
    val shippingSaverFee: Long,
    val premiumFee: Long,
    val totalPotongan: Long,
    val penghasilanBersih: Long,
    val hargaWajibPasang: Long? = null,
    val hargaSetelahDiskon: Long? = null,
    /** Desired net income input, Target Bersih mode only. */
    val targetBersih: Long? = null
)
