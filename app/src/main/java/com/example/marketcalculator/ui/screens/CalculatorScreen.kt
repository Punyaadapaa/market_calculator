package com.example.marketcalculator.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketcalculator.data.CalcMode
import com.example.marketcalculator.data.HasilKalkulasi
import com.example.marketcalculator.ui.util.ThousandsSeparatorTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val uiState = viewModel.uiState

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ──
            HeaderSection()

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Section Label: Pilih Mode ──
                SectionLabel(
                    icon = Icons.Outlined.Calculate,
                    title = "Pilih Mode Kalkulasi"
                )

                // ── Tab Selector (pill-style) ──
                ModeSelector(mode = uiState.mode, onModeChange = viewModel::onModeChange)

                // ── Form Section dengan Animasi ──
                AnimatedContent(
                    targetState = uiState.mode,
                    transitionSpec = {
                        val dir = if (targetState == CalcMode.TARGET_HARGA) 1 else -1
                        (slideInHorizontally(tween(250)) { w -> dir * w / 3 } + fadeIn(tween(250)))
                            .togetherWith(
                                slideOutHorizontally(tween(200)) { w -> -dir * w / 3 } + fadeOut(tween(150))
                            )
                    },
                    label = "mode_transition"
                ) { mode ->
                    when (mode) {
                        CalcMode.HARGA_JUAL -> HargaJualForm(
                            hargaAwal = uiState.hargaAwal,
                            onHargaAwalChange = viewModel::onHargaAwalChange,
                            diskonPersen = uiState.diskonPersenHargaJual,
                            onDiskonChange = viewModel::onDiskonHargaJualChange,
                            pakaiAsuransi = uiState.pakaiAsuransi,
                            onAsuransiChange = viewModel::onAsuransiChange,
                            pesanError = uiState.pesanError
                        )
                        CalcMode.TARGET_HARGA -> TargetHargaForm(
                            targetHarga = uiState.targetHarga,
                            onTargetHargaChange = viewModel::onTargetHargaChange,
                            diskonPersen = uiState.diskonPersen,
                            onDiskonChange = viewModel::onDiskonChange,
                            pakaiAsuransi = uiState.pakaiAsuransi,
                            onAsuransiChange = viewModel::onAsuransiChange,
                            pesanError = uiState.pesanError
                        )
                    }
                }

                // ── Section Label: Hasil ──
                SectionLabel(
                    icon = Icons.Outlined.Receipt,
                    title = "Hasil Kalkulasi"
                )

                // ── Ringkasan Hasil ──
                RingkasanCard(hasil = uiState.hasil, onReset = viewModel::onReset)

                // ── Info Footer ──
                InfoFooter()

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SECTION LABEL
// ═══════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════
// HEADER
// ═══════════════════════════════════════════════════════════

@Composable
private fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .statusBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon bulat dengan double ring
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "Kalkulator Seller",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Hitung potongan & penghasilan bersih Shopee",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// TAB SELECTOR (Pill-style, modern)
// ═══════════════════════════════════════════════════════════

