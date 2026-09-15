package com.example.allatonce.modules

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.ui.components.ModulePanel
import com.example.allatonce.ui.components.ReadoutValue

@Composable
fun TemperatureHumidityModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val tempSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) }
    val humidSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var temperature by remember { mutableFloatStateOf(0f) }
    var humidity by remember { mutableFloatStateOf(0f) }
    var hasTemp by remember { mutableStateOf(false) }
    var hasHumid by remember { mutableStateOf(false) }

    DisposableEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            if (tempSensor == null && humidSensor == null) {
                status = ModuleStatus.NOT_SUPPORTED
                return@DisposableEffect onDispose {}
            }
            val listeners = mutableListOf<SensorEventListener>()
            if (tempSensor != null) {
                hasTemp = true
                val tl = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        temperature = event.values[0]
                        status = ModuleStatus.LIVE
                    }
                    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                }
                sensorManager.registerListener(tl, tempSensor, SensorManager.SENSOR_DELAY_UI)
                listeners.add(tl)
            }
            if (humidSensor != null) {
                hasHumid = true
                val hl = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        humidity = event.values[0]
                        status = ModuleStatus.LIVE
                    }
                    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                }
                sensorManager.registerListener(hl, humidSensor, SensorManager.SENSOR_DELAY_UI)
                listeners.add(hl)
            }
            onDispose { listeners.forEach { sensorManager.unregisterListener(it) } }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            onDispose {}
        }
    }

    val statusMsg = buildString {
        if (tempSensor == null) append("Temperature sensor not present. ")
        if (humidSensor == null) append("Humidity sensor not present.")
    }

    ModulePanel(label = "Temperature / Humidity", status = status, statusMessage = statusMsg, modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            if (hasTemp) ReadoutValue(value = String.format(java.util.Locale.US, "%.1f", temperature), unit = "°C")
            if (hasHumid) ReadoutValue(value = String.format(java.util.Locale.US, "%.0f", humidity), unit = "%RH")
        }
    }
}
