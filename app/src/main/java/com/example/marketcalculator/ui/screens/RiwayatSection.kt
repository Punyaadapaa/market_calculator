package com.example.marketcalculator.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.marketcalculator.data.CalcMode
import com.example.marketcalculator.data.RiwayatEntri
import com.example.marketcalculator.ui.theme.tabularNums
import com.example.marketcalculator.ui.util.formatRupiah
import com.example.marketcalculator.ui.util.waktuRelatif

/**
 * Panel riwayat perhitungan: header yang bisa dibuka/tutup, daftar entri,
 * dialog detail, dan hapus (satu/semua).
 */
@Composable
fun RiwayatSection(
    daftar: List<RiwayatEntri>,
    onMuat: (RiwayatEntri) -> Unit,
    onHapus: (Long) -> Unit,
    onHapusSemua: () -> Unit
) {
    var terbuka by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<RiwayatEntri?>(null) }
    var konfirmasiHapusSemua by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Header (bisa diklik untuk buka/tutup) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { terbuka = !terbuka }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Riwayat Perhitungan",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (daftar.isEmpty()) "Belum ada riwayat"
                        else "${daftar.size} tersimpan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    if (terbuka) "Tutup" else "Buka",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = terbuka,
                enter = expandVertically(tween(220)) + fadeIn(tween(220)),
                exit = shrinkVertically(tween(180)) + fadeOut(tween(120))
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    if (daftar.isEmpty()) {
                        Text(
                            "Setiap perhitungan yang kamu simpan akan muncul di sini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        daftar.forEach { entri ->
                            RiwayatItem(
                                entri = entri,
                                onClick = { detail = entri },
                                onHapus = { onHapus(entri.id) }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 0.5.dp
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = { konfirmasiHapusSemua = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Hapus semua")
                        }
                    }
                }
            }
        }
    }

    // ── Dialog detail ──
    detail?.let { entri ->
        RiwayatDetailDialog(
            entri = entri,
            onTutup = { detail = null },
            onMuat = {
                onMuat(entri)
                detail = null
            }
        )
    }

    // ── Konfirmasi hapus semua ──
    if (konfirmasiHapusSemua) {
        AlertDialog(
            onDismissRequest = { konfirmasiHapusSemua = false },
            title = { Text("Hapus semua riwayat?") },
            text = { Text("Semua ${daftar.size} entri riwayat akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = {
                    onHapusSemua()
                    konfirmasiHapusSemua = false
                }) { Text("Hapus semua") }
            },
            dismissButton = {
                TextButton(onClick = { konfirmasiHapusSemua = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun RiwayatItem(
    entri: RiwayatEntri,
    onClick: () -> Unit,
    onHapus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (entri.mode == CalcMode.TARGET_HARGA.name) "Target Harga" else "Harga Jual",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "  ·  ${waktuRelatif(entri.id)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                entri.kategoriNama,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Bersih ${formatRupiah(entri.penghasilanBersih)}",
                style = MaterialTheme.typography.bodySmall.tabularNums(),
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )
        }
        IconButton(onClick = onHapus) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Hapus entri ini",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RiwayatDetailDialog(
    entri: RiwayatEntri,
    onTutup: () -> Unit,
    onMuat: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onTutup,
        title = { Text("Detail Perhitungan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "${waktuRelatif(entri.id)}  ·  ${entri.kategoriNama}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DetailBaris("Harga dasar", formatRupiah(entri.hargaAwal))
                DetailBaris("Diskon", "${entri.diskonPersen}%")
                DetailBaris(
                    "Fee admin", "${formatPersenRingkas(entri.persenFee)} · ${formatRupiah(entri.commissionFee)}"
                )
                DetailBaris("Biaya proses", formatRupiah(entri.prosesFee))
                if (entri.promoXtraFee > 0) {
                    DetailBaris("Promo XTRA", "${formatPersenRingkas(entri.promoXtraPersen)} · ${formatRupiah(entri.promoXtraFee)}")
                }
                if (entri.shippingSaverFee > 0) {
                    DetailBaris("Shipping Fee Saver", formatRupiah(entri.shippingSaverFee))
                }
                if (entri.premiumFee > 0) {
                    DetailBaris("Premium (0,5%)", formatRupiah(entri.premiumFee))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DetailBaris("Total potongan", formatRupiah(entri.totalPotongan))
                DetailBaris(
                    "Penghasilan bersih", formatRupiah(entri.penghasilanBersih), tebal = true
                )
                entri.hargaWajibPasang?.let {
                    DetailBaris("Harga wajib pasang", formatRupiah(it))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onMuat) {
                Icon(Icons.Outlined.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Muat ke kalkulator")
            }
        },
        dismissButton = {
            TextButton(onClick = onTutup) { Text("Tutup") }
        }
    )
}

@Composable
private fun DetailBaris(label: String, nilai: String, tebal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            nilai,
            style = MaterialTheme.typography.bodySmall.tabularNums(),
            color = if (tebal) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (tebal) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}

/** 8.25 -> "8,25%", 4.0 -> "4%". */
private fun formatPersenRingkas(persen: Double): String {
    val teks = if (persen % 1.0 == 0.0) persen.toInt().toString()
    else String.format("%.2f", persen).trimEnd('0').trimEnd('.').replace('.', ',')
    return "$teks%"
}
