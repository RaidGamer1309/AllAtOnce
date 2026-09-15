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
import com.example.allatonce.ui.components.AxisReadout
import com.example.allatonce.ui.components.ModulePanel

@Composable
fun GravityModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val gravitySensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) }
    val linearSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var gx by remember { mutableFloatStateOf(0f) }
    var gy by remember { mutableFloatStateOf(0f) }
    var gz by remember { mutableFloatStateOf(0f) }
    var lx by remember { mutableFloatStateOf(0f) }
    var ly by remember { mutableFloatStateOf(0f) }
    var lz by remember { mutableFloatStateOf(0f) }

    DisposableEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            if (gravitySensor == null && linearSensor == null) {
                status = ModuleStatus.NOT_SUPPORTED
                return@DisposableEffect onDispose {}
            }
            val listeners = mutableListOf<SensorEventListener>()

            if (gravitySensor != null) {
                val gl = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        gx = event.values[0]; gy = event.values[1]; gz = event.values[2]
                        status = ModuleStatus.LIVE
                    }
                    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                }
                sensorManager.registerListener(gl, gravitySensor, SensorManager.SENSOR_DELAY_UI)
                listeners.add(gl)
            }
            if (linearSensor != null) {
                val ll = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        lx = event.values[0]; ly = event.values[1]; lz = event.values[2]
                        status = ModuleStatus.LIVE
                    }
                    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                }
                sensorManager.registerListener(ll, linearSensor, SensorManager.SENSOR_DELAY_UI)
                listeners.add(ll)
            }

            onDispose { listeners.forEach { sensorManager.unregisterListener(it) } }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            onDispose {}
        }
    }

    ModulePanel(label = "Gravity / Linear Accel", status = status, modifier = modifier) {
        if (gravitySensor != null) {
            Text("GRAVITY", style = MaterialTheme.typography.labelSmall, color = TextDim)
            AxisReadout(x = gx, y = gy, z = gz, unit = "m/s²")
        }
        if (linearSensor != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("LINEAR", style = MaterialTheme.typography.labelSmall, color = TextDim)
            AxisReadout(x = lx, y = ly, z = lz, unit = "m/s²")
        }
    }
}
