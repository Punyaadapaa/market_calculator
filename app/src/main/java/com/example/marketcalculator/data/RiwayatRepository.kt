package com.example.marketcalculator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Akses DataStore untuk riwayat perhitungan. Satu instance per proses. */
private val Context.riwayatDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "riwayat_kalkulator"
)

/**
 * Simpan & baca riwayat perhitungan ke DataStore Preferences (diserialisasi
 * sebagai JSON). Maksimal [BATAS] entri terbaru yang disimpan.
 */
class RiwayatRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val keyDaftar = stringPreferencesKey("daftar_riwayat")

    /** Aliran daftar riwayat, terbaru di depan. */
    val riwayat: Flow<List<RiwayatEntri>> = context.riwayatDataStore.data.map { prefs ->
        val raw = prefs[keyDaftar] ?: return@map emptyList()
        runCatching { json.decodeFromString<List<RiwayatEntri>>(raw) }
            .getOrDefault(emptyList())
    }

    /** Tambah entri baru di depan, potong ke [BATAS] terbaru. */
    suspend fun tambah(entri: RiwayatEntri) {
        context.riwayatDataStore.edit { prefs ->
            val lama = prefs[keyDaftar]
                ?.let { runCatching { json.decodeFromString<List<RiwayatEntri>>(it) }.getOrNull() }
                ?: emptyList()
            val baru = (listOf(entri) + lama).take(BATAS)
            prefs[keyDaftar] = json.encodeToString<List<RiwayatEntri>>(baru)
        }
    }

    /** Hapus satu entri berdasarkan id. */
    suspend fun hapus(id: Long) {
        context.riwayatDataStore.edit { prefs ->
            val lama = prefs[keyDaftar]
                ?.let { runCatching { json.decodeFromString<List<RiwayatEntri>>(it) }.getOrNull() }
                ?: emptyList()
            prefs[keyDaftar] = json.encodeToString<List<RiwayatEntri>>(lama.filterNot { it.id == id })
        }
    }

    /** Hapus semua entri. */
    suspend fun hapusSemua() {
        context.riwayatDataStore.edit { prefs -> prefs.remove(keyDaftar) }
    }

    companion object {
        const val BATAS = 50
    }
}
