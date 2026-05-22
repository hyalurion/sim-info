package com.hyalurion.sim.info.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme
import android.widget.Toast
import com.hyalurion.sim.info.R
import com.hyalurion.sim.info.data.CellIdentityInfo
import com.hyalurion.sim.info.data.SimInfo
import com.hyalurion.sim.info.data.SimInfoManager

@Composable
fun SimInfoScreen(
    hasPermission: Boolean = false,
    onRequestPermission: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    var simInfoList by remember { mutableStateOf<List<SimInfo>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Request permission on first launch
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            onRequestPermission()
        }
    }

    // Load SIM info when permission state changes
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            try {
                val simInfoManager = SimInfoManager(context)
                val infoList = simInfoManager.getSimInfoList()
                if (infoList.isEmpty()) {
                    error = context.getString(R.string.no_sim_card)
                } else {
                    simInfoList = infoList
                }
            } catch (e: Exception) {
                error = context.getString(R.string.error_occurred)
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
            error = context.getString(R.string.permission_required)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = context.getString(R.string.loading))
                }
            }
            error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = error!!)
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Retry permission request button
                    if (!hasPermission) {
                        BasicComponent(
                            title = context.getString(R.string.grant_permission),
                            onClick = onRequestPermission
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Settings button in error state
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = MiuixIcons.Settings,
                            contentDescription = "Settings",
                            tint = MiuixTheme.colorScheme.primary
                        )
                    }
                }
            }
            simInfoList != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Settings button at the top
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = MiuixIcons.Settings,
                                contentDescription = "Settings",
                                tint = MiuixTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    simInfoList!!.forEachIndexed { index, simInfo ->
                        SimInfoCard(simInfo = simInfo, slotIndex = index + 1)
                        
                        if (index < simInfoList!!.lastIndex) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimInfoCard(simInfo: SimInfo, slotIndex: Int) {
    val context = LocalContext.current
    
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // SIM Slot Title
            Text(
                text = context.getString(R.string.sim_slot, slotIndex),
                style = MiuixTheme.textStyles.headline1,
                color = MiuixTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Basic Info Section
            SmallTitle(text = context.getString(R.string.section_basic_info))

            InfoListItem(
                title = context.getString(R.string.carrier_name),
                value = simInfo.carrierName ?: context.getString(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.carrierName) }
            )

            // Country/Region - Display full country name
            InfoListItem(
                title = context.getString(R.string.country_region),
                value = simInfo.countryName ?: simInfo.countryIso ?: context.getString(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.countryName ?: simInfo.countryIso) }
            )

            // ISO Country Code
            InfoListItem(
                title = context.getString(R.string.iso_country_code),
                value = simInfo.countryIso ?: context.getString(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.countryIso) }
            )

            // MCC
            InfoListItem(
                title = context.getString(R.string.mobile_country_code),
                value = simInfo.mcc ?: context.getString(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.mcc) }
            )

            InfoListItem(
                title = context.getString(R.string.mobile_network_code),
                value = simInfo.mnc ?: context.getString(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.mnc) }
            )

            InfoListItem(
                title = context.getString(R.string.phone_number),
                value = simInfo.phoneNumber ?: context.getString(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.phoneNumber) }
            )

            InfoListItem(
                title = context.getString(R.string.sim_state),
                value = simInfo.simState ?: context.getString(R.string.not_available),
                showCopyIcon = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Network Status Section
            SmallTitle(text = context.getString(R.string.section_network_status))

            InfoListItem(
                title = context.getString(R.string.network_type),
                value = simInfo.networkType ?: context.getString(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.networkType) }
            )

            val roamingStatus = when (simInfo.isNetworkRoaming) {
                true -> context.getString(R.string.roaming_yes)
                false -> context.getString(R.string.roaming_no)
                null -> context.getString(R.string.not_available)
            }
            InfoListItem(
                title = context.getString(R.string.network_roaming),
                value = roamingStatus,
                showCopyIcon = false
            )

            InfoListItem(
                title = context.getString(R.string.data_activity),
                value = simInfo.dataActivity ?: context.getString(R.string.not_available),
                showCopyIcon = false
            )

            InfoListItem(
                title = context.getString(R.string.data_state),
                value = simInfo.dataState ?: context.getString(R.string.not_available),
                showCopyIcon = false
            )

            val voipStatus = when (simInfo.isVoipAvailable) {
                true -> context.getString(R.string.voip_supported)
                false -> context.getString(R.string.voip_not_supported)
                null -> context.getString(R.string.not_available)
            }
            InfoListItem(
                title = context.getString(R.string.voip_support),
                value = voipStatus,
                showCopyIcon = false
            )

            InfoListItem(
                title = context.getString(R.string.signal_strength),
                value = simInfo.signalStrength ?: context.getString(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.signalStrength) }
            )

            // Cell Identity Section
            if (simInfo.cellIdentity != null) {
                Spacer(modifier = Modifier.height(12.dp))

                SmallTitle(text = context.getString(R.string.section_cell_identity))

                CellIdentitySection(simInfo.cellIdentity)
            }

            // Neighboring Cell Info
            if (!simInfo.neighboringCellInfo.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                SmallTitle(text = context.getString(R.string.section_neighboring_cells))

                InfoListItem(
                    title = context.getString(R.string.neighboring_cell_info),
                    value = simInfo.neighboringCellInfo,
                    onCopy = { copyToClipboard(context, simInfo.neighboringCellInfo) }
                )
            }
        }
    }
}

