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

/** DataStore access for calculation history, one instance per process. */
private val Context.riwayatDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "riwayat_kalkulator"
)

/**
 * Reads and writes calculation history in DataStore Preferences as JSON.
 * Only the newest [BATAS] entries are kept.
 */
class RiwayatRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val keyDaftar = stringPreferencesKey("daftar_riwayat")

    /** History list flow, newest first. */
    val riwayat: Flow<List<RiwayatEntri>> = context.riwayatDataStore.data.map { prefs ->
        val raw = prefs[keyDaftar] ?: return@map emptyList()
        runCatching { json.decodeFromString<List<RiwayatEntri>>(raw) }
            .getOrDefault(emptyList())
    }

    /** Prepend a new entry and trim to the newest [BATAS]. */
    suspend fun tambah(entri: RiwayatEntri) {
        context.riwayatDataStore.edit { prefs ->
            val lama = prefs[keyDaftar]
                ?.let { runCatching { json.decodeFromString<List<RiwayatEntri>>(it) }.getOrNull() }
                ?: emptyList()
            val baru = (listOf(entri) + lama).take(BATAS)
            prefs[keyDaftar] = json.encodeToString<List<RiwayatEntri>>(baru)
        }
    }

    /** Delete one entry by id. */
    suspend fun hapus(id: Long) {
        context.riwayatDataStore.edit { prefs ->
            val lama = prefs[keyDaftar]
                ?.let { runCatching { json.decodeFromString<List<RiwayatEntri>>(it) }.getOrNull() }
                ?: emptyList()
            prefs[keyDaftar] = json.encodeToString<List<RiwayatEntri>>(lama.filterNot { it.id == id })
        }
    }

    /** Delete all entries. */
    suspend fun hapusSemua() {
        context.riwayatDataStore.edit { prefs -> prefs.remove(keyDaftar) }
    }

    companion object {
        const val BATAS = 50
    }
}
