package com.example.marketcalculator.ui.screens

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.marketcalculator.data.CalcMode
import com.example.marketcalculator.data.CalculatorEngine
import com.example.marketcalculator.data.HasilKalkulasi
import com.example.marketcalculator.data.KategoriFee
import com.example.marketcalculator.data.RiwayatEntri
import com.example.marketcalculator.data.RiwayatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val mode: CalcMode = CalcMode.HARGA_JUAL,

    // Harga Jual inputs
    val hargaAwal: String = "",
    val diskonPersenHargaJual: String = "0",
    val pakaiPromoXtraHargaJual: Boolean = false,

    // Target Harga inputs
    val targetHarga: String = "",
    val diskonPersen: String = "0",
    val pakaiPromoXtraTarget: Boolean = false,

    // Target Bersih inputs (net income the seller wants to receive)
    val targetBersih: String = "",
    val diskonPersenBersih: String = "0",
    val pakaiPromoXtraBersih: Boolean = false,

    // Category and extra fees, shared by all modes
    val kategoriFee: KategoriFee = KategoriFee.default,
    // Promo XTRA rate is fixed at 4.5%, so it is not part of the UI state.
    val pakaiShippingSaver: Boolean = false,

    val hasil: HasilKalkulasi = HasilKalkulasi(),
    val pesanError: String? = null
)

