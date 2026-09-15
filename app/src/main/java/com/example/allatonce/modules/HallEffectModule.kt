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
 * Hall Effect Sensor Module
 *
 * Detects magnetic fields from accessories like magnetic flip covers or docks.
 * Android does not expose a dedicated "Hall Effect" sensor type, but some OEMs
 * expose it as an uncalibrated magnetometer or a device-specific sensor.
 * We detect support by scanning the sensor list for any sensor whose name or
 * vendor string contains "hall".
 */
@Composable
fun HallEffectModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // Try to find a hall sensor from the device's full sensor list
    val hallSensor = remember {
        sensorManager.getSensorList(Sensor.TYPE_ALL).firstOrNull { sensor ->
            sensor.name.contains("hall", ignoreCase = true) ||
            sensor.stringType.contains("hall", ignoreCase = true)
        }
    }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var detected by remember { mutableStateOf("—") }
    var sensorName by remember { mutableStateOf("") }

    LaunchedEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            if (hallSensor == null) {
                status = ModuleStatus.NOT_SUPPORTED
            } else {
                sensorName = hallSensor.name
                detected = "Sensor found"
                status = ModuleStatus.LIVE
            }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
        }
    }

    ModulePanel(label = "Hall Effect", status = status, modifier = modifier) {
        Text(
            text = sensorName,
            style = MaterialTheme.typography.bodyLarge,
            color = GreenActive
        )
        Text(
            text = "Detects magnetic covers / docks",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
    }
}
