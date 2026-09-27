package com.example.marketcalculator.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.marketcalculator.data.CalcMode
import com.example.marketcalculator.data.CalculatorEngine
import com.example.marketcalculator.data.HasilKalkulasi
import com.example.marketcalculator.data.KategoriFee
import com.example.marketcalculator.data.PROMO_XTRA_DEFAULT_PERCENT

data class CalculatorUiState(
    val mode: CalcMode = CalcMode.HARGA_JUAL,

    // Input mode Harga Jual
    val hargaAwal: String = "",
    val diskonPersenHargaJual: String = "0",
    val pakaiPromoXtraHargaJual: Boolean = false,

    // Input mode Target Harga
    val targetHarga: String = "",
    val diskonPersen: String = "25",
    val pakaiPromoXtraTarget: Boolean = false,

    // Kategori & biaya tambahan (dipakai kedua mode)
    val kategoriFee: KategoriFee = KategoriFee.default,
    val promoXtraPersen: String = PROMO_XTRA_DEFAULT_PERCENT.toString(),
    val shippingSaver: String = "",
    val premium: String = "",

    val hasil: HasilKalkulasi = HasilKalkulasi(),
    val pesanError: String? = null
)

class CalculatorViewModel : ViewModel() {

    var uiState by mutableStateOf(CalculatorUiState())
        private set

    fun onModeChange(mode: CalcMode) {
        if (mode == uiState.mode) return
        // Jangan bawa diskon antar mode: field-nya beda & default-nya beda.
        uiState = uiState.copy(
            mode = mode,
            pesanError = null
        )
        recalculate()
    }

    fun onHargaAwalChange(value: String) {
        uiState = uiState.copy(hargaAwal = value.filter { it.isDigit() })
        recalculate()
    }

    fun onDiskonHargaJualChange(value: String) {
        uiState = uiState.copy(diskonPersenHargaJual = value.filter { it.isDigit() }.take(3))
        recalculate()
    }

    fun onTargetHargaChange(value: String) {
        uiState = uiState.copy(targetHarga = value.filter { it.isDigit() })
        recalculate()
    }

    fun onDiskonChange(value: String) {
        // Batasi maksimal 3 digit biar gak ada yang iseng ngetik diskon 99999%.
        uiState = uiState.copy(diskonPersen = value.filter { it.isDigit() }.take(3))
        recalculate()
    }

    fun onKategoriChange(kategori: KategoriFee) {
        uiState = uiState.copy(kategoriFee = kategori)
        recalculate()
    }

    fun onPromoXtraToggle(checked: Boolean) {
        uiState = when (uiState.mode) {
            CalcMode.HARGA_JUAL -> uiState.copy(pakaiPromoXtraHargaJual = checked)
            CalcMode.TARGET_HARGA -> uiState.copy(pakaiPromoXtraTarget = checked)
        }
        recalculate()
    }

    fun onPromoXtraPersenChange(value: String) {
        // Boleh desimal (mis. "4.5"); filter digit + satu titik.
        val bersih = value.filter { it.isDigit() || it == '.' }.take(4)
        uiState = uiState.copy(promoXtraPersen = bersih)
        recalculate()
    }

    fun onShippingSaverChange(value: String) {
        uiState = uiState.copy(shippingSaver = value.filter { it.isDigit() })
        recalculate()
    }

    fun onPremiumChange(value: String) {
        uiState = uiState.copy(premium = value.filter { it.isDigit() })
        recalculate()
    }

    fun onReset() {
        uiState = CalculatorUiState()
    }

    private fun promoXtraAktif(): Boolean = when (uiState.mode) {
        CalcMode.HARGA_JUAL -> uiState.pakaiPromoXtraHargaJual
        CalcMode.TARGET_HARGA -> uiState.pakaiPromoXtraTarget
    }

    private fun recalculate() {
        val kategori = uiState.kategoriFee
        val promoPersen = if (promoXtraAktif()) {
            uiState.promoXtraPersen.toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }
        val saver = uiState.shippingSaver.toLongOrNull() ?: 0L
        val premium = uiState.premium.toLongOrNull() ?: 0L

        when (uiState.mode) {
            CalcMode.HARGA_JUAL -> {
                val harga = uiState.hargaAwal.toLongOrNull() ?: 0L
                val diskon = uiState.diskonPersenHargaJual.toIntOrNull() ?: 0
                val hasil = CalculatorEngine.hitungDariHargaAwal(
                    hargaAwal = harga,
                    diskonPersen = diskon,
                    kategoriFee = kategori,
                    promoXtraPersen = promoPersen,
                    shippingSaver = saver,
                    premium = premium
                )

                uiState = if (hasil == null) {
                    uiState.copy(pesanError = "Diskon nggak bisa 100% atau lebih")
                } else {
                    uiState.copy(hasil = hasil, pesanError = null)
                }
            }

            CalcMode.TARGET_HARGA -> {
                val target = uiState.targetHarga.toLongOrNull() ?: 0L
                val diskon = uiState.diskonPersen.toIntOrNull() ?: 0
                val hasil = CalculatorEngine.hitungDariTargetHarga(
                    targetHarga = target,
                    diskonPersen = diskon,
                    kategoriFee = kategori,
                    promoXtraPersen = promoPersen,
                    shippingSaver = saver,
                    premium = premium
                )

                uiState = if (hasil == null) {
                    uiState.copy(
                        pesanError = "Diskon nggak bisa 100% atau lebih, hasilnya jadi gak masuk akal"
                    )
                } else {
                    uiState.copy(hasil = hasil, pesanError = null)
                }
            }
        }
    }
}
