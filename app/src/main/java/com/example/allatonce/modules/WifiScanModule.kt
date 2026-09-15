package com.example.allatonce.modules

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.*
import com.example.allatonce.ui.components.ModulePanel
import kotlinx.coroutines.delay

data class WifiNetwork(val ssid: String, val level: Int, val frequency: Int)

@SuppressLint("MissingPermission")
@Composable
fun WifiScanModule(
    panelMode: PanelMode,
    hasPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wifiManager = remember { context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var networks by remember { mutableStateOf<List<WifiNetwork>>(emptyList()) }

    LaunchedEffect(panelMode, hasPermission) {
        if (panelMode == PanelMode.LIVE && hasPermission) {
            status = ModuleStatus.LIVE
            while (true) {
                try {
                    @Suppress("DEPRECATION")
                    wifiManager.startScan()
                    @Suppress("DEPRECATION")
                    val results = wifiManager.scanResults
                    networks = results
                        .sortedByDescending { it.level }
                        .take(10)
                        .map { sr ->
                            WifiNetwork(
                                ssid = sr.SSID.ifBlank { "(hidden)" },
                                level = sr.level,
                                frequency = sr.frequency
                            )
                        }
                } catch (_: Exception) {}
                delay(10000) // Scan every 10 seconds
            }
        } else {
            status = when {
                panelMode != PanelMode.LIVE -> if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
                !hasPermission -> ModuleStatus.PERMISSION_DENIED
                else -> ModuleStatus.DORMANT
            }
        }
    }

    ModulePanel(label = "Wi-Fi Scan", status = status, modifier = modifier) {
        if (networks.isEmpty()) {
            Text("Scanning...", style = MaterialTheme.typography.bodyMedium, color = TextDim)
        } else {
            Text(
                "${networks.size} networks",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                networks.take(5).forEach { net ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = net.ssid,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text(
                            text = "${net.level} dBm",
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                net.level > -50 -> GreenActive
                                net.level > -70 -> AmberAccent
                                else -> AlertRed
                            }
                        )
                    }
                }
            }
        }
    }
}
