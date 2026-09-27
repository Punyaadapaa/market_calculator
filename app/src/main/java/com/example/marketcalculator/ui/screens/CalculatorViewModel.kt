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
import com.example.marketcalculator.data.PROMO_XTRA_DEFAULT_PERCENT
import com.example.marketcalculator.data.RiwayatEntri
import com.example.marketcalculator.data.RiwayatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

class CalculatorViewModel(
    application: Application,
    private val savedState: SavedStateHandle
) : AndroidViewModel(application) {

    private val riwayatRepo = RiwayatRepository(application)

    /** Daftar riwayat (terbaru di depan), otomatis update dari DataStore. */
    val riwayat: StateFlow<List<RiwayatEntri>> = riwayatRepo.riwayat.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    var uiState by mutableStateOf(muatStateAwal())
        private set

    /**
     * Bangun state awal. Kalau proses app pernah dimatikan sistem dan
     * dipulihkan, ambil lagi input terakhir dari SavedStateHandle.
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
            kategoriFee = savedState.get<String>(KEY_KATEGORI)
                ?.let { runCatching { KategoriFee.valueOf(it) }.getOrNull() }
                ?: default.kategoriFee,
            promoXtraPersen = savedState[KEY_PROMO_PERSEN] ?: default.promoXtraPersen,
            shippingSaver = savedState[KEY_SAVER] ?: default.shippingSaver,
            premium = savedState[KEY_PREMIUM] ?: default.premium
        )
        return hitung(state)
    }

    /** Simpan field input penting supaya tahan proses death, lalu hitung. */
    private fun terapkan(state: CalculatorUiState): CalculatorUiState {
        savedState[KEY_MODE] = state.mode.name
        savedState[KEY_HARGA_AWAL] = state.hargaAwal
        savedState[KEY_DISKON_HJ] = state.diskonPersenHargaJual
        savedState[KEY_PROMO_HJ] = state.pakaiPromoXtraHargaJual
        savedState[KEY_TARGET] = state.targetHarga
        savedState[KEY_DISKON] = state.diskonPersen
        savedState[KEY_PROMO_TARGET] = state.pakaiPromoXtraTarget
        savedState[KEY_KATEGORI] = state.kategoriFee.name
        savedState[KEY_PROMO_PERSEN] = state.promoXtraPersen
        savedState[KEY_SAVER] = state.shippingSaver
        savedState[KEY_PREMIUM] = state.premium
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
        const val KEY_KATEGORI = "kategori"
        const val KEY_PROMO_PERSEN = "promo_persen"
        const val KEY_SAVER = "saver"
        const val KEY_PREMIUM = "premium"
    }

    fun onModeChange(mode: CalcMode) {
        if (mode == uiState.mode) return
        // Jangan bawa diskon antar mode: field-nya beda & default-nya beda.
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
        // Batasi maksimal 3 digit biar gak ada yang iseng ngetik diskon 99999%.
        uiState = terapkan(uiState.copy(diskonPersen = value.filter { it.isDigit() }.take(3)))
    }

    fun onKategoriChange(kategori: KategoriFee) {
        uiState = terapkan(uiState.copy(kategoriFee = kategori))
    }

    fun onPromoXtraToggle(checked: Boolean) {
        val berikut = when (uiState.mode) {
            CalcMode.HARGA_JUAL -> uiState.copy(pakaiPromoXtraHargaJual = checked)
            CalcMode.TARGET_HARGA -> uiState.copy(pakaiPromoXtraTarget = checked)
        }
        uiState = terapkan(berikut)
    }

    fun onPromoXtraPersenChange(value: String) {
        // Boleh desimal (mis. "4.5"); filter digit + satu titik.
        val bersih = value.filter { it.isDigit() || it == '.' }.take(4)
        uiState = terapkan(uiState.copy(promoXtraPersen = bersih))
    }

    fun onShippingSaverChange(value: String) {
        uiState = terapkan(uiState.copy(shippingSaver = value.filter { it.isDigit() }))
    }

    fun onPremiumChange(value: String) {
        uiState = terapkan(uiState.copy(premium = value.filter { it.isDigit() }))
    }

    fun onReset() {
        val kosong = CalculatorUiState()
        uiState = terapkan(kosong.copy(hasil = HasilKalkulasi()))
    }

    // ── Riwayat ──────────────────────────────────────────────

    /** Simpan perhitungan saat ini ke riwayat (dipanggil user). */
    fun simpanKeRiwayat() {
        val s = uiState
        val h = s.hasil
        if (h.totalPotongan == 0L && h.penghasilanBersih == 0L) return // belum ada hitungan
        val entri = RiwayatEntri(
            id = System.currentTimeMillis(),
            mode = s.mode.name,
            kategoriNama = s.kategoriFee.label,
            hargaAwal = when (s.mode) {
                CalcMode.HARGA_JUAL -> s.hargaAwal.toLongOrNull() ?: 0L
                CalcMode.TARGET_HARGA -> h.hargaWajibPasang ?: 0L
            },
            diskonPersen = when (s.mode) {
                CalcMode.HARGA_JUAL -> s.diskonPersenHargaJual.toIntOrNull() ?: 0
                CalcMode.TARGET_HARGA -> s.diskonPersen.toIntOrNull() ?: 0
            },
            promoXtraPersen = s.promoXtraPersen.toDoubleOrNull() ?: 0.0,
            promoXtraAktif = promoXtraAktif(s),
            shippingSaver = s.shippingSaver.toLongOrNull() ?: 0L,
            premium = s.premium.toLongOrNull() ?: 0L,
            persenFee = h.persenFeeAktif,
            commissionFee = h.commissionFee,
            prosesFee = h.prosesFee,
            promoXtraFee = h.promoXtraFee,
            shippingSaverFee = h.shippingSaverFee,
            premiumFee = h.premiumFee,
            totalPotongan = h.totalPotongan,
            penghasilanBersih = h.penghasilanBersih,
            hargaWajibPasang = h.hargaWajibPasang,
            hargaSetelahDiskon = h.hargaSetelahDiskon
        )
        viewModelScope.launch { riwayatRepo.tambah(entri) }
    }

    fun hapusRiwayat(id: Long) {
        viewModelScope.launch { riwayatRepo.hapus(id) }
    }

    fun hapusSemuaRiwayat() {
        viewModelScope.launch { riwayatRepo.hapusSemua() }
    }

    /** Muat entri riwayat kembali ke kalkulator untuk dihitung ulang/diubah. */
    fun muatDariRiwayat(entri: RiwayatEntri) {
        val mode = runCatching { CalcMode.valueOf(entri.mode) }.getOrDefault(CalcMode.HARGA_JUAL)
        val kategori = KategoriFee.entries.firstOrNull { it.label == entri.kategoriNama }
            ?: uiState.kategoriFee
        val dasar = CalculatorUiState(
            mode = mode,
            hargaAwal = if (mode == CalcMode.HARGA_JUAL) entri.hargaAwal.toString() else uiState.hargaAwal,
            diskonPersenHargaJual = if (mode == CalcMode.HARGA_JUAL) entri.diskonPersen.toString() else uiState.diskonPersenHargaJual,
            pakaiPromoXtraHargaJual = if (mode == CalcMode.HARGA_JUAL) entri.promoXtraAktif else uiState.pakaiPromoXtraHargaJual,
            targetHarga = if (mode == CalcMode.TARGET_HARGA) entri.hargaAwal.toString() else uiState.targetHarga,
            diskonPersen = if (mode == CalcMode.TARGET_HARGA) entri.diskonPersen.toString() else uiState.diskonPersen,
            pakaiPromoXtraTarget = if (mode == CalcMode.TARGET_HARGA) entri.promoXtraAktif else uiState.pakaiPromoXtraTarget,
            kategoriFee = kategori,
            promoXtraPersen = trimAngkaDesimal(entri.promoXtraPersen),
            shippingSaver = if (entri.shippingSaver > 0) entri.shippingSaver.toString() else "",
            premium = if (entri.premium > 0) entri.premium.toString() else ""
        )
        uiState = terapkan(dasar)
    }

    /** 4.5 -> "4.5", 4.0 -> "4" (biar field-nya rapi). */
    private fun trimAngkaDesimal(nilai: Double): String =
        if (nilai % 1.0 == 0.0) nilai.toInt().toString() else nilai.toString()

    private fun promoXtraAktif(state: CalculatorUiState): Boolean = when (state.mode) {
        CalcMode.HARGA_JUAL -> state.pakaiPromoXtraHargaJual
        CalcMode.TARGET_HARGA -> state.pakaiPromoXtraTarget
    }

    /** Hitung [state] dan kembalikan salinannya yang sudah berisi hasil. */
    private fun hitung(state: CalculatorUiState): CalculatorUiState {
        val kategori = state.kategoriFee
        val promoPersen = if (promoXtraAktif(state)) {
            state.promoXtraPersen.toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }
        val saver = state.shippingSaver.toLongOrNull() ?: 0L
        val premium = state.premium.toLongOrNull() ?: 0L

        return when (state.mode) {
            CalcMode.HARGA_JUAL -> {
                val harga = state.hargaAwal.toLongOrNull() ?: 0L
                val diskon = state.diskonPersenHargaJual.toIntOrNull() ?: 0
                val hasil = CalculatorEngine.hitungDariHargaAwal(
                    hargaAwal = harga,
                    diskonPersen = diskon,
                    kategoriFee = kategori,
                    promoXtraPersen = promoPersen,
                    shippingSaver = saver,
                    premium = premium
                )
                if (hasil == null) {
                    state.copy(pesanError = "Diskon nggak bisa 100% atau lebih")
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
                    shippingSaver = saver,
                    premium = premium
                )
                if (hasil == null) {
                    state.copy(
                        pesanError = "Diskon nggak bisa 100% atau lebih, hasilnya jadi gak masuk akal"
                    )
                } else {
                    state.copy(hasil = hasil, pesanError = null)
                }
            }
        }
    }
}
