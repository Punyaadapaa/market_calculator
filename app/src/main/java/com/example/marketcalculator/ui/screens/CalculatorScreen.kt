package com.example.marketcalculator.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marketcalculator.BuildConfig
import com.example.marketcalculator.data.CalcMode
import com.example.marketcalculator.data.KategoriFee
import com.example.marketcalculator.data.formatPersen
import com.example.marketcalculator.ui.theme.tabularNums
import com.example.marketcalculator.ui.util.ThousandsSeparatorTransformation
import com.example.marketcalculator.ui.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val uiState = viewModel.uiState

    // Track focused input fields. When count drops to zero (keyboard closed / tap outside), calculation is considered done and saved to history.
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
            // Header
            HeaderSection()

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tab selector (pill style)
                ModeSelector(mode = uiState.mode, onModeChange = viewModel::onModeChange)

                // Single form card: input + Shopee costs (design board alignment)
                AnimatedContent(
                    targetState = uiState.mode,
                    transitionSpec = {
                        val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        (slideInHorizontally(tween(250)) { w -> dir * w / 3 } + fadeIn(tween(250)))
                            .togetherWith(
                                slideOutHorizontally(tween(200)) { w -> -dir * w / 3 } + fadeOut(tween(150))
                            )
                    },
                    label = "mode_transition"
                ) { mode ->
                    val promoAktif = when (mode) {
                        CalcMode.HARGA_JUAL -> uiState.pakaiPromoXtraHargaJual
                        CalcMode.TARGET_HARGA -> uiState.pakaiPromoXtraTarget
                        CalcMode.TARGET_BERSIH -> uiState.pakaiPromoXtraBersih
                    }
                    when (mode) {
                        CalcMode.HARGA_JUAL -> KalkulatorFormCard(
                            judul = "Harga Produk",
                            labelField = "Harga Sebelum Diskon",
                            helperField = "Harga pasang sebelum voucher toko",
                            nilai = uiState.hargaAwal,
                            onNilaiChange = viewModel::onHargaAwalChange,
                            diskonPersen = uiState.diskonPersenHargaJual,
                            onDiskonChange = viewModel::onDiskonHargaJualChange,
                            pesanError = uiState.pesanError,
                            onFokusBerubah = onFokusBerubah,
                            kategori = uiState.kategoriFee,
                            onKategoriChange = viewModel::onKategoriChange,
                            promoXtraAktif = promoAktif,
                            onPromoXtraToggle = viewModel::onPromoXtraToggle,
                            pakaiShippingSaver = uiState.pakaiShippingSaver,
                            onShippingSaverToggle = viewModel::onShippingSaverToggle
                        )
                        CalcMode.TARGET_HARGA -> KalkulatorFormCard(
                            judul = "Target Pembeli",
                            labelField = "Harga Dibayar Pembeli",
                            helperField = "Harga setelah voucher, sebelum potongan Shopee",
                            nilai = uiState.targetHarga,
                            onNilaiChange = viewModel::onTargetHargaChange,
                            diskonPersen = uiState.diskonPersen,
                            onDiskonChange = viewModel::onDiskonChange,
                            pesanError = uiState.pesanError,
                            onFokusBerubah = onFokusBerubah,
                            kategori = uiState.kategoriFee,
                            onKategoriChange = viewModel::onKategoriChange,
                            promoXtraAktif = promoAktif,
                            onPromoXtraToggle = viewModel::onPromoXtraToggle,
                            pakaiShippingSaver = uiState.pakaiShippingSaver,
                            onShippingSaverToggle = viewModel::onShippingSaverToggle
                        )
                        CalcMode.TARGET_BERSIH -> KalkulatorFormCard(
                            judul = "Target Penghasilan",
                            labelField = "Penghasilan Bersih Diinginkan",
                            helperField = "Target yang diterima setelah semua potongan",
                            nilai = uiState.targetBersih,
                            onNilaiChange = viewModel::onTargetBersihChange,
                            diskonPersen = uiState.diskonPersenBersih,
                            onDiskonChange = viewModel::onDiskonBersihChange,
                            pesanError = uiState.pesanError,
                            onFokusBerubah = onFokusBerubah,
                            kategori = uiState.kategoriFee,
                            onKategoriChange = viewModel::onKategoriChange,
                            promoXtraAktif = promoAktif,
                            onPromoXtraToggle = viewModel::onPromoXtraToggle,
                            pakaiShippingSaver = uiState.pakaiShippingSaver,
                            onShippingSaverToggle = viewModel::onShippingSaverToggle
                        )
                    }
                }

                // Result summary
                RingkasanCard(
                    state = uiState,
                    onReset = viewModel::onReset
                )

                // History
                val daftarRiwayat by viewModel.riwayat.collectAsStateWithLifecycle()
                RiwayatSection(
                    daftar = daftarRiwayat,
                    onMuat = viewModel::muatDariRiwayat,
                    onHapus = viewModel::hapusRiwayat,
                    onHapusSemua = viewModel::hapusSemuaRiwayat
                )

                // App version (from BuildConfig)
                VersionFooter()

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Header

