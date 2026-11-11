package com.example.marketcalculator.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.marketcalculator.data.CalcMode
import com.example.marketcalculator.data.RiwayatEntri
import com.example.marketcalculator.ui.theme.tabularNums
import com.example.marketcalculator.ui.util.formatRupiah
import com.example.marketcalculator.ui.util.waktuRelatif

/**
 * Calculation history panel: collapsible header, entry list, detail dialog,
 * and delete (single or all).
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
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header (click to expand/collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { terbuka = !terbuka }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Riwayat Perhitungan",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (daftar.isEmpty()) "Belum ada riwayat"
                        else "${daftar.size} tersimpan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    if (terbuka) "Tutup" else "Buka",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
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
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = { konfirmasiHapusSemua = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                "Hapus semua",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail dialog
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

    // Confirm delete all
    if (konfirmasiHapusSemua) {
        KonfirmasiHapusDialog(
            jumlah = daftar.size,
            onBatal = { konfirmasiHapusSemua = false },
            onHapus = {
                onHapusSemua()
                konfirmasiHapusSemua = false
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
    // Displayed value: price before for Target Harga mode, otherwise net profit.
    val nilai = if (entri.mode == CalcMode.TARGET_HARGA.name) {
        entri.hargaWajibPasang ?: entri.penghasilanBersih
    } else {
        entri.penghasilanBersih
    }

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
                    when (entri.mode) {
                        CalcMode.TARGET_HARGA.name -> "Target Harga"
                        CalcMode.TARGET_BERSIH.name -> "Target Bersih"
                        else -> "Harga Jual"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "  ·  ${waktuRelatif(entri.id)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                entri.kategoriNama,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        Text(
            formatRupiah(nilai),
            style = MaterialTheme.typography.bodyMedium.tabularNums(),
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(
            onClick = onHapus
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Hapus entri ini",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
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
    Dialog(onDismissRequest = onTutup) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Detail Perhitungan",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .clickable(onClick = onTutup),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${waktuRelatif(entri.id)}  ·  ${entri.kategoriNama}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (entri.mode == CalcMode.TARGET_BERSIH.name && entri.targetBersih != null) {
                        DetailBaris("Target penghasilan bersih", formatRupiah(entri.targetBersih))
                    } else {
                        DetailBaris("Harga dasar", formatRupiah(entri.hargaAwal))
                    }
                    DetailBaris("Diskon", "${entri.diskonPersen}%")
                    DetailBaris(
                        "Fee admin",
                        "${formatPersenRingkas(entri.persenFee)} · ${formatRupiah(entri.commissionFee)}"
                    )
                    DetailBaris("Biaya proses", formatRupiah(entri.prosesFee))
                    if (entri.promoXtraFee > 0) {
                        DetailBaris(
                            "Promo XTRA",
                            "${formatPersenRingkas(entri.promoXtraPersen)} · ${formatRupiah(entri.promoXtraFee)}"
                        )
                    }
                    if (entri.shippingSaverFee > 0) {
                        DetailBaris("Shipping Fee Saver", formatRupiah(entri.shippingSaverFee))
                    }
                    if (entri.premiumFee > 0) {
                        DetailBaris("PPh 22 / WHT 22 (0,5%)", formatRupiah(entri.premiumFee))
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    DetailBaris("Total potongan", formatRupiah(entri.totalPotongan))
                    DetailBaris(
                        "Penghasilan bersih",
                        formatRupiah(entri.penghasilanBersih),
                        tebal = true
                    )
                    entri.hargaWajibPasang?.let {
                        DetailBaris("Harga wajib pasang", formatRupiah(it))
                    }
                    entri.hargaSetelahDiskon?.let {
                        DetailBaris("Harga setelah diskon", formatRupiah(it))
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onTutup) {
                        Text("Tutup", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onMuat,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Muat ke kalkulator", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun KonfirmasiHapusDialog(
    jumlah: Int,
    onBatal: () -> Unit,
    onHapus: () -> Unit
) {
    Dialog(onDismissRequest = onBatal) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Hapus semua riwayat?",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Semua $jumlah entri riwayat akan dihapus permanen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBatal) {
                        Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onHapus,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Hapus semua", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
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
