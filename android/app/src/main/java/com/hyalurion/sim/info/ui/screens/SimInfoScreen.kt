package com.hyalurion.sim.info.ui.screens

import android.annotation.SuppressLint
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
import androidx.compose.ui.res.stringResource

@SuppressLint("LocalContextGetResourceValueCall")
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
                text = stringResource(R.string.sim_slot, slotIndex),
                style = MiuixTheme.textStyles.headline1,
                color = MiuixTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Basic Info Section
            SmallTitle(text = stringResource(R.string.section_basic_info))

            InfoListItem(
                title = stringResource(R.string.carrier_name),
                value = simInfo.carrierName ?: stringResource(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.carrierName) }
            )

            // Country/Region - Display full country name
            InfoListItem(
                title = stringResource(R.string.country_region),
                value = simInfo.countryName ?: simInfo.countryIso ?: stringResource(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.countryName ?: simInfo.countryIso) }
            )

            // ISO Country Code
            InfoListItem(
                title = stringResource(R.string.iso_country_code),
                value = simInfo.countryIso ?: stringResource(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.countryIso) }
            )

            // MCC
            InfoListItem(
                title = stringResource(R.string.mobile_country_code),
                value = simInfo.mcc ?: stringResource(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.mcc) }
            )

            InfoListItem(
                title = stringResource(R.string.mobile_network_code),
                value = simInfo.mnc ?: stringResource(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.mnc) }
            )

            InfoListItem(
                title = stringResource(R.string.phone_number),
                value = simInfo.phoneNumber ?: stringResource(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.phoneNumber) }
            )

            InfoListItem(
                title = stringResource(R.string.sim_state),
                value = simInfo.simState ?: stringResource(R.string.not_available),
                showCopyIcon = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Network Status Section
            SmallTitle(text = stringResource(R.string.section_network_status))

            InfoListItem(
                title = stringResource(R.string.network_type),
                value = simInfo.networkType ?: stringResource(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.networkType) }
            )

            val roamingStatus = when (simInfo.isNetworkRoaming) {
                true -> stringResource(R.string.roaming_yes)
                false -> stringResource(R.string.roaming_no)
                null -> stringResource(R.string.not_available)
            }
            InfoListItem(
                title = stringResource(R.string.network_roaming),
                value = roamingStatus,
                showCopyIcon = false
            )

            InfoListItem(
                title = stringResource(R.string.data_activity),
                value = simInfo.dataActivity ?: stringResource(R.string.not_available),
                showCopyIcon = false
            )

            InfoListItem(
                title = stringResource(R.string.data_state),
                value = simInfo.dataState ?: stringResource(R.string.not_available),
                showCopyIcon = false
            )

            val voipStatus = when (simInfo.isVoipAvailable) {
                true -> stringResource(R.string.voip_supported)
                false -> stringResource(R.string.voip_not_supported)
                null -> stringResource(R.string.not_available)
            }
            InfoListItem(
                title = stringResource(R.string.voip_support),
                value = voipStatus,
                showCopyIcon = false
            )

            InfoListItem(
                title = stringResource(R.string.signal_strength),
                value = simInfo.signalStrength ?: stringResource(R.string.not_available),
                onCopy = { copyToClipboard(context, simInfo.signalStrength) }
            )

            // Cell Identity Section
            if (simInfo.cellIdentity != null) {
                Spacer(modifier = Modifier.height(12.dp))

                SmallTitle(text = stringResource(R.string.section_cell_identity))

                CellIdentitySection(simInfo.cellIdentity)
            }

            // Neighboring Cell Info
            if (!simInfo.neighboringCellInfo.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                SmallTitle(text = stringResource(R.string.section_neighboring_cells))

                InfoListItem(
                    title = stringResource(R.string.neighboring_cell_info),
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
            title = stringResource(R.string.cell_type),
            value = type,
            showCopyIcon = false
        )
    }

    // LAC (GSM/WCDMA) or TAC (LTE/NR)
    val lacTac = cellIdentity.lac ?: cellIdentity.tac
    if (lacTac != null) {
        val label = if (cellIdentity.tac != null) {
            stringResource(R.string.tac)
        } else {
            stringResource(R.string.lac)
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
            cellIdentity.nci != null -> stringResource(R.string.nci)
            cellIdentity.ci != null -> stringResource(R.string.ci)
            else -> stringResource(R.string.cid)
        }
        InfoListItem(
            title = label,
            value = ciCid,
            onCopy = { copyToClipboard(context, ciCid) }
        )
    }

    cellIdentity.pci?.let { pci ->
        InfoListItem(
            title = stringResource(R.string.pci),
            value = pci,
            onCopy = { copyToClipboard(context, pci) }
        )
    }

    cellIdentity.psc?.let { psc ->
        InfoListItem(
            title = stringResource(R.string.psc),
            value = psc,
            onCopy = { copyToClipboard(context, psc) }
        )
    }

    cellIdentity.earfcn?.let { earfcn ->
        InfoListItem(
            title = stringResource(R.string.earfcn),
            value = earfcn,
            onCopy = { copyToClipboard(context, earfcn) }
        )
    }

    cellIdentity.uarfcn?.let { uarfcn ->
        InfoListItem(
            title = stringResource(R.string.uarfcn),
            value = uarfcn,
            onCopy = { copyToClipboard(context, uarfcn) }
        )
    }

    cellIdentity.nrarfcn?.let { nrarfcn ->
        InfoListItem(
            title = stringResource(R.string.nrarfcn),
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
