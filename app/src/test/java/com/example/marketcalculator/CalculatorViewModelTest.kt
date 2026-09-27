package com.example.marketcalculator

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.example.marketcalculator.data.CalcMode
import com.example.marketcalculator.data.KategoriFee
import com.example.marketcalculator.ui.screens.CalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CalculatorViewModelTest {

    private val app: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun `input tersimpan ke SavedStateHandle dan dipulihkan`() {
        val handle = SavedStateHandle()
        val vm1 = CalculatorViewModel(app, handle)
        vm1.onHargaAwalChange("1000000")
        vm1.onKategoriChange(KategoriFee.FASHION_BAWAH)

        // Simulasi proses death: buat VM baru dari handle yang sama.
        val vm2 = CalculatorViewModel(app, handle)
        assertEquals("1000000", vm2.uiState.hargaAwal)
        assertEquals(KategoriFee.FASHION_BAWAH, vm2.uiState.kategoriFee)
        // Hasil langsung terhitung ulang saat restore (bukan 0).
        assertNotEquals(0L, vm2.uiState.hasil.penghasilanBersih)
    }

    @Test
    fun `restore mempertahankan mode target harga`() {
        val handle = SavedStateHandle()
        val vm1 = CalculatorViewModel(app, handle)
        vm1.onModeChange(CalcMode.TARGET_HARGA)
        vm1.onTargetHargaChange("500000")

        val vm2 = CalculatorViewModel(app, handle)
        assertEquals(CalcMode.TARGET_HARGA, vm2.uiState.mode)
        assertEquals("500000", vm2.uiState.targetHarga)
    }

    @Test
    fun `reset mengosongkan SavedStateHandle`() {
        val handle = SavedStateHandle()
        val vm = CalculatorViewModel(app, handle)
        vm.onHargaAwalChange("1000000")
        vm.onReset()

        val vm2 = CalculatorViewModel(app, handle)
        assertEquals("", vm2.uiState.hargaAwal)
    }

    @Test
    fun `hasil terhitung otomatis tiap perubahan input`() {
        val handle = SavedStateHandle()
        val vm = CalculatorViewModel(app, handle)
        // Awal: kosong.
        assertEquals(0L, vm.uiState.hasil.penghasilanBersih)

        // Setelah input: langsung terhitung (auto), tanpa aksi hitung manual.
        vm.onHargaAwalChange("1000000")
        vm.onKategoriChange(KategoriFee.FASHION_BAWAH)
        assertNotEquals(0L, vm.uiState.hasil.penghasilanBersih)
        assertEquals(82_500L, vm.uiState.hasil.commissionFee)
    }

    @Test
    fun `premium selalu dihitung 0_5 persen`() {
        val handle = SavedStateHandle()
        val vm = CalculatorViewModel(app, handle)
        vm.onHargaAwalChange("200000")
        // 200.000 x 0,5% = 1.000, selalu ada tanpa input user.
        assertEquals(1_000L, vm.uiState.hasil.premiumFee)
    }
}