@Composable
private fun CellIdentitySection(cellIdentity: CellIdentityInfo) {
    val context = LocalContext.current

    cellIdentity.type?.let { type ->
        InfoListItem(
            title = context.getString(R.string.cell_type),
            value = type,
            showCopyIcon = false
        )
    }

    // LAC (GSM/WCDMA) or TAC (LTE/NR)
    val lacTac = cellIdentity.lac ?: cellIdentity.tac
    if (lacTac != null) {
        val label = if (cellIdentity.tac != null) {
            context.getString(R.string.tac)
        } else {
            context.getString(R.string.lac)
        }
        InfoListItem(
            title = label,
            value = lacTac,
            onCopy = { copyToClipboard(context, lacTac) }
        )
    }

    // CI (LTE) or CID (GSM/WCDMA) or NCI (NR)
    val ciCid = cellIdentity.ci ?: cellIdentity.cid ?: cellIdentity.nci
    if (ciCid != null) {
        val label = when {
            cellIdentity.nci != null -> context.getString(R.string.nci)
            cellIdentity.ci != null -> context.getString(R.string.ci)
            else -> context.getString(R.string.cid)
        }
        InfoListItem(
            title = label,
            value = ciCid,
            onCopy = { copyToClipboard(context, ciCid) }
        )
    }

    cellIdentity.pci?.let { pci ->
        InfoListItem(
            title = context.getString(R.string.pci),
            value = pci,
            onCopy = { copyToClipboard(context, pci) }
        )
    }

    cellIdentity.psc?.let { psc ->
        InfoListItem(
            title = context.getString(R.string.psc),
            value = psc,
            onCopy = { copyToClipboard(context, psc) }
        )
    }

    cellIdentity.earfcn?.let { earfcn ->
        InfoListItem(
            title = context.getString(R.string.earfcn),
            value = earfcn,
            onCopy = { copyToClipboard(context, earfcn) }
        )
    }

    cellIdentity.uarfcn?.let { uarfcn ->
        InfoListItem(
            title = context.getString(R.string.uarfcn),
            value = uarfcn,
            onCopy = { copyToClipboard(context, uarfcn) }
        )
    }

    cellIdentity.nrarfcn?.let { nrarfcn ->
        InfoListItem(
            title = context.getString(R.string.nrarfcn),
            value = nrarfcn,
            onCopy = { copyToClipboard(context, nrarfcn) }
        )
    }
}

@Composable
private fun InfoListItem(
    title: String,
    value: String,
    onCopy: (() -> Unit)? = null,
    showCopyIcon: Boolean = true
) {
    BasicComponent(
        title = title,
        summary = value,
        onClick = if (onCopy != null && showCopyIcon) onCopy else null,
        endActions = if (showCopyIcon && onCopy != null) {
            {
                IconButton(
                    onClick = onCopy
                ) {
                    Icon(
                        imageVector = MiuixIcons.Copy,
                        contentDescription = "Copy",
                        tint = MiuixTheme.colorScheme.primary
                    )
                }
            }
        } else null
    )
}

private fun copyToClipboard(context: android.content.Context, text: String?) {
    if (!text.isNullOrBlank()) {
        val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("SIM Info", text)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(context, context.getString(R.string.copy_success), Toast.LENGTH_SHORT).show()
    }
}
