package com.example.allatonce.modules

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.*
import com.example.allatonce.ui.components.ModulePanel

/**
 * Color Temperature / Multi-Spectral Sensor Module
 *
 * Reads ambient color balance and light spectrum to dynamically adjust
 * screen warmth (like True Tone on iPhones / similar tech on Android).
 * Some OEMs expose spectral or color-temperature sensors as vendor-specific
 * sensor types. We scan the sensor list for any matching names.
 */
@Composable
fun ColorTemperatureModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // Scan for spectral / color temperature / multi-spectral sensors
    val spectralSensor = remember {
        sensorManager.getSensorList(Sensor.TYPE_ALL).firstOrNull { sensor ->
            val nameL = sensor.name.lowercase()
            val typeL = sensor.stringType.lowercase()
            nameL.contains("color") || nameL.contains("spectral") ||
            nameL.contains("cct") || nameL.contains("chromaticity") ||
            typeL.contains("color") || typeL.contains("spectral") ||
            typeL.contains("cct")
        }
    }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var sensorName by remember { mutableStateOf("") }
    var sensorVendor by remember { mutableStateOf("") }

    LaunchedEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            if (spectralSensor == null) {
                status = ModuleStatus.NOT_SUPPORTED
            } else {
                sensorName = spectralSensor.name
                sensorVendor = spectralSensor.vendor
                status = ModuleStatus.LIVE
            }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
        }
    }

    ModulePanel(
        label = "Color Temperature",
        status = status,
        statusMessage = "Multi-spectral / CCT sensors are OEM-specific",
        modifier = modifier
    ) {
        Text(
            text = sensorName,
            style = MaterialTheme.typography.bodyLarge,
            color = GreenActive
        )
        Text(
            text = "Vendor: $sensorVendor",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
        Text(
            text = "Ambient color / white balance sensing",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
    }
}
