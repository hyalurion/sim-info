package com.hyalurion.sim.info.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hyalurion.sim.info.R
import com.hyalurion.sim.info.data.CountryPresets
import com.hyalurion.sim.info.data.PresetCarriers
import com.hyalurion.sim.info.data.SimCardInfo
import com.hyalurion.sim.info.manager.CarrierConfigManager
import com.hyalurion.sim.info.manager.ShizukuManager
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun CarrierModifierScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val shizukuState by ShizukuManager.state.collectAsState()
    val scrollBehavior = MiuixScrollBehavior()

    var simCards by remember { mutableStateOf<List<SimCardInfo>>(emptyList()) }
    var selectedSimIndex by remember { mutableIntStateOf(0) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    var selectedCountryIndex by remember { mutableIntStateOf(0) }
    var customCountryCode by remember { mutableStateOf("") }
    var isCustomCountry by remember { mutableStateOf(false) }

    var selectedCarrierIndex by remember { mutableIntStateOf(0) }
    var customCarrierName by remember { mutableStateOf("") }
    var isCustomCarrier by remember { mutableStateOf(false) }

    LaunchedEffect(shizukuState, refreshTrigger) {
        if (shizukuState == ShizukuManager.ShizukuState.READY) {
            simCards = CarrierConfigManager.getSimCards(context)
            if (selectedSimIndex >= simCards.size) selectedSimIndex = 0
        }
    }

    val simOptions = remember(simCards) {
        simCards.map { "SIM ${it.slot} (${it.carrierName})" }
    }

    val customString = stringResource(R.string.carrier_modifier_custom)
    val countryOptions = remember(customString) {
        CountryPresets.countries.map { "${it.displayName} (${it.code})" } + listOf(customString)
    }

    val carrierOptions = remember(customString) {
        PresetCarriers.presets.map { it.name } + listOf(customString)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(R.string.carrier_modifier_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "Back",
                            tint = MiuixTheme.colorScheme.onBackground
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        when (shizukuState) {
            ShizukuManager.ShizukuState.READY -> {
                if (simCards.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = paddingValues.calculateTopPadding())
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.carrier_modifier_no_sim),
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }
                    return@Scaffold
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SmallTitle(text = stringResource(R.string.carrier_modifier_sim_select))
                        Card {
                            OverlayDropdownPreference(
                                title = stringResource(R.string.carrier_modifier_sim_card),
                                items = simOptions,
                                selectedIndex = selectedSimIndex,
                                onSelectedIndexChange = { selectedSimIndex = it }
                            )
                        }
                    }

                    val selectedSim = simCards.getOrNull(selectedSimIndex)
                    if (selectedSim != null) {
                        item {
                            SmallTitle(text = stringResource(R.string.carrier_modifier_current_config))
                            Card {
                                if (selectedSim.currentConfig.isEmpty()) {
                                    BasicComponent(
                                        title = stringResource(R.string.carrier_modifier_no_override),
                                        summary = null
                                    )
                                } else {
                                    selectedSim.currentConfig.forEach { (key, value) ->
                                        val title = when (key) {
                                            "country_code" -> stringResource(R.string.iso_country_code)
                                            "carrier_name" -> stringResource(R.string.carrier_name)
                                            else -> key
                                        }
                                        BasicComponent(title = title, summary = value)
                                    }
                                }
                            }
                        }

                        item {
                            SmallTitle(text = stringResource(R.string.carrier_modifier_country_code))
                            Card {
                                OverlayDropdownPreference(
                                    title = stringResource(R.string.carrier_modifier_select_country),
                                    items = countryOptions,
                                    selectedIndex = selectedCountryIndex,
                                    onSelectedIndexChange = { index ->
                                        selectedCountryIndex = index
                                        isCustomCountry = index == countryOptions.size - 1
                                    }
                                )
                                if (isCustomCountry) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextField(
                                        value = customCountryCode,
                                        onValueChange = {
                                            if (it.length <= 2 && it.all { char -> char.isLetter() }) {
                                                customCountryCode = it.uppercase()
                                            }
                                        },
                                        label = stringResource(R.string.carrier_modifier_custom_country_hint),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        item {
                            SmallTitle(text = stringResource(R.string.carrier_modifier_carrier_name))
                            Card {
                                OverlayDropdownPreference(
                                    title = stringResource(R.string.carrier_modifier_select_carrier),
                                    items = carrierOptions,
                                    selectedIndex = selectedCarrierIndex,
                                    onSelectedIndexChange = { index ->
                                        selectedCarrierIndex = index
                                        isCustomCarrier = index == carrierOptions.size - 1
                                        if (!isCustomCarrier) {
                                            customCarrierName = PresetCarriers.presets.getOrNull(index)?.displayName.orEmpty()
                                        }
                                    }
                                )
                                if (isCustomCarrier) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextField(
                                        value = customCarrierName,
                                        onValueChange = { customCarrierName = it },
                                        label = stringResource(R.string.carrier_modifier_custom_carrier_hint),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val result = CarrierConfigManager.resetCarrierConfig(selectedSim.subId)
                                        val msg = if (result.isSuccess) {
                                            context.getString(R.string.carrier_modifier_reset_success)
                                        } else {
                                            val err = result.exceptionOrNull()
                                            "Reset failed: ${err?.javaClass?.simpleName}: ${err?.message}"
                                        }
                                        Log.e("CarrierModifier", "Reset result: $msg")
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        if (result.isSuccess) {
                                            refreshTrigger++
                                            selectedCountryIndex = 0
                                            selectedCarrierIndex = 0
                                            isCustomCountry = false
                                            isCustomCarrier = false
                                            customCountryCode = ""
                                            customCarrierName = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = stringResource(R.string.carrier_modifier_reset))
                                }
                                Button(
                                    onClick = {
                                        val countryCode = if (isCustomCountry) {
                                            customCountryCode.takeIf { it.length == 2 }
                                        } else {
                                            CountryPresets.countries.getOrNull(selectedCountryIndex)?.code
                                        }
                                        val carrierName = if (isCustomCarrier) {
                                            customCarrierName.takeIf { it.isNotEmpty() }
                                        } else {
                                            PresetCarriers.presets.getOrNull(selectedCarrierIndex)?.displayName
                                        }
                                        val result = CarrierConfigManager.setCarrierConfig(
                                            selectedSim.subId,
                                            countryCode,
                                            carrierName
                                        )
                                        val msg = if (result.isSuccess) {
                                            context.getString(R.string.carrier_modifier_save_success)
                                        } else {
                                            val err = result.exceptionOrNull()
                                            "Save failed: ${err?.javaClass?.simpleName}: ${err?.message}"
                                        }
                                        Log.e("CarrierModifier", "Save result: $msg")
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        if (result.isSuccess) {
                                            refreshTrigger++
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColorsPrimary(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = stringResource(R.string.carrier_modifier_save))
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                ShizukuNotReadyContent(
                    state = shizukuState,
                    paddingValues = paddingValues,
                    onRequestPermission = { ShizukuManager.requestPermission() }
                )
            }
        }
    }
}

@Composable
private fun ShizukuNotReadyContent(
    state: ShizukuManager.ShizukuState,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onRequestPermission: () -> Unit
) {
    val message = when (state) {
        ShizukuManager.ShizukuState.NOT_INSTALLED -> stringResource(R.string.shizuku_not_installed)
        ShizukuManager.ShizukuState.NOT_RUNNING -> stringResource(R.string.shizuku_not_running)
        ShizukuManager.ShizukuState.WAITING_PERMISSION -> stringResource(R.string.shizuku_waiting_permission)
        ShizukuManager.ShizukuState.READY -> ""
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (state == ShizukuManager.ShizukuState.WAITING_PERMISSION || state == ShizukuManager.ShizukuState.NOT_RUNNING) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColorsPrimary()
            ) {
                Text(text = stringResource(R.string.shizuku_request_permission))
            }
        }
    }
}
