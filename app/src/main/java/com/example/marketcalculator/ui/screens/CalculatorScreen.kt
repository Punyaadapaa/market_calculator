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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected as semanticsSelected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketcalculator.BuildConfig
import com.example.marketcalculator.data.CalcMode
import com.example.marketcalculator.data.HasilKalkulasi
import com.example.marketcalculator.data.KategoriFee
import com.example.marketcalculator.data.formatPersen
import com.example.marketcalculator.ui.theme.tabularNums
import com.example.marketcalculator.ui.util.ThousandsSeparatorTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val uiState = viewModel.uiState

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
                            pesanError = uiState.pesanError
                        )
                        CalcMode.TARGET_HARGA -> TargetHargaForm(
                            targetHarga = uiState.targetHarga,
                            onTargetHargaChange = viewModel::onTargetHargaChange,
                            diskonPersen = uiState.diskonPersen,
                            onDiskonChange = viewModel::onDiskonChange,
                            pesanError = uiState.pesanError
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
                    shippingSaver = uiState.shippingSaver,
                    onShippingSaverChange = viewModel::onShippingSaverChange,
                    premium = uiState.premium,
                    onPremiumChange = viewModel::onPremiumChange
                )

                // ── Section Label: Hasil ──
                SectionLabel(
                    icon = Icons.Outlined.Receipt,
                    title = "Hasil Kalkulasi"
                )

                // ── Ringkasan Hasil ──
                RingkasanCard(hasil = uiState.hasil, onReset = viewModel::onReset)

                // ── Info Footer ──
                InfoFooter()

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
    pesanError: String?
) {
    FormCard(
        title = "Informasi Harga",
        subtitle = "Masukkan harga produk yang dipasang di Shopee"
    ) {
        RupiahField(
            label = "Harga Produk (Sebelum Diskon)",
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
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
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TargetHargaForm(
    targetHarga: String,
    onTargetHargaChange: (String) -> Unit,
    diskonPersen: String,
    onDiskonChange: (String) -> Unit,
    pesanError: String?
) {
    FormCard(
        title = "Target Pembeli",
        subtitle = "Harga yang ingin dibayar pembeli setelah diskon"
    ) {
        RupiahField(
            label = "Target Harga Setelah Diskon",
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
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
            modifier = Modifier.fillMaxWidth()
        )
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                color = MaterialTheme.colorScheme.outlineVariant,
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
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        visualTransformation = ThousandsSeparatorTransformation(),
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.titleMedium.tabularNums(),
        modifier = Modifier.fillMaxWidth()
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
    shippingSaver: String,
    onShippingSaverChange: (String) -> Unit,
    premium: String,
    onPremiumChange: (String) -> Unit
) {
    FormCard(
        title = "Biaya Shopee",
        subtitle = "Kategori menentukan fee admin; biaya lain opsional"
    ) {
        KategoriDropdown(
            kategori = kategori,
            onKategoriChange = onKategoriChange
        )

        PromoXtraRow(
            checked = promoXtraAktif,
            onCheckedChange = onPromoXtraToggle,
            persen = promoXtraPersen,
            onPersenChange = onPromoXtraPersenChange
        )

        RupiahField(
            label = "Shipping Fee Saver (opsional)",
            value = shippingSaver,
            onValueChange = onShippingSaverChange,
            icon = Icons.Outlined.LocalShipping
        )

        RupiahField(
            label = "Premium (opsional)",
            value = premium,
            onValueChange = onPremiumChange,
            icon = Icons.Outlined.Star
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
            supportingText = { Text("Fee admin: ${formatPersen(kategori.persen)}") },
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
                        Text(
                            "${item.label}  (${formatPersen(item.persen)})",
                            style = MaterialTheme.typography.bodyMedium
                        )
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
    onPersenChange: (String) -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Biaya Promo XTRA",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    if (checked) "Aktif — persen dari harga awal" else "Nonaktif",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (checked)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        // Field persen ditaruh di baris terpisah supaya tidak terpotong saat
        // font scale besar (mis. 1.3): lebar mengikuti layar, bukan fixed 96dp.
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
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodyMedium.tabularNums(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// RINGKASAN CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun RingkasanCard(hasil: HasilKalkulasi, onReset: () -> Unit) {
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
                // Hasil utama (harga wajib pasang / harga dibayar pembeli) — tonal, tanpa gradient
                HasilUtamaPanel(hasil)

                // ── Rincian Potongan ──
                Text(
                    "Rincian Potongan",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                DetailRow(
                    icon = Icons.AutoMirrored.Outlined.TrendingDown,
                    label = "Biaya Admin (${formatPersen(hasil.persenFeeAktif)})",
                    amount = hasil.commissionFee,
                    color = MaterialTheme.colorScheme.error
                )
                DetailRow(
                    icon = Icons.AutoMirrored.Outlined.TrendingDown,
                    label = "Biaya Proses Pesanan",
                    amount = hasil.prosesFee,
                    color = MaterialTheme.colorScheme.error
                )
                if (hasil.promoXtraFee > 0) {
                    DetailRow(
                        icon = Icons.AutoMirrored.Outlined.TrendingDown,
                        label = "Biaya Promo XTRA",
                        amount = hasil.promoXtraFee,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (hasil.shippingSaverFee > 0) {
                    DetailRow(
                        icon = Icons.AutoMirrored.Outlined.TrendingDown,
                        label = "Shipping Fee Saver",
                        amount = hasil.shippingSaverFee,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (hasil.premiumFee > 0) {
                    DetailRow(
                        icon = Icons.AutoMirrored.Outlined.TrendingDown,
                        label = "Premium",
                        amount = hasil.premiumFee,
                        color = MaterialTheme.colorScheme.error
                    )
                }

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

                Spacer(Modifier.height(2.dp))

                // Penghasilan Bersih — panel tonal, warna ikut tanda (rugi = error)
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
                tint = color.copy(alpha = 0.7f),
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
            formatRupiahDenganTanda(amount),
            style = MaterialTheme.typography.bodyMedium.tabularNums(),
            color = color,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/** Format dengan tanda minus konsisten: -Rp1.250 (tidak dobel minus). */
private fun formatRupiahDenganTanda(amount: Long): String {
    val nominal = formatRupiah(amount) // sudah menangani tanda negatif
    return if (amount > 0) "- $nominal" else nominal
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
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp).padding(top = 1.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "Fee admin dihitung dari harga sebelum diskon (voucher ditanggung Shopee) " +
                "sesuai kategori produk, ditambah biaya proses pesanan Rp1.250.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
    }
}

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
