package com.example.marketcalculator.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected as semanticsSelected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketcalculator.BuildConfig
import com.example.marketcalculator.data.CalcMode
import com.example.marketcalculator.data.HasilKalkulasi
import com.example.marketcalculator.data.KategoriFee
import com.example.marketcalculator.data.formatPersen
import com.example.marketcalculator.ui.theme.tabularNums
import com.example.marketcalculator.ui.util.ThousandsSeparatorTransformation
import com.example.marketcalculator.ui.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val uiState = viewModel.uiState

    // Lacak berapa field yang sedang fokus. Saat jumlahnya turun ke 0
    // (keyboard ditutup / user ketuk di luar), perhitungan dianggap SELESAI
    // lalu disimpan ke riwayat.
    val jumlahFokus = remember { mutableStateOf(0) }
    val onFokusBerubah: (Boolean) -> Unit = { fokus ->
        val baru = (jumlahFokus.value + if (fokus) 1 else -1).coerceAtLeast(0)
        val sebelumnya = jumlahFokus.value
        jumlahFokus.value = baru
        if (sebelumnya > 0 && baru == 0) viewModel.simpanRiwayat()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
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
                            pesanError = uiState.pesanError,
                            onFokusBerubah = onFokusBerubah
                        )
                        CalcMode.TARGET_HARGA -> TargetHargaForm(
                            targetHarga = uiState.targetHarga,
                            onTargetHargaChange = viewModel::onTargetHargaChange,
                            diskonPersen = uiState.diskonPersen,
                            onDiskonChange = viewModel::onDiskonChange,
                            pesanError = uiState.pesanError,
                            onFokusBerubah = onFokusBerubah
                        )
                    }
                }

                // ── Section Label: Kategori & Biaya Tambahan ──
                SectionLabel(
                    icon = Icons.Outlined.Storefront,
                    title = "Kategori & Biaya Tambahan"
                )

                BiayaTambahanCard(
                    kategori = uiState.kategoriFee,
                    onKategoriChange = viewModel::onKategoriChange,
                    promoXtraAktif = if (uiState.mode == CalcMode.HARGA_JUAL)
                        uiState.pakaiPromoXtraHargaJual
                    else
                        uiState.pakaiPromoXtraTarget,
                    onPromoXtraToggle = viewModel::onPromoXtraToggle,
                    promoXtraPersen = uiState.promoXtraPersen,
                    onPromoXtraPersenChange = viewModel::onPromoXtraPersenChange,
                    pakaiShippingSaver = uiState.pakaiShippingSaver,
                    onShippingSaverToggle = viewModel::onShippingSaverToggle,
                    onFokusBerubah = onFokusBerubah
                )

                // ── Section Label: Hasil ──
                SectionLabel(
                    icon = Icons.Outlined.Receipt,
                    title = "Hasil Kalkulasi"
                )

                // ── Ringkasan Hasil ──
                RingkasanCard(
                    hasil = uiState.hasil,
                    onReset = viewModel::onReset
                )

                // ── Riwayat Perhitungan ──
                val daftarRiwayat by viewModel.riwayat.collectAsStateWithLifecycle()
                RiwayatSection(
                    daftar = daftarRiwayat,
                    onMuat = viewModel::muatDariRiwayat,
                    onHapus = viewModel::hapusRiwayat,
                    onHapusSemua = viewModel::hapusSemuaRiwayat
                )

                // ── Versi app (dari BuildConfig, otomatis dari build.gradle) ──
                VersionFooter()

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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ═══════════════════════════════════════════════════════════
// HEADER
// ═══════════════════════════════════════════════════════════