@Composable
private fun ModeSelector(mode: CalcMode, onModeChange: (CalcMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PillTab(
            label = "Harga Jual",
            icon = Icons.Outlined.Payments,
            selected = mode == CalcMode.HARGA_JUAL,
            onClick = { onModeChange(CalcMode.HARGA_JUAL) },
            modifier = Modifier.weight(1f)
        )
        PillTab(
            label = "Target Harga",
            icon = Icons.Outlined.ShoppingCart,
            selected = mode == CalcMode.TARGET_HARGA,
            onClick = { onModeChange(CalcMode.TARGET_HARGA) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PillTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(200),
        label = "pill_bg"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(11.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else
            Color.Transparent,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// FORM CARDS
// ═══════════════════════════════════════════════════════════

@Composable
private fun HargaJualForm(
    hargaAwal: String,
    onHargaAwalChange: (String) -> Unit,
    diskonPersen: String,
    onDiskonChange: (String) -> Unit,
    pakaiAsuransi: Boolean,
    onAsuransiChange: (Boolean) -> Unit,
    pesanError: String?
) {
    FormCard(
        title = "Informasi Harga",
        subtitle = "Masukkan harga produk yang dipasang di Shopee"
    ) {
        RupiahField(
            label = "Harga Produk (Before)",
            value = hargaAwal,
            onValueChange = onHargaAwalChange,
            icon = Icons.Outlined.Payments
        )
        OutlinedTextField(
            value = diskonPersen,
            onValueChange = onDiskonChange,
            label = { Text("Voucher Diskon") },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Percent,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            suffix = { Text("%") },
            singleLine = true,
            isError = pesanError != null,
            supportingText = {
                if (pesanError != null) {
                    Text(pesanError, color = MaterialTheme.colorScheme.error)
                }
            },
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        AsuransiRow(checked = pakaiAsuransi, onCheckedChange = onAsuransiChange)
    }
}

@Composable
private fun TargetHargaForm(
    targetHarga: String,
    onTargetHargaChange: (String) -> Unit,
    diskonPersen: String,
    onDiskonChange: (String) -> Unit,
    pakaiAsuransi: Boolean,
    onAsuransiChange: (Boolean) -> Unit,
    pesanError: String?
) {
    FormCard(
        title = "Target Pembeli",
        subtitle = "Harga yang ingin dibayar pembeli setelah diskon"
    ) {
        RupiahField(
            label = "Target Harga Bayar Pembeli (After)",
            value = targetHarga,
            onValueChange = onTargetHargaChange,
            icon = Icons.Outlined.ShoppingCart
        )
        OutlinedTextField(
            value = diskonPersen,
            onValueChange = onDiskonChange,
            label = { Text("Voucher Diskon") },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Percent,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            suffix = { Text("%") },
            singleLine = true,
            isError = pesanError != null,
            supportingText = {
                if (pesanError != null) {
                    Text(pesanError, color = MaterialTheme.colorScheme.error)
                }
            },
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        AsuransiRow(checked = pakaiAsuransi, onCheckedChange = onAsuransiChange)
    }
}

@Composable
private fun FormCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card header
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )

            content()
        }
    }
}

@Composable
private fun RupiahField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = { new -> onValueChange(new.filter { it.isDigit() }) },
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        },
        prefix = { Text("Rp ") },
        placeholder = { Text("0") },
        singleLine = true,
        visualTransformation = ThousandsSeparatorTransformation(),
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AsuransiRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (checked)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .then(
                if (checked)
                    Modifier.border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = if (checked)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "Asuransi Pengiriman",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    if (checked) "Premi +0.5% aktif" else "Nonaktif",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (checked)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}

// ═══════════════════════════════════════════════════════════
// RINGKASAN CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun RingkasanCard(hasil: HasilKalkulasi, onReset: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Harga Wajib Pasang (hanya muncul di mode Target)
            AnimatedVisibility(
                visible = hasil.hargaWajibPasang != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (hasil.hargaWajibPasang != null) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Storefront,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "HARGA WAJIB PASANG DI SHOPEE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    formatRupiah(hasil.hargaWajibPasang),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            // Harga Setelah Diskon (hanya muncul di mode Harga Jual + diskon > 0)
            AnimatedVisibility(
                visible = hasil.hargaSetelahDiskon != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (hasil.hargaSetelahDiskon != null) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.03f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.ShoppingCart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "HARGA YANG DIBAYAR PEMBELI",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    formatRupiah(hasil.hargaSetelahDiskon),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            // ── Rincian Potongan ──
            Text(
                "RINCIAN POTONGAN",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp
            )

            DetailRow(
                icon = Icons.AutoMirrored.Outlined.TrendingDown,
                label = "Biaya Layanan (${hasil.persenFeeAktif}%)",
                amount = hasil.serviceFee,
                color = MaterialTheme.colorScheme.error
            )
            DetailRow(
                icon = Icons.AutoMirrored.Outlined.TrendingDown,
                label = "Biaya Proses Pesanan (Flat)",
                amount = hasil.prosesFee,
                color = MaterialTheme.colorScheme.error
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )

            DetailRow(
                icon = Icons.AutoMirrored.Outlined.TrendingDown,
                label = "Total Potongan",
                amount = hasil.totalPotongan,
                color = MaterialTheme.colorScheme.error,
                bold = true
            )

            Spacer(Modifier.height(4.dp))

            // Penghasilan Bersih — Hero section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "PENGHASILAN BERSIH",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        formatRupiah(hasil.penghasilanBersih),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Reset button
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Icon(
                    Icons.Outlined.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Reset Kalkulator", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DETAIL ROW (Baris rincian potongan)
// ═══════════════════════════════════════════════════════════

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    amount: Long,
    color: Color,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
            )
        }
        Text(
            "- ${formatRupiah(amount)}",
            style = MaterialTheme.typography.bodyMedium,
            color = color.copy(alpha = 0.85f),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ═══════════════════════════════════════════════════════════
// INFO FOOTER
// ═══════════════════════════════════════════════════════════

@Composable
private fun InfoFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp).padding(top = 1.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "Potongan: Admin 12,75% (tanpa asuransi) / 13,25% (dengan asuransi) + Rp1.250 biaya proses pesanan.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            lineHeight = 16.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════
// FORMAT RUPIAH
// ═══════════════════════════════════════════════════════════

private fun formatRupiah(amount: Long): String {
    val text = amount.toString().removePrefix("-")
    val sb = StringBuilder()
    for ((index, char) in text.reversed().withIndex()) {
        if (index != 0 && index % 3 == 0) sb.append('.')
        sb.append(char)
    }
    val prefix = if (amount < 0) "-Rp" else "Rp"
    return "$prefix${sb.reverse()}"
}
