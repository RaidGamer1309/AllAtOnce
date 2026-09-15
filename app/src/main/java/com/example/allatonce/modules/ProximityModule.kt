package com.example.allatonce.modules

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.AmberAccent
import com.example.allatonce.theme.TextPrimary
import com.example.allatonce.ui.components.ModulePanel
import com.example.allatonce.ui.components.ReadoutValue

@Composable
fun ProximityModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var distance by remember { mutableFloatStateOf(0f) }
    var maxRange by remember { mutableFloatStateOf(0f) }

    DisposableEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            if (sensor == null) {
                status = ModuleStatus.NOT_SUPPORTED
                return@DisposableEffect onDispose {}
            }
            maxRange = sensor.maximumRange
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    distance = event.values[0]
                    status = ModuleStatus.LIVE
                }
                override fun onAccuracyChanged(s: Sensor?, a: Int) {}
            }
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            onDispose {}
        }
    }

    ModulePanel(label = "Proximity", status = status, modifier = modifier) {
        val isNear = distance < maxRange
        ReadoutValue(
            value = String.format(java.util.Locale.US, "%.1f", distance),
            unit = "cm",
            valueColor = if (isNear) AmberAccent else TextPrimary
        )
        Text(
            text = if (isNear) "NEAR" else "FAR",
            style = MaterialTheme.typography.labelLarge,
            color = if (isNear) AmberAccent else TextPrimary
        )
    }
}
