package com.example.marketcalculator

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.marketcalculator.data.RiwayatEntri
import com.example.marketcalculator.data.RiwayatRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RiwayatRepositoryTest {

    private val app: Application = ApplicationProvider.getApplicationContext()

    private fun entri(id: Long, bersih: Long) = RiwayatEntri(
        id = id,
        mode = "HARGA_JUAL",
        kategoriNama = "Fashion Bawah",
        hargaAwal = 100_000,
        diskonPersen = 0,
        promoXtraPersen = 0.0,
        promoXtraAktif = false,
        pakaiShippingSaver = false,
        persenFee = 8.25,
        commissionFee = 8_250,
        prosesFee = 1_250,
        promoXtraFee = 0,
        shippingSaverFee = 0,
        premiumFee = 0,
        totalPotongan = 9_500,
        penghasilanBersih = bersih
    )

    @Test
    fun `tambah menyimpan entri terbaru di depan`() = runBlocking {
        val repo = RiwayatRepository(app)
        repo.hapusSemua() // pastikan bersih (DataStore mungkin persisten antar test)
        repo.tambah(entri(1, 90_500))
        repo.tambah(entri(2, 91_000))

        val daftar = repo.riwayat.first()
        assertEquals(listOf(2L, 1L), daftar.map { it.id })
    }

    @Test
    fun `hapus satu entri`() = runBlocking {
        val repo = RiwayatRepository(app)
        repo.hapusSemua()
        repo.tambah(entri(10, 1))
        repo.tambah(entri(11, 2))
        repo.hapus(10)

        val daftar = repo.riwayat.first()
        assertEquals(listOf(11L), daftar.map { it.id })
    }

    @Test
    fun `batas 50 entri terbaru`() = runBlocking {
        val repo = RiwayatRepository(app)
        repo.hapusSemua()
        repeat(55) { i -> repo.tambah(entri(i.toLong(), i.toLong())) }

        val daftar = repo.riwayat.first()
        assertEquals(RiwayatRepository.BATAS, daftar.size)
        // Entri terbaru (id 54) ada, yang paling lama (id 0) terbuang.
        assertEquals(54L, daftar.first().id)
        assertEquals(true, daftar.none { it.id == 0L })
    }
}
