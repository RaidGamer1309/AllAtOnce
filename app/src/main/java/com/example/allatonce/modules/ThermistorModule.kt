package com.example.allatonce.modules

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.*
import com.example.allatonce.ui.components.ModulePanel
import com.example.allatonce.ui.components.ReadoutValue
import java.io.BufferedReader
import java.io.FileReader

/**
 * Internal Temperature Sensors (Thermistors) Module
 *
 * Monitors internal thermal loads across the SoC, battery, and charging
 * circuitry. Uses multiple data sources:
 * - Android SensorManager for TYPE_TEMPERATURE (device temperature) if available
 * - Thermal zone readings from /sys/class/thermal/thermal_zone*/temp
 *
 * Helps assess whether the device is throttling performance due to heat.
 */
@Composable
fun ThermistorModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // Try the deprecated TYPE_TEMPERATURE (internal device temp, not ambient)
    @Suppress("DEPRECATION")
    val deviceTempSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE) }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var sensorTemp by remember { mutableFloatStateOf(0f) }
    var thermalZones by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
    var hasSensorData by remember { mutableStateOf(false) }

    DisposableEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            // Try reading thermal zones from sysfs
            val zones = readThermalZones()
            thermalZones = zones

            if (deviceTempSensor == null && zones.isEmpty()) {
                status = ModuleStatus.NOT_SUPPORTED
                return@DisposableEffect onDispose {}
            }

            if (zones.isNotEmpty()) {
                status = ModuleStatus.LIVE
            }

            val listener = if (deviceTempSensor != null) {
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        sensorTemp = event.values[0]
                        hasSensorData = true
                        status = ModuleStatus.LIVE
                    }
                    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                }
            } else null

            if (listener != null && deviceTempSensor != null) {
                sensorManager.registerListener(listener, deviceTempSensor, SensorManager.SENSOR_DELAY_UI)
            }

            onDispose {
                if (listener != null) {
                    sensorManager.unregisterListener(listener)
                }
            }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            onDispose {}
        }
    }

    ModulePanel(
        label = "Internal Thermistors",
        status = status,
        statusMessage = "Reads SoC / battery thermal sensors",
        modifier = modifier
    ) {
        if (hasSensorData) {
            ReadoutValue(
                value = String.format(java.util.Locale.US, "%.1f", sensorTemp),
                unit = "°C",
                valueColor = when {
                    sensorTemp > 45f -> AlertRed
                    sensorTemp > 38f -> AmberAccent
                    else -> GreenActive
                }
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        if (thermalZones.isNotEmpty()) {
            Text(
                text = "Thermal zones (${thermalZones.size} found):",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim
            )
            // Show up to the first 4 zones to keep the panel compact
            thermalZones.take(4).forEach { (name, temp) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f °C", temp),
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            temp > 50f -> AlertRed
                            temp > 40f -> AmberAccent
                            else -> TextPrimary
                        }
                    )
                }
            }
            if (thermalZones.size > 4) {
                Text(
                    text = "+${thermalZones.size - 4} more zones",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
                )
            }
        } else if (!hasSensorData) {
            Text(
                text = "No thermal data accessible",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim
            )
        }
    }
}

/**
 * Reads thermal zone temperatures from /sys/class/thermal/thermal_zone*/
 * Returns a list of (zone_type, temperature_celsius) pairs.
 */
private fun readThermalZones(): List<Pair<String, Float>> {
    val results = mutableListOf<Pair<String, Float>>()
    try {
        val thermalDir = java.io.File("/sys/class/thermal/")
        if (!thermalDir.exists()) return results

        thermalDir.listFiles()?.filter { it.name.startsWith("thermal_zone") }?.forEach { zone ->
            try {
                val typeFile = java.io.File(zone, "type")
                val tempFile = java.io.File(zone, "temp")
                if (typeFile.exists() && tempFile.exists()) {
                    val type = BufferedReader(FileReader(typeFile)).use { it.readLine()?.trim() ?: zone.name }
                    val tempRaw = BufferedReader(FileReader(tempFile)).use { it.readLine()?.trim()?.toFloatOrNull() }
                    if (tempRaw != null) {
                        // Thermal zone temps are usually in millidegrees
                        val tempC = if (tempRaw > 1000f) tempRaw / 1000f else tempRaw
                        if (tempC in -40f..150f) { // sanity check
                            results.add(type to tempC)
                        }
                    }
                }
            } catch (_: Exception) { /* Skip inaccessible zones */ }
        }
    } catch (_: Exception) { /* Sysfs not accessible */ }
    return results.sortedByDescending { it.second }
}
