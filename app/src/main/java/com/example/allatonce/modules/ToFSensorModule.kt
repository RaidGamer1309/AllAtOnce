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
 * Laser / 3D ToF (Time-of-Flight) Sensor Module
 *
 * Fires infrared pulses to calculate depth and distance for fast camera
 * autofocus and portrait mode separation. Some devices expose a ToF or
 * depth sensor through the Android Sensor framework.
 */
@Composable
fun ToFSensorModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // Scan for ToF / depth / laser AF sensors
    val tofSensor = remember {
        sensorManager.getSensorList(Sensor.TYPE_ALL).firstOrNull { sensor ->
            val nameL = sensor.name.lowercase()
            val typeL = sensor.stringType.lowercase()
            nameL.contains("tof") || nameL.contains("time-of-flight") ||
            nameL.contains("time_of_flight") || nameL.contains("depth") ||
            nameL.contains("laser") ||
            typeL.contains("tof") || typeL.contains("depth") ||
            typeL.contains("laser")
        }
    }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var sensorName by remember { mutableStateOf("") }
    var sensorVendor by remember { mutableStateOf("") }

    LaunchedEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            if (tofSensor == null) {
                status = ModuleStatus.NOT_SUPPORTED
            } else {
                sensorName = tofSensor.name
                sensorVendor = tofSensor.vendor
                status = ModuleStatus.LIVE
            }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
        }
    }

    ModulePanel(
        label = "Laser / ToF Depth",
        status = status,
        statusMessage = "ToF depth sensors are common on flagship cameras",
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
            text = "IR depth / laser autofocus",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
    }
}
