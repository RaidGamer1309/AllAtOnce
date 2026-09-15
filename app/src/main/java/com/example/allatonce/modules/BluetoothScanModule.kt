package com.example.allatonce.modules

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
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

data class BleDevice(val name: String, val address: String, val rssi: Int)

@SuppressLint("MissingPermission")
@Composable
fun BluetoothScanModule(
    panelMode: PanelMode,
    hasPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bluetoothManager = remember { context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager }
    val scanner = remember { bluetoothManager?.adapter?.bluetoothLeScanner }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var devices by remember { mutableStateOf<Map<String, BleDevice>>(emptyMap()) }

    DisposableEffect(panelMode, hasPermission) {
        if (panelMode == PanelMode.LIVE) {
            if (scanner == null) {
                status = if (bluetoothManager?.adapter == null) ModuleStatus.NOT_SUPPORTED
                else ModuleStatus.ERROR
                return@DisposableEffect onDispose {}
            }
            if (!hasPermission) {
                status = ModuleStatus.PERMISSION_DENIED
                return@DisposableEffect onDispose {}
            }

            val callback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    val name = result.device.name ?: "(unnamed)"
                    val addr = result.device.address
                    devices = devices + (addr to BleDevice(name, addr, result.rssi))
                    status = ModuleStatus.LIVE
                }
                override fun onScanFailed(errorCode: Int) {
                    status = ModuleStatus.ERROR
                }
            }

            try {
                scanner.startScan(callback)
            } catch (_: Exception) {
                status = ModuleStatus.ERROR
            }

            onDispose {
                try { scanner.stopScan(callback) } catch (_: Exception) {}
            }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            devices = emptyMap()
            onDispose {}
        }
    }

    ModulePanel(
        label = "Bluetooth LE Scan",
        status = status,
        statusMessage = if (scanner == null) "BLE scanner not available" else "",
        modifier = modifier
    ) {
        if (devices.isEmpty()) {
            Text("Scanning...", style = MaterialTheme.typography.bodyMedium, color = TextDim)
        } else {
            Text(
                "${devices.size} devices found",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                devices.values.sortedByDescending { it.rssi }.take(5).forEach { dev ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dev.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text(
                            text = "${dev.rssi} dBm",
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                dev.rssi > -50 -> GreenActive
                                dev.rssi > -70 -> AmberAccent
                                else -> AlertRed
                            }
                        )
                    }
                }
            }
        }
    }
}
