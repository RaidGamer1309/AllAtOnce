package com.example.allatonce.modules

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.TextDim
import com.example.allatonce.theme.TextPrimary
import com.example.allatonce.ui.components.ModulePanel
import com.example.allatonce.ui.components.ReadoutValue

@Composable
fun BarometerModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var pressure by remember { mutableFloatStateOf(0f) }

    DisposableEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            if (sensor == null) {
                status = ModuleStatus.NOT_SUPPORTED
                return@DisposableEffect onDispose {}
            }
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    pressure = event.values[0]
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

    ModulePanel(label = "Barometer", status = status, modifier = modifier) {
        ReadoutValue(value = String.format(java.util.Locale.US, "%.1f", pressure), unit = "hPa")
        Spacer(modifier = Modifier.height(2.dp))
        val altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure)
        Text(
            text = "≈ ${String.format(java.util.Locale.US, "%.0f", altitude)} m altitude (est.)",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
    }
}
