package com.hyalurion.sim.info.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hyalurion.sim.info.R
import com.hyalurion.sim.info.data.CellIdentityInfo
import com.hyalurion.sim.info.data.SimInfo
import com.hyalurion.sim.info.data.SimInfoManager
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun SimInfoScreen(
    hasPermission: Boolean = false,
    onRequestPermission: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCarrierModifier: () -> Unit = {}
) {
    val context = LocalContext.current
    var simInfoList by remember { mutableStateOf<List<SimInfo>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val scrollBehavior = MiuixScrollBehavior()

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            onRequestPermission()
        }
    }

    LaunchedEffect(hasPermission) @androidx.annotation.RequiresPermission(android.Manifest.permission.READ_PHONE_STATE) {
        if (hasPermission) {
            try {
                val simInfoManager = SimInfoManager(context)
                @SuppressLint("MissingPermission")
                val infoList = simInfoManager.getSimInfoList()
                if (infoList.isEmpty()) {
                    error = context.getString(R.string.no_sim_card)
                } else {
                    simInfoList = infoList
                }
            } catch (_: Exception) {
                error = context.getString(R.string.error_occurred)
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
            error = context.getString(R.string.permission_required)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(R.string.app_name),
                largeTitle = stringResource(R.string.app_name),
                navigationIcon = {},
                actions = {
                    IconButton(onClick = onNavigateToCarrierModifier) {
                        Icon(
                            imageVector = MiuixIcons.Edit,
                            contentDescription = stringResource(R.string.carrier_modifier_title),
                            tint = MiuixTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = MiuixIcons.Settings,
                            contentDescription = stringResource(R.string.tab_settings),
                            tint = MiuixTheme.colorScheme.onBackground
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = paddingValues.calculateTopPadding()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = context.getString(R.string.loading),
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = paddingValues.calculateTopPadding()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error!!,
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (!hasPermission) {
                            Card(
                                onClick = onRequestPermission,
                                showIndication = true,
                                pressFeedbackType = PressFeedbackType.Sink
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = context.getString(R.string.grant_permission),
                                        style = MiuixTheme.textStyles.body1,
                                        color = MiuixTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                simInfoList != null -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                        contentPadding = PaddingValues(
                            top = paddingValues.calculateTopPadding() + 8.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(simInfoList!!.size) { index ->
                            SimInfoCard(simInfo = simInfoList!![index], slotIndex = index + 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimInfoCard(simInfo: SimInfo, slotIndex: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Page-level SIM heading stays outside the card, separate from the in-card section labels.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.sim_slot, slotIndex),
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.primary
            )

            simInfo.simState?.let { state ->
                Text(
                    text = localizedSimState(state),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val sections = buildInfoSections(simInfo)
                sections.forEachIndexed { index, section ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    val title = section.title
                    if (title != null) {
                        // Align section labels with the text inside the cells, not with the card edge.
                        SmallTitle(
                            text = title,
                            insideMargin = PaddingValues(12.dp, 2.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    InfoGrid(items = section.items)
                }
            }
        }
    }
}

// A compact label/value pair. Short values share a row; long values span the full width.
private data class InfoItem(
    val label: String,
    val value: String,
    val copyValue: String? = null,
    val isUnavailable: Boolean = false,
    val fullWidth: Boolean = false,
    val numeric: Boolean = false
)

private data class InfoSection(
    val title: String?,
    val items: List<InfoItem>
)

private fun infoItem(
    label: String,
    rawValue: String?,
    notAvailable: String,
    fullWidth: Boolean = false,
    numeric: Boolean = false,
    copyable: Boolean = true
): InfoItem = InfoItem(
    label = label,
    value = rawValue ?: notAvailable,
    copyValue = if (copyable && !rawValue.isNullOrBlank()) rawValue else null,
    isUnavailable = rawValue.isNullOrBlank(),
    fullWidth = fullWidth,
    numeric = numeric
)

@Composable
private fun buildInfoSections(simInfo: SimInfo): List<InfoSection> {
    val na = stringResource(R.string.not_available)
    val sections = mutableListOf<InfoSection>()

    val basicTitle = stringResource(R.string.section_basic_info)
    val carrierLabel = stringResource(R.string.carrier_name)
    val countryLabel = stringResource(R.string.country_region)
    val isoLabel = stringResource(R.string.iso_country_code)
    val mccLabel = stringResource(R.string.mobile_country_code)
    val mncLabel = stringResource(R.string.mobile_network_code)
    val phoneLabel = stringResource(R.string.phone_number)
    sections.add(
        InfoSection(
            title = basicTitle,
            items = listOf(
                infoItem(carrierLabel, simInfo.carrierName, na),
                infoItem(countryLabel, simInfo.countryName ?: simInfo.countryIso, na),
                infoItem(isoLabel, simInfo.countryIso, na, numeric = true),
                infoItem(mccLabel, simInfo.mcc, na, numeric = true),
                infoItem(mncLabel, simInfo.mnc, na, numeric = true),
                infoItem(phoneLabel, simInfo.phoneNumber, na, fullWidth = true, numeric = true)
            )
        )
    )

    val networkTitle = stringResource(R.string.section_network_status)
    val networkTypeLabel = stringResource(R.string.network_type)
    val roamingLabel = stringResource(R.string.network_roaming)
    val roamingValue = when (simInfo.isNetworkRoaming) {
        true -> stringResource(R.string.roaming_yes)
        false -> stringResource(R.string.roaming_no)
        null -> na
    }
    val dataActivityLabel = stringResource(R.string.data_activity)
    val dataStateLabel = stringResource(R.string.data_state)
    val voipLabel = stringResource(R.string.voip_support)
    val voipValue = when (simInfo.isVoipAvailable) {
        true -> stringResource(R.string.voip_supported)
        false -> stringResource(R.string.voip_not_supported)
        null -> na
    }
    val signalLabel = stringResource(R.string.signal_strength)
    sections.add(
        InfoSection(
            title = networkTitle,
            items = listOf(
                infoItem(networkTypeLabel, simInfo.networkType, na),
                InfoItem(
                    label = roamingLabel,
                    value = roamingValue,
                    isUnavailable = simInfo.isNetworkRoaming == null
                ),
                infoItem(dataActivityLabel, simInfo.dataActivity, na, copyable = false),
                infoItem(dataStateLabel, simInfo.dataState, na, copyable = false),
                InfoItem(
                    label = voipLabel,
                    value = voipValue,
                    isUnavailable = simInfo.isVoipAvailable == null
                ),
                infoItem(signalLabel, simInfo.signalStrength, na, fullWidth = true)
            )
        )
    )

    val cellIdentity = simInfo.cellIdentity
    if (cellIdentity != null) {
        val cellItems = buildCellIdentityItems(cellIdentity, na)
        if (cellItems.isNotEmpty()) {
            sections.add(
                InfoSection(
                    title = stringResource(R.string.section_cell_identity),
                    items = cellItems
                )
            )
        }
    }

    val neighbors = simInfo.neighboringCellInfo
    if (!neighbors.isNullOrBlank()) {
        // The label already names the section, so no separate SmallTitle is needed.
        sections.add(
            InfoSection(
                title = null,
                items = listOf(
                    infoItem(
                        label = stringResource(R.string.neighboring_cell_info),
                        rawValue = neighbors,
                        notAvailable = na,
                        fullWidth = true
                    )
                )
            )
        )
    }

    return sections
}

@Composable
private fun buildCellIdentityItems(cellIdentity: CellIdentityInfo, notAvailable: String): List<InfoItem> {
    val items = mutableListOf<InfoItem>()

    val typeLabel = stringResource(R.string.cell_type)
    cellIdentity.type?.let { items.add(InfoItem(label = typeLabel, value = it)) }

    val lacTac = cellIdentity.lac ?: cellIdentity.tac
    if (lacTac != null) {
        val label = if (cellIdentity.tac != null) {
            stringResource(R.string.tac)
        } else {
            stringResource(R.string.lac)
        }
        items.add(infoItem(label, lacTac, notAvailable, numeric = true))
    }

    val ciCid = cellIdentity.ci ?: cellIdentity.cid ?: cellIdentity.nci
    if (ciCid != null) {
        val label = when {
            cellIdentity.nci != null -> stringResource(R.string.nci)
            cellIdentity.ci != null -> stringResource(R.string.ci)
            else -> stringResource(R.string.cid)
        }
        items.add(infoItem(label, ciCid, notAvailable, numeric = true))
    }

    val pciLabel = stringResource(R.string.pci)
    cellIdentity.pci?.let { items.add(infoItem(pciLabel, it, notAvailable, numeric = true)) }

    val pscLabel = stringResource(R.string.psc)
    cellIdentity.psc?.let { items.add(infoItem(pscLabel, it, notAvailable, numeric = true)) }

    val earfcnLabel = stringResource(R.string.earfcn)
    cellIdentity.earfcn?.let { items.add(infoItem(earfcnLabel, it, notAvailable, numeric = true)) }

    val uarfcnLabel = stringResource(R.string.uarfcn)
    cellIdentity.uarfcn?.let { items.add(infoItem(uarfcnLabel, it, notAvailable, numeric = true)) }

    val nrarfcnLabel = stringResource(R.string.nrarfcn)
    cellIdentity.nrarfcn?.let { items.add(infoItem(nrarfcnLabel, it, notAvailable, numeric = true)) }

    return items
}

@Composable
private fun InfoGrid(items: List<InfoItem>) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // Two columns on phones, three on tablets/foldables and wide windows.
        val columns = if (maxWidth >= 600.dp) 3 else 2
        val rows = buildInfoRows(items, columns)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { rowItems ->
                val fullWidthRow = rowItems.size == 1 && rowItems[0].fullWidth
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cells always split the row evenly, so partial rows never leave an empty slot.
                    rowItems.forEach { item ->
                        val cellModifier = if (fullWidthRow) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.weight(1f)
                        }
                        InfoTile(
                            item = item,
                            modifier = cellModifier
                                .heightIn(min = 62.dp)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

// Pack short cells into rows while keeping long cells on their own row, preserving field order.
private fun buildInfoRows(items: List<InfoItem>, columns: Int): List<List<InfoItem>> {
    val rows = mutableListOf<List<InfoItem>>()
    var pending = mutableListOf<InfoItem>()

    // A single leftover cell spans the whole row instead of leaving half of it blank.
    fun flush() {
        if (pending.isEmpty()) return
        rows.add(
            if (pending.size == 1) listOf(pending[0].copy(fullWidth = true)) else pending
        )
        pending = mutableListOf()
    }

    items.forEach { item ->
        if (item.fullWidth) {
            flush()
            rows.add(listOf(item))
        } else {
            pending.add(item)
            if (pending.size == columns) {
                flush()
            }
        }
    }

    flush()
    return rows
}

@Composable
private fun InfoTile(item: InfoItem, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val copyValue = item.copyValue
    val clickModifier = if (copyValue != null) {
        Modifier.clickable { copyToClipboard(context, copyValue) }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MiuixTheme.colorScheme.surfaceContainerHighest,
        contentColor = MiuixTheme.colorScheme.onSurfaceContainerHighest,
        // surfaceContainerHigh equals surfaceContainer in dark mode; the outline keeps cells visible.
        border = BorderStroke(1.dp, MiuixTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(clickModifier)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.label,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.value,
                    modifier = Modifier.weight(1f),
                    style = MiuixTheme.textStyles.body1,
                    fontWeight = FontWeight.Medium,
                    fontFamily = if (item.numeric) FontFamily.Monospace else null,
                    color = if (item.isUnavailable) {
                        MiuixTheme.colorScheme.onSurfaceContainerVariant
                    } else {
                        MiuixTheme.colorScheme.onSurfaceContainerHighest
                    },
                    maxLines = if (item.fullWidth) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (copyValue != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = MiuixIcons.Copy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(14.dp),
                        tint = MiuixTheme.colorScheme.onSurfaceContainerVariant
                    )
                }
            }
        }
    }
}

// TelephonyManager returns English state strings; reuse the existing localized resources.
@Composable
private fun localizedSimState(state: String): String = when (state) {
    "Absent" -> stringResource(R.string.sim_state_absent)
    "PIN Required" -> stringResource(R.string.sim_state_pin_required)
    "PUK Required" -> stringResource(R.string.sim_state_puk_required)
    "Network Locked" -> stringResource(R.string.sim_state_network_locked)
    "Ready" -> stringResource(R.string.sim_state_ready)
    "Not Ready" -> stringResource(R.string.sim_state_not_ready)
    "Permanently Disabled" -> stringResource(R.string.sim_state_perm_disabled)
    "Card IO Error" -> stringResource(R.string.sim_state_card_io_error)
    "Card Restricted" -> stringResource(R.string.sim_state_card_restricted)
    else -> state
}

private fun copyToClipboard(context: android.content.Context, text: String?) {
    if (!text.isNullOrBlank()) {
        val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("SIM Info", text)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(context, context.getString(R.string.copy_success), Toast.LENGTH_SHORT).show()
    }
}
