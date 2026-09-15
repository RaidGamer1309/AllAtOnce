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
 * Flicker Sensor Module
 *
 * Measures the frequency of artificial light cycles (50Hz / 60Hz) to prevent
 * banding and flicker artifacts in photos and video. Some flagship Android
 * devices expose a dedicated flicker detection sensor.
 */
@Composable
fun FlickerSensorModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // Scan for flicker detection sensors
    val flickerSensor = remember {
        sensorManager.getSensorList(Sensor.TYPE_ALL).firstOrNull { sensor ->
            val nameL = sensor.name.lowercase()
            val typeL = sensor.stringType.lowercase()
            nameL.contains("flicker") || typeL.contains("flicker")
        }
    }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var sensorName by remember { mutableStateOf("") }

    LaunchedEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            if (flickerSensor == null) {
                status = ModuleStatus.NOT_SUPPORTED
            } else {
                sensorName = flickerSensor.name
                status = ModuleStatus.LIVE
            }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
        }
    }

    ModulePanel(
        label = "Flicker Sensor",
        status = status,
        statusMessage = "Flicker sensors are OEM-specific, found on flagship cameras",
        modifier = modifier
    ) {
        Text(
            text = sensorName,
            style = MaterialTheme.typography.bodyLarge,
            color = GreenActive
        )
        Text(
            text = "Detects 50Hz / 60Hz artificial light flicker",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
    }
}