@Composable
private fun HeaderSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Rp",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "Kalkulator Seller",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Hitung profit Shopee sebelum pasang harga",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Tab selector (pill style)

@Composable
private fun ModeSelector(mode: CalcMode, onModeChange: (CalcMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PillTab(
            label = "Harga Jual",
            selected = mode == CalcMode.HARGA_JUAL,
            onClick = { onModeChange(CalcMode.HARGA_JUAL) },
            modifier = Modifier.weight(1f)
        )
        PillTab(
            label = "Target Harga",
            selected = mode == CalcMode.TARGET_HARGA,
            onClick = { onModeChange(CalcMode.TARGET_HARGA) },
            modifier = Modifier.weight(1f)
        )
        PillTab(
            label = "Target Bersih",
            selected = mode == CalcMode.TARGET_BERSIH,
            onClick = { onModeChange(CalcMode.TARGET_BERSIH) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Icon-less tab pill, so three labels fit on narrow screens without clipping.
 */
@Composable
private fun PillTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primary
        else
            Color.Transparent,
        modifier = modifier
            .heightIn(min = 44.dp)
            .semantics { semanticsSelected = selected }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Form card (input + Shopee costs)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KalkulatorFormCard(
    judul: String,
    labelField: String,
    helperField: String,
    nilai: String,
    onNilaiChange: (String) -> Unit,
    diskonPersen: String,
    onDiskonChange: (String) -> Unit,
    pesanError: String?,
    onFokusBerubah: (Boolean) -> Unit,
    kategori: KategoriFee,
    onKategoriChange: (KategoriFee) -> Unit,
    promoXtraAktif: Boolean,
    onPromoXtraToggle: (Boolean) -> Unit,
    pakaiShippingSaver: Boolean,
    onShippingSaverToggle: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                judul,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            RupiahField(
                label = labelField,
                helper = helperField,
                value = nilai,
                onValueChange = onNilaiChange,
                onFokusBerubah = onFokusBerubah
            )

            PersenField(
                value = diskonPersen,
                onValueChange = onDiskonChange,
                pesanError = pesanError,
                onFokusBerubah = onFokusBerubah,
                onSelesai = { focusManager.clearFocus() }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            BiayaShopeeSection(
                kategori = kategori,
                onKategoriChange = onKategoriChange,
                promoXtraAktif = promoXtraAktif,
                onPromoXtraToggle = onPromoXtraToggle,
                pakaiShippingSaver = pakaiShippingSaver,
                onShippingSaverToggle = onShippingSaverToggle
            )
        }
    }
}

/** Small label above an input box, per the design board. */
@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    errorContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    errorBorderColor = MaterialTheme.colorScheme.error
)

@Composable
private fun RupiahField(
    label: String,
    helper: String,
    value: String,
    onValueChange: (String) -> Unit,
    onFokusBerubah: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = { new -> onValueChange(new.filter { it.isDigit() }) },
            prefix = { Text("Rp ") },
            placeholder = { Text("0") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            visualTransformation = ThousandsSeparatorTransformation(),
            colors = fieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFokusBerubah(it.isFocused) }
        )
        Text(
            helper,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PersenField(
    value: String,
    onValueChange: (String) -> Unit,
    pesanError: String?,
    onFokusBerubah: (Boolean) -> Unit,
    onSelesai: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel("Voucher Diskon")
        OutlinedTextField(
            value = value,
            onValueChange = { new -> onValueChange(new.filter { it.isDigit() }.take(3)) },
            suffix = {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSelesai() }),
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
            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            colors = fieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFokusBerubah(it.isFocused) }
        )
        if (pesanError == null) {
            Text(
                "Diskon yang ditanggung Shopee",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Shopee costs (category + toggles)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BiayaShopeeSection(
    kategori: KategoriFee,
    onKategoriChange: (KategoriFee) -> Unit,
    promoXtraAktif: Boolean,
    onPromoXtraToggle: (Boolean) -> Unit,
    pakaiShippingSaver: Boolean,
    onShippingSaverToggle: (Boolean) -> Unit
) {
    var sheetTerbuka by remember { mutableStateOf(false) }

    KategoriRow(kategori = kategori, onKlik = { sheetTerbuka = true })

    BiayaToggleRow(
        checked = promoXtraAktif,
        onCheckedChange = onPromoXtraToggle,
        judul = "Promo XTRA",
        keterangan = "4,5% (maks. Rp60.000)",
        ikon = Icons.Outlined.Percent
    )

    BiayaToggleRow(
        checked = pakaiShippingSaver,
        onCheckedChange = onShippingSaverToggle,
        judul = "Shipping Fee Saver",
        keterangan = "Rp350 per pesanan",
        ikonTeks = "Rp"
    )

    if (sheetTerbuka) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { sheetTerbuka = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            PilihKategoriSheet(
                kategori = kategori,
                onPilih = {
                    onKategoriChange(it)
                    sheetTerbuka = false
                },
                onTutup = { sheetTerbuka = false }
            )
        }
    }
}

/** Small icon bubble in front of cost rows. */
@Composable
private fun IkonBulat(
    container: Color,
    warna: Color,
    ikon: ImageVector? = null,
    teks: String? = null
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        if (ikon != null) {
            Icon(ikon, contentDescription = null, tint = warna, modifier = Modifier.size(16.dp))
        } else {
            Text(
                teks.orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                color = warna,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun KategoriRow(kategori: KategoriFee, onKlik: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onKlik)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IkonBulat(
            container = MaterialTheme.colorScheme.primaryContainer,
            warna = MaterialTheme.colorScheme.primary,
            ikon = Icons.Outlined.Percent
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Kategori Produk",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                kategori.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(50)
        ) {
            Text(
                formatPersen(kategori.persen),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun BiayaToggleRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    judul: String,
    keterangan: String,
    ikon: ImageVector? = null,
    ikonTeks: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IkonBulat(
            container = if (checked) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHighest,
            warna = if (checked) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            ikon = ikon,
            teks = ikonTeks
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                judul,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                keterangan,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.8f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.secondary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun PilihKategoriSheet(
    kategori: KategoriFee,
    onPilih: (KategoriFee) -> Unit,
    onTutup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Pilih Kategori",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(vertical = 6.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable(onClick = onTutup),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Tutup",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            "Biaya admin mengikuti kategori produk",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        KategoriFee.entries.forEach { item ->
            val dipilih = item == kategori
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (dipilih) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                    .clickable { onPilih(item) }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IkonBulat(
                    container = if (dipilih) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                    warna = if (dipilih) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    ikon = Icons.Outlined.Percent
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = if (dipilih) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        formatPersen(item.persen),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (dipilih) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                if (dipilih) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Result card

@Composable
private fun RingkasanCard(
    state: CalculatorUiState,
    onReset: () -> Unit
) {
    val hasil = state.hasil
    val kosong = hasil.hargaWajibPasang == null &&
        hasil.hargaSetelahDiskon == null &&
        hasil.totalPotongan == 0L

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 60.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (kosong) {
                    EmptyHasil()
                } else {
                    HasilUtama(state)

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

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
                        DetailRow(label = "PPh 22 / WHT 22 (0,5%)", amount = hasil.premiumFee)
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    DetailRow(
                        label = "Total Potongan",
                        amount = hasil.totalPotongan,
                        bold = true
                    )
                }
            }

            SmallFloatingActionButton(
                onClick = onReset,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = "Reset kalkulator",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyHasil() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "=",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Isi harga di atas",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "Hasil dan rincian potongan muncul di sini.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Main result panel per mode. Panel color follows the design board:
 * green for net income, orange for the required listed price.
 */
@Composable
private fun HasilUtama(state: CalculatorUiState) {
    val hasil = state.hasil
    when (state.mode) {
        CalcMode.HARGA_JUAL -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val rugi = hasil.penghasilanBersih < 0
            PanelUtama(
                label = if (rugi) "Rugi — hasil di bawah nol" else "Penghasilan bersih",
                nilai = formatRupiah(hasil.penghasilanBersih),
                sub = "Dari ${formatRupiah(state.hargaAwal.toLongOrNull() ?: 0L)} setelah potongan",
                nilaiColor = if (rugi) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.secondary,
                container = if (rugi) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.secondaryContainer
            )
            hasil.hargaSetelahDiskon?.let {
                InfoRow(
                    label = "Harga setelah diskon ${state.diskonPersenHargaJual}%",
                    nilai = formatRupiah(it)
                )
            }
        }

        CalcMode.TARGET_HARGA -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PanelUtama(
                label = "Harga wajib pasang di Shopee",
                nilai = formatRupiah(hasil.hargaWajibPasang ?: 0L),
                sub = "Agar pembeli membayar ${formatRupiah(state.targetHarga.toLongOrNull() ?: 0L)} setelah diskon",
                nilaiColor = MaterialTheme.colorScheme.primary,
                container = MaterialTheme.colorScheme.primaryContainer
            )
            InfoRow(
                label = "Harga yang dibayar pembeli",
                nilai = formatRupiah(state.targetHarga.toLongOrNull() ?: 0L)
            )
        }

        CalcMode.TARGET_BERSIH -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PanelUtama(
                label = "Penghasilan bersih",
                nilai = formatRupiah(hasil.penghasilanBersih),
                sub = "Target yang ingin kamu terima",
                nilaiColor = MaterialTheme.colorScheme.secondary,
                container = MaterialTheme.colorScheme.secondaryContainer
            )
            InfoRow(
                label = "Harga wajib pasang di Shopee",
                nilai = formatRupiah(hasil.hargaWajibPasang ?: 0L),
                warnaNilai = MaterialTheme.colorScheme.primary
            )
            InfoRow(
                label = "Harga setelah diskon ${hasil.diskonPersenDipakai}%",
                nilai = formatRupiah(hasil.hargaSetelahDiskon ?: 0L)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Diskon yang digunakan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatPersen(hasil.diskonPersenDipakai.toDouble()),
                    style = MaterialTheme.typography.bodySmall.tabularNums(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PanelUtama(
    label: String,
    nilai: String,
    sub: String,
    nilaiColor: Color,
    container: Color
) {
    Surface(
        color = container,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(nilaiColor)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                nilai,
                style = MaterialTheme.typography.displayLarge.tabularNums(),
                color = nilaiColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Gray info row, e.g. price after discount. */
@Composable
private fun InfoRow(
    label: String,
    nilai: String,
    warnaNilai: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
            color = warnaNilai,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Detail row (cost breakdown)

@Composable
private fun DetailRow(
    label: String,
    amount: Long,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (bold) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
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

// Version footer

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