class CalculatorViewModel(
    application: Application,
    private val savedState: SavedStateHandle
) : AndroidViewModel(application) {

    private val riwayatRepo = RiwayatRepository(application)

    /** History list, newest first, updated automatically from DataStore. */
    val riwayat: StateFlow<List<RiwayatEntri>> = riwayatRepo.riwayat.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    var uiState by mutableStateOf(muatStateAwal())
        private set

    /** Last saved valid input, used to skip duplicates. */
    private var kunciTerakhir: String? = null

    /**
     * Builds the initial state, restoring the last input from SavedStateHandle
     * when the system killed and restored the process.
     */
    private fun muatStateAwal(): CalculatorUiState {
        val default = CalculatorUiState()
        val state = CalculatorUiState(
            mode = savedState.get<String>(KEY_MODE)
                ?.let { runCatching { CalcMode.valueOf(it) }.getOrNull() }
                ?: default.mode,
            hargaAwal = savedState[KEY_HARGA_AWAL] ?: default.hargaAwal,
            diskonPersenHargaJual = savedState[KEY_DISKON_HJ] ?: default.diskonPersenHargaJual,
            pakaiPromoXtraHargaJual = savedState[KEY_PROMO_HJ] ?: default.pakaiPromoXtraHargaJual,
            targetHarga = savedState[KEY_TARGET] ?: default.targetHarga,
            diskonPersen = savedState[KEY_DISKON] ?: default.diskonPersen,
            pakaiPromoXtraTarget = savedState[KEY_PROMO_TARGET] ?: default.pakaiPromoXtraTarget,
            targetBersih = savedState[KEY_TARGET_BERSIH] ?: default.targetBersih,
            diskonPersenBersih = savedState[KEY_DISKON_BERSIH] ?: default.diskonPersenBersih,
            pakaiPromoXtraBersih = savedState[KEY_PROMO_BERSIH] ?: default.pakaiPromoXtraBersih,
            kategoriFee = savedState.get<String>(KEY_KATEGORI)
                ?.let { runCatching { KategoriFee.valueOf(it) }.getOrNull() }
                ?: default.kategoriFee,
            pakaiShippingSaver = savedState[KEY_SAVER] ?: default.pakaiShippingSaver
        )
        return hitung(state)
    }

    /** Saves the important input fields against process death, then recalculates. */
    private fun terapkan(state: CalculatorUiState): CalculatorUiState {
        savedState[KEY_MODE] = state.mode.name
        savedState[KEY_HARGA_AWAL] = state.hargaAwal
        savedState[KEY_DISKON_HJ] = state.diskonPersenHargaJual
        savedState[KEY_PROMO_HJ] = state.pakaiPromoXtraHargaJual
        savedState[KEY_TARGET] = state.targetHarga
        savedState[KEY_DISKON] = state.diskonPersen
        savedState[KEY_PROMO_TARGET] = state.pakaiPromoXtraTarget
        savedState[KEY_TARGET_BERSIH] = state.targetBersih
        savedState[KEY_DISKON_BERSIH] = state.diskonPersenBersih
        savedState[KEY_PROMO_BERSIH] = state.pakaiPromoXtraBersih
        savedState[KEY_KATEGORI] = state.kategoriFee.name
        savedState[KEY_SAVER] = state.pakaiShippingSaver
        return hitung(state)
    }

    private companion object {
        const val KEY_MODE = "mode"
        const val KEY_HARGA_AWAL = "harga_awal"
        const val KEY_DISKON_HJ = "diskon_hj"
        const val KEY_PROMO_HJ = "promo_hj"
        const val KEY_TARGET = "target"
        const val KEY_DISKON = "diskon"
        const val KEY_PROMO_TARGET = "promo_target"
        const val KEY_TARGET_BERSIH = "target_bersih"
        const val KEY_DISKON_BERSIH = "diskon_bersih"
        const val KEY_PROMO_BERSIH = "promo_bersih"
        const val KEY_KATEGORI = "kategori"
        const val KEY_SAVER = "saver"
    }

    // Input actions

    fun onModeChange(mode: CalcMode) {
        if (mode == uiState.mode) return
        uiState = terapkan(uiState.copy(mode = mode, pesanError = null))
    }

    fun onHargaAwalChange(value: String) {
        uiState = terapkan(uiState.copy(hargaAwal = value.filter { it.isDigit() }))
    }

    fun onDiskonHargaJualChange(value: String) {
        uiState = terapkan(uiState.copy(diskonPersenHargaJual = value.filter { it.isDigit() }.take(3)))
    }

    fun onTargetHargaChange(value: String) {
        uiState = terapkan(uiState.copy(targetHarga = value.filter { it.isDigit() }))
    }

    fun onDiskonChange(value: String) {
        uiState = terapkan(uiState.copy(diskonPersen = value.filter { it.isDigit() }.take(3)))
    }

    fun onTargetBersihChange(value: String) {
        uiState = terapkan(uiState.copy(targetBersih = value.filter { it.isDigit() }))
    }

    fun onDiskonBersihChange(value: String) {
        uiState = terapkan(uiState.copy(diskonPersenBersih = value.filter { it.isDigit() }.take(3)))
    }

    fun onKategoriChange(kategori: KategoriFee) {
        uiState = terapkan(uiState.copy(kategoriFee = kategori))
    }

    fun onPromoXtraToggle(checked: Boolean) {
        val berikut = when (uiState.mode) {
            CalcMode.HARGA_JUAL -> uiState.copy(pakaiPromoXtraHargaJual = checked)
            CalcMode.TARGET_HARGA -> uiState.copy(pakaiPromoXtraTarget = checked)
            CalcMode.TARGET_BERSIH -> uiState.copy(pakaiPromoXtraBersih = checked)
        }
        uiState = terapkan(berikut)
    }

    fun onShippingSaverToggle(checked: Boolean) {
        uiState = terapkan(uiState.copy(pakaiShippingSaver = checked))
    }

    fun onReset() {
        kunciTerakhir = null
        uiState = terapkan(CalculatorUiState())
    }

    // History

    fun hapusRiwayat(id: Long) {
        viewModelScope.launch { riwayatRepo.hapus(id) }
    }

    fun hapusSemuaRiwayat() {
        viewModelScope.launch { riwayatRepo.hapusSemua() }
    }

    /** Load a history entry back into the calculator for editing or recalculation. */
    fun muatDariRiwayat(entri: RiwayatEntri) {
        val mode = runCatching { CalcMode.valueOf(entri.mode) }.getOrDefault(CalcMode.HARGA_JUAL)
        // Match by label first; fallback to the saved fee percentage.
        // If both fail, keep the current category.
        val kategori = KategoriFee.entries.firstOrNull { it.label == entri.kategoriNama }
            ?: KategoriFee.entries.firstOrNull {
                kotlin.math.abs(it.persen - entri.persenFee) < 0.001
            }
            ?: uiState.kategoriFee
        val dasar = CalculatorUiState(
            mode = mode,
            hargaAwal = if (mode == CalcMode.HARGA_JUAL) entri.hargaAwal.toString() else uiState.hargaAwal,
            diskonPersenHargaJual = if (mode == CalcMode.HARGA_JUAL) entri.diskonPersen.toString() else uiState.diskonPersenHargaJual,
            pakaiPromoXtraHargaJual = if (mode == CalcMode.HARGA_JUAL) entri.promoXtraAktif else uiState.pakaiPromoXtraHargaJual,
            targetHarga = if (mode == CalcMode.TARGET_HARGA) entri.hargaAwal.toString() else uiState.targetHarga,
            diskonPersen = if (mode == CalcMode.TARGET_HARGA) entri.diskonPersen.toString() else uiState.diskonPersen,
            pakaiPromoXtraTarget = if (mode == CalcMode.TARGET_HARGA) entri.promoXtraAktif else uiState.pakaiPromoXtraTarget,
            targetBersih = if (mode == CalcMode.TARGET_BERSIH) (entri.targetBersih ?: 0L).toString() else uiState.targetBersih,
            diskonPersenBersih = if (mode == CalcMode.TARGET_BERSIH) entri.diskonPersen.toString() else uiState.diskonPersenBersih,
            pakaiPromoXtraBersih = if (mode == CalcMode.TARGET_BERSIH) entri.promoXtraAktif else uiState.pakaiPromoXtraBersih,
            kategoriFee = kategori,
            pakaiShippingSaver = entri.pakaiShippingSaver
        )
        uiState = terapkan(dasar)
    }

    /**
     * Saves the current calculation to history. Called when the user is done
     * typing (keyboard closed or Done pressed), not on every keystroke, so one
     * calculation makes one entry.
     *
     * Skipped when:
     * - there is no valid result yet (zero price or an error), or
     * - the input is identical to the last saved one.
     */
    fun simpanRiwayat() {
        val state = uiState
        if (state.pesanError != null) return
        val h = state.hasil
        if (h.totalPotongan == 0L && h.penghasilanBersih == 0L) return

        val kunci = daftarKunci(state)
        if (kunci == kunciTerakhir) return
        kunciTerakhir = kunci

        val entri = RiwayatEntri(
            id = System.currentTimeMillis(),
            mode = state.mode.name,
            kategoriNama = state.kategoriFee.label,
            hargaAwal = when (state.mode) {
                CalcMode.HARGA_JUAL -> state.hargaAwal.toLongOrNull() ?: 0L
                CalcMode.TARGET_HARGA -> h.hargaWajibPasang ?: 0L
                CalcMode.TARGET_BERSIH -> h.hargaWajibPasang ?: 0L
            },
            diskonPersen = when (state.mode) {
                CalcMode.HARGA_JUAL -> state.diskonPersenHargaJual.toIntOrNull() ?: 0
                CalcMode.TARGET_HARGA -> state.diskonPersen.toIntOrNull() ?: 0
                CalcMode.TARGET_BERSIH -> state.diskonPersenBersih.toIntOrNull() ?: 0
            },
            promoXtraPersen = 4.5,
            promoXtraAktif = promoXtraAktif(state),
            pakaiShippingSaver = state.pakaiShippingSaver,
            persenFee = h.persenFeeAktif,
            commissionFee = h.commissionFee,
            prosesFee = h.prosesFee,
            promoXtraFee = h.promoXtraFee,
            shippingSaverFee = h.shippingSaverFee,
            premiumFee = h.premiumFee,
            totalPotongan = h.totalPotongan,
            penghasilanBersih = h.penghasilanBersih,
            hargaWajibPasang = h.hargaWajibPasang,
            hargaSetelahDiskon = h.hargaSetelahDiskon,
            targetBersih = h.targetBersih
        )
        viewModelScope.launch { riwayatRepo.tambah(entri) }
    }

    /** Input identity key, used to detect duplicates. */
    private fun daftarKunci(state: CalculatorUiState): String = listOf(
        state.mode.name,
        state.hargaAwal,
        state.diskonPersenHargaJual,
        state.targetHarga,
        state.diskonPersen,
        state.targetBersih,
        state.diskonPersenBersih,
        state.kategoriFee.name,
        pakaiPromoXtraFlags(state),
        state.pakaiShippingSaver.toString()
    ).joinToString("|")

    private fun pakaiPromoXtraFlags(state: CalculatorUiState): String = when (state.mode) {
        CalcMode.HARGA_JUAL -> state.pakaiPromoXtraHargaJual.toString()
        CalcMode.TARGET_HARGA -> state.pakaiPromoXtraTarget.toString()
        CalcMode.TARGET_BERSIH -> state.pakaiPromoXtraBersih.toString()
    }

    // Calculation

    /** Promo XTRA is active when the mode toggle is ON. Rate is fixed at 4.5%. */
    private fun promoXtraAktif(state: CalculatorUiState): Boolean = when (state.mode) {
        CalcMode.HARGA_JUAL -> state.pakaiPromoXtraHargaJual
        CalcMode.TARGET_HARGA -> state.pakaiPromoXtraTarget
        CalcMode.TARGET_BERSIH -> state.pakaiPromoXtraBersih
    }

    /** Calculate [state] and return a copy with the result. */
    private fun hitung(state: CalculatorUiState): CalculatorUiState {
        val kategori = state.kategoriFee
        // Fixed 4.5% when the toggle is ON, otherwise 0.
        val promoPersen = if (promoXtraAktif(state)) 4.5 else 0.0

        return when (state.mode) {
            CalcMode.HARGA_JUAL -> {
                val harga = state.hargaAwal.toLongOrNull() ?: 0L
                val diskon = state.diskonPersenHargaJual.toIntOrNull() ?: 0
                val hasil = CalculatorEngine.hitungDariHargaAwal(
                    hargaAwal = harga,
                    diskonPersen = diskon,
                    kategoriFee = kategori,
                    promoXtraPersen = promoPersen,
                    pakaiShippingSaver = state.pakaiShippingSaver
                )
                if (hasil == null) {
                    state.copy(pesanError = "Diskon tidak boleh 100% atau lebih")
                } else {
                    state.copy(hasil = hasil, pesanError = null)
                }
            }

            CalcMode.TARGET_HARGA -> {
                val target = state.targetHarga.toLongOrNull() ?: 0L
                val diskon = state.diskonPersen.toIntOrNull() ?: 0
                val hasil = CalculatorEngine.hitungDariTargetHarga(
                    targetHarga = target,
                    diskonPersen = diskon,
                    kategoriFee = kategori,
                    promoXtraPersen = promoPersen,
                    pakaiShippingSaver = state.pakaiShippingSaver
                )
                if (hasil == null) {
                    state.copy(pesanError = "Diskon tidak boleh 100% atau lebih")
                } else {
                    state.copy(hasil = hasil, pesanError = null)
                }
            }

            CalcMode.TARGET_BERSIH -> {
                val target = state.targetBersih.toLongOrNull() ?: 0L
                val diskon = state.diskonPersenBersih.toIntOrNull() ?: 0
                val hasil = CalculatorEngine.hitungDariTargetBersih(
                    targetBersih = target,
                    diskonPersen = diskon,
                    kategoriFee = kategori,
                    promoXtraPersen = promoPersen,
                    pakaiShippingSaver = state.pakaiShippingSaver
                )
                if (hasil == null) {
                    val pesan = if (diskon >= 100) {
                        "Diskon tidak boleh 100% atau lebih"
                    } else {
                        "Target terlalu besar untuk dihitung"
                    }
                    state.copy(pesanError = pesan)
                } else {
                    state.copy(hasil = hasil, pesanError = null)
                }
            }
        }
    }
}