@Composable
private fun HeaderSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .statusBarsPadding()
    ) {
        // Ikon bulat sederhana (tanpa double ring / gradient)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Storefront,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            "Kalkulator Seller",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
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
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
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
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(11.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primary
        else
            Color.Transparent,
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics { semanticsSelected = selected }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimary
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
    pesanError: String?,
    onFokusBerubah: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    FormCard(title = "Harga Produk") {
        RupiahField(
            label = "Harga Sebelum Diskon",
            value = hargaAwal,
            onValueChange = onHargaAwalChange,
            icon = Icons.Outlined.Payments,
            onFokusBerubah = onFokusBerubah
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            isError = pesanError != null,
            supportingText = {
                if (pesanError != null) {
                    Text(
                        pesanError,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.titleMedium.tabularNums(),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFokusBerubah(it.isFocused) }
        )
    }
}

@Composable
private fun TargetHargaForm(
    targetHarga: String,
    onTargetHargaChange: (String) -> Unit,
    diskonPersen: String,
    onDiskonChange: (String) -> Unit,
    pesanError: String?,
    onFokusBerubah: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    FormCard(title = "Target Pembeli") {
        RupiahField(
            label = "Harga Dibayar Pembeli",
            value = targetHarga,
            onValueChange = onTargetHargaChange,
            icon = Icons.Outlined.ShoppingCart,
            onFokusBerubah = onFokusBerubah
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            isError = pesanError != null,
            supportingText = {
                if (pesanError != null) {
                    Text(
                        pesanError,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFokusBerubah(it.isFocused) }
        )
    }
}

@Composable
private fun FormCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
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
    icon: ImageVector,
    onFokusBerubah: (Boolean) -> Unit = {}
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
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        visualTransformation = ThousandsSeparatorTransformation(),
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.titleMedium.tabularNums(),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { onFokusBerubah(it.isFocused) }
    )
}

@Composable
private fun BiayaTambahanCard(
    kategori: KategoriFee,
    onKategoriChange: (KategoriFee) -> Unit,
    promoXtraAktif: Boolean,
    onPromoXtraToggle: (Boolean) -> Unit,
    promoXtraPersen: String,
    onPromoXtraPersenChange: (String) -> Unit,
    pakaiShippingSaver: Boolean,
    onShippingSaverToggle: (Boolean) -> Unit,
    onFokusBerubah: (Boolean) -> Unit
) {
    FormCard(title = "Biaya Shopee") {
        KategoriDropdown(
            kategori = kategori,
            onKategoriChange = onKategoriChange
        )

        PromoXtraRow(
            checked = promoXtraAktif,
            onCheckedChange = onPromoXtraToggle,
            persen = promoXtraPersen,
            onPersenChange = onPromoXtraPersenChange,
            onFokusBerubah = onFokusBerubah
        )

        ToggleRow(
            checked = pakaiShippingSaver,
            onCheckedChange = onShippingSaverToggle,
            icon = Icons.Outlined.LocalShipping,
            judul = "Shipping Fee Saver",
            keterangan = "Rp350 per pesanan"
        )
    }
}

/** Baris toggle sederhana: ikon + judul + keterangan opsional + Switch. */
@Composable
private fun ToggleRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
    judul: String,
    keterangan: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (checked) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            judul,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (keterangan != null) {
            Text(
                keterangan,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(10.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KategoriDropdown(
    kategori: KategoriFee,
    onKategoriChange: (KategoriFee) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = kategori.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Kategori Produk") },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            suffix = { Text("${formatPersen(kategori.persen)}") },
            supportingText = { Text(kategori.contoh, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            KategoriFee.entries.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                "${item.label}  (${formatPersen(item.persen)})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                item.contoh,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    onClick = {
                        onKategoriChange(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PromoXtraRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    persen: String,
    onPersenChange: (String) -> Unit,
    onFokusBerubah: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (checked)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Percent,
                contentDescription = null,
                tint = if (checked)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Promo XTRA",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        // Field persen di baris sendiri (aman saat font scale besar).
        if (checked) {
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = persen,
                onValueChange = onPersenChange,
                label = { Text("Persen Promo XTRA") },
                suffix = { Text("%") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodyMedium.tabularNums(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onFokusBerubah(it.isFocused) }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// RINGKASAN CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun RingkasanCard(
    hasil: HasilKalkulasi,
    onReset: () -> Unit
) {
    val kosong = hasil.totalPotongan == 0L && hasil.penghasilanBersih == 0L &&
        hasil.hargaWajibPasang == null && hasil.hargaSetelahDiskon == null

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (kosong) {
                EmptyHasil()
            } else {
                // Hasil utama (harga wajib pasang / harga dibayar pembeli)
                HasilUtamaPanel(hasil)

                Text(
                    "Rincian Potongan",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                DetailRow(
                    label = "Biaya Admin (${formatPersen(hasil.persenFeeAktif)})",
                    amount = hasil.commissionFee
                )
                DetailRow(
                    label = "Biaya Proses Pesanan",
                    amount = hasil.prosesFee
                )
                if (hasil.promoXtraFee > 0) {
                    DetailRow(label = "Promo XTRA", amount = hasil.promoXtraFee)
                }
                if (hasil.shippingSaverFee > 0) {
                    DetailRow(label = "Shipping Fee Saver", amount = hasil.shippingSaverFee)
                }
                if (hasil.premiumFee > 0) {
                    DetailRow(label = "Premium", amount = hasil.premiumFee)
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )

                DetailRow(
                    label = "Total Potongan",
                    amount = hasil.totalPotongan,
                    bold = true
                )

                Spacer(Modifier.height(2.dp))

                PenghasilanPanel(hasil.penghasilanBersih)
            }

            Spacer(Modifier.height(2.dp))

            // Reset — selalu terlihat, tidak perlu scroll jauh
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

@Composable
private fun EmptyHasil() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Calculate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Isi harga di atas",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "Hasil dan rincian potongan akan muncul di sini.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HasilUtamaPanel(hasil: HasilKalkulasi) {
    when {
        hasil.hargaWajibPasang != null -> {
            NilaiPanel(
                label = "Harga wajib pasang di Shopee",
                nilai = formatRupiah(hasil.hargaWajibPasang),
                warna = MaterialTheme.colorScheme.primary,
                icon = Icons.Outlined.Storefront,
                container = MaterialTheme.colorScheme.primaryContainer,
                onContainer = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        hasil.hargaSetelahDiskon != null -> {
            NilaiPanel(
                label = "Harga yang dibayar pembeli",
                nilai = formatRupiah(hasil.hargaSetelahDiskon),
                warna = MaterialTheme.colorScheme.tertiary,
                icon = Icons.Outlined.ShoppingCart,
                container = MaterialTheme.colorScheme.surfaceContainerHigh,
                onContainer = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Panel nilai (solid tonal, tanpa gradient/border). Dipakai untuk hasil utama.
 */
@Composable
private fun NilaiPanel(
    label: String,
    nilai: String,
    warna: Color,
    icon: ImageVector,
    container: Color,
    onContainer: Color
) {
    Surface(
        color = container,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = warna,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = onContainer
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                nilai,
                style = MaterialTheme.typography.displayLarge.tabularNums(),
                color = warna,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Panel penghasilan bersih. WAJIB berubah warna saat rugi (negatif) —
 * jangan pernah tampilkan angka negatif dengan warna profit.
 */
@Composable
private fun PenghasilanPanel(penghasilan: Long) {
    val rugi = penghasilan < 0
    val warna = if (rugi) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    val container = if (rugi) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.secondaryContainer
    val onContainer = if (rugi) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onSecondaryContainer
    val icon = if (rugi) Icons.AutoMirrored.Outlined.TrendingDown
    else Icons.AutoMirrored.Outlined.TrendingUp
    val label = if (rugi) "RUGI — hasil di bawah nol" else "Penghasilan bersih"

    Surface(
        color = container,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = warna,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = onContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                formatRupiah(penghasilan),
                style = MaterialTheme.typography.displayLarge.tabularNums(),
                color = warna,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DETAIL ROW (Baris rincian potongan)
// ═══════════════════════════════════════════════════════════

@Composable
private fun DetailRow(
    label: String,
    amount: Long,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            formatRupiah(amount, tanda = true),
            style = MaterialTheme.typography.bodyMedium.tabularNums(),
            color = MaterialTheme.colorScheme.error,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ═══════════════════════════════════════════════════════════
// INFO FOOTER
// ═══════════════════════════════════════════════════════════

@Composable
private fun VersionFooter() {
    Text(
        text = "v${BuildConfig.VERSION_NAME}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}
