package com.example.marketcalculator

import androidx.lifecycle.SavedStateHandle
import com.example.marketcalculator.data.CalcMode
import com.example.marketcalculator.data.KategoriFee
import com.example.marketcalculator.ui.screens.CalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CalculatorViewModelTest {

    @Test
    fun `input tersimpan ke SavedStateHandle dan dipulihkan`() {
        val handle = SavedStateHandle()
        val vm1 = CalculatorViewModel(handle)
        vm1.onHargaAwalChange("1000000")
        vm1.onKategoriChange(KategoriFee.FASHION_BAWAH)

        // Simulasi proses death: buat VM baru dari handle yang sama.
        val vm2 = CalculatorViewModel(handle)
        assertEquals("1000000", vm2.uiState.hargaAwal)
        assertEquals(KategoriFee.FASHION_BAWAH, vm2.uiState.kategoriFee)
        // Hasil langsung terhitung ulang saat restore (bukan 0).
        assertNotEquals(0L, vm2.uiState.hasil.penghasilanBersih)
    }

    @Test
    fun `restore mempertahankan mode target harga`() {
        val handle = SavedStateHandle()
        val vm1 = CalculatorViewModel(handle)
        vm1.onModeChange(CalcMode.TARGET_HARGA)
        vm1.onTargetHargaChange("500000")

        val vm2 = CalculatorViewModel(handle)
        assertEquals(CalcMode.TARGET_HARGA, vm2.uiState.mode)
        assertEquals("500000", vm2.uiState.targetHarga)
    }

    @Test
    fun `reset mengosongkan SavedStateHandle`() {
        val handle = SavedStateHandle()
        val vm = CalculatorViewModel(handle)
        vm.onHargaAwalChange("1000000")
        vm.onReset()

        val vm2 = CalculatorViewModel(handle)
        assertEquals("", vm2.uiState.hargaAwal)
    }
}
