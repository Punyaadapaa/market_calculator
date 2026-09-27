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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val diskonPersen: String = "0",
    val pakaiPromoXtraTarget: Boolean = false,

    // Kategori & biaya tambahan (dipakai kedua mode)
    val kategoriFee: KategoriFee = KategoriFee.default,
    val promoXtraPersen: String = PROMO_XTRA_DEFAULT_PERCENT.toString(),
    val pakaiShippingSaver: Boolean = false,

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

    /** Input perhitungan valid terakhir yang sudah disimpan (anti-duplikat). */
    private var kunciTerakhir: String? = null

    /** Job debounce penyimpanan riwayat (dibatalkan tiap input baru). */
    private var jobSimpan: Job? = null

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
            pakaiShippingSaver = savedState[KEY_SAVER] ?: default.pakaiShippingSaver
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
        savedState[KEY_SAVER] = state.pakaiShippingSaver
        val hasil = hitung(state)
        simpanOtomatis(hasil)
        return hasil
    }

    private companion object {
        /**
         * Jeda (ms) sebelum hasil disimpan ke riwayat, dihitung sejak input
         * terakhir. Memberi waktu user selesai mengetik supaya 1 perhitungan
         * hanya tersimpan sekali.
         */
        const val JEDA_SIMPAN_MS = 800L

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
    }

    // ── Aksi input ───────────────────────────────────────────

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

    fun onShippingSaverToggle(checked: Boolean) {
        uiState = terapkan(uiState.copy(pakaiShippingSaver = checked))
    }

    fun onReset() {
        jobSimpan?.cancel()
        kunciTerakhir = null
        uiState = terapkan(CalculatorUiState())
    }

    // ── Riwayat ──────────────────────────────────────────────

    fun hapusRiwayat(id: Long) {
        viewModelScope.launch { riwayatRepo.hapus(id) }
    }

    fun hapusSemuaRiwayat() {
        viewModelScope.launch { riwayatRepo.hapusSemua() }
    }

    /** Muat entri riwayat kembali ke kalkulator untuk dihitung ulang/diubah. */
    fun muatDariRiwayat(entri: RiwayatEntri) {
        val mode = runCatching { CalcMode.valueOf(entri.mode) }.getOrDefault(CalcMode.HARGA_JUAL)
        // Cocokkan label dulu; kalau gagal (mis. label lama sudah berubah),
        // fallback ke persen fee yang tersimpan di entri.
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
            kategoriFee = kategori,
            promoXtraPersen = trimAngkaDesimal(entri.promoXtraPersen),
            pakaiShippingSaver = entri.pakaiShippingSaver
        )
        uiState = terapkan(dasar)
    }

    /**
     * Simpan otomatis hasil perhitungan ke riwayat, SETELAH user berhenti
     * mengetik selama [JEDA_SIMPAN_MS]. Ini mencegah 1 perhitungan tercatat
     * berkali-kali saat harga diketik digit per digit (mis. "1","10","100"...).
     *
     * Dilewati kalau:
     * - belum ada hitungan valid (harga 0 / ada error), atau
     * - inputnya sama persis dengan yang terakhir disimpan (anti-duplikat).
     */
    private fun simpanOtomatis(state: CalculatorUiState) {
        // Batalkan jadwal simpan sebelumnya (user masih mengetik).
        jobSimpan?.cancel()

        if (state.pesanError != null) return
        val h = state.hasil
        if (h.totalPotongan == 0L && h.penghasilanBersih == 0L) return

        val kunci = daftarKunci(state)
        if (kunci == kunciTerakhir) return

        jobSimpan = viewModelScope.launch {
            delay(JEDA_SIMPAN_MS)
            // Cek ulang: kalau ternyata sudah tersimpan (mis. karena aksi lain), skip.
            if (kunci == kunciTerakhir) return@launch
            kunciTerakhir = kunci

            val entri = RiwayatEntri(
                id = System.currentTimeMillis(),
                mode = state.mode.name,
                kategoriNama = state.kategoriFee.label,
                hargaAwal = when (state.mode) {
                    CalcMode.HARGA_JUAL -> state.hargaAwal.toLongOrNull() ?: 0L
                    CalcMode.TARGET_HARGA -> h.hargaWajibPasang ?: 0L
                },
                diskonPersen = when (state.mode) {
                    CalcMode.HARGA_JUAL -> state.diskonPersenHargaJual.toIntOrNull() ?: 0
                    CalcMode.TARGET_HARGA -> state.diskonPersen.toIntOrNull() ?: 0
                },
                promoXtraPersen = state.promoXtraPersen.toDoubleOrNull() ?: 0.0,
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
                hargaSetelahDiskon = h.hargaSetelahDiskon
            )
            riwayatRepo.tambah(entri)
        }
    }

    /** Kunci identitas input (untuk deteksi duplikat). */
    private fun daftarKunci(state: CalculatorUiState): String = listOf(
        state.mode.name,
        state.hargaAwal,
        state.diskonPersenHargaJual,
        state.targetHarga,
        state.diskonPersen,
        state.kategoriFee.name,
        state.promoXtraPersen,
        pakaiPromoXtraFlags(state),
        state.pakaiShippingSaver.toString()
    ).joinToString("|")

    private fun pakaiPromoXtraFlags(state: CalculatorUiState): String = when (state.mode) {
        CalcMode.HARGA_JUAL -> state.pakaiPromoXtraHargaJual.toString()
        CalcMode.TARGET_HARGA -> state.pakaiPromoXtraTarget.toString()
    }

    /** 4.5 -> "4.5", 4.0 -> "4" (biar field-nya rapi). */
    private fun trimAngkaDesimal(nilai: Double): String =
        if (nilai % 1.0 == 0.0) nilai.toInt().toString() else nilai.toString()

    // ── Perhitungan ──────────────────────────────────────────

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
        }
    }
}
