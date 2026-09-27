package com.example.marketcalculator.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.marketcalculator.data.CalcMode
import com.example.marketcalculator.data.CalculatorEngine
import com.example.marketcalculator.data.HasilKalkulasi

data class CalculatorUiState(
    val mode: CalcMode = CalcMode.HARGA_JUAL,
    val hargaAwal: String = "",
    val diskonPersenHargaJual: String = "0",
    val targetHarga: String = "",
    val diskonPersen: String = "25",
    val pakaiAsuransi: Boolean = false,
    val hasil: HasilKalkulasi = HasilKalkulasi(),
    val pesanError: String? = null
)

class CalculatorViewModel : ViewModel() {

    var uiState by mutableStateOf(CalculatorUiState())
        private set

    fun onModeChange(mode: CalcMode) {
        uiState = uiState.copy(mode = mode)
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
        // Batasin maksimal 3 digit biar gak ada yang iseng ngetik diskon 99999%
        uiState = uiState.copy(diskonPersen = value.filter { it.isDigit() }.take(3))
        recalculate()
    }

    fun onAsuransiChange(checked: Boolean) {
        uiState = uiState.copy(pakaiAsuransi = checked)
        recalculate()
    }

    fun onReset() {
        uiState = CalculatorUiState()
    }

    private fun recalculate() {
        when (uiState.mode) {
            CalcMode.HARGA_JUAL -> {
                val harga = uiState.hargaAwal.toLongOrNull() ?: 0L
                val diskon = uiState.diskonPersenHargaJual.toIntOrNull() ?: 0
                val hasil = CalculatorEngine.hitungDariHargaAwal(harga, diskon, uiState.pakaiAsuransi)

                if (hasil == null) {
                    uiState = uiState.copy(
                        pesanError = "Diskon nggak bisa 100% atau lebih"
                    )
                } else {
                    uiState = uiState.copy(hasil = hasil, pesanError = null)
                }
            }

            CalcMode.TARGET_HARGA -> {
                val target = uiState.targetHarga.toLongOrNull() ?: 0L
                val diskon = uiState.diskonPersen.toIntOrNull() ?: 0
                val hasil = CalculatorEngine.hitungDariTargetHarga(
                    targetHarga = target,
                    diskonPersen = diskon,
                    pakaiAsuransi = uiState.pakaiAsuransi
                )

                if (hasil == null) {
                    uiState = uiState.copy(
                        pesanError = "Diskon nggak bisa 100% atau lebih, hasilnya jadi gak masuk akal"
                    )
                } else {
                    uiState = uiState.copy(hasil = hasil, pesanError = null)
                }
            }
        }
    }
}
