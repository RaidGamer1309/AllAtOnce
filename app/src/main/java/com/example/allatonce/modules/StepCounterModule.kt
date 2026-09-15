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
import com.example.allatonce.theme.AmberAccent
import com.example.allatonce.theme.TextDim
import com.example.allatonce.ui.components.ModulePanel
import com.example.allatonce.ui.components.ReadoutValue

@Composable
fun StepCounterModule(
    panelMode: PanelMode,
    hasPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val stepCounterSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) }
    val stepDetectorSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var totalSteps by remember { mutableFloatStateOf(0f) }
    var stepsDetected by remember { mutableIntStateOf(0) }

    DisposableEffect(panelMode, hasPermission) {
        if (panelMode == PanelMode.LIVE) {
            if (!hasPermission) {
                status = ModuleStatus.PERMISSION_DENIED
                return@DisposableEffect onDispose {}
            }
            if (stepCounterSensor == null && stepDetectorSensor == null) {
                status = ModuleStatus.NOT_SUPPORTED
                return@DisposableEffect onDispose {}
            }
            val listeners = mutableListOf<SensorEventListener>()
            if (stepCounterSensor != null) {
                val cl = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        totalSteps = event.values[0]
                        status = ModuleStatus.LIVE
                    }
                    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                }
                sensorManager.registerListener(cl, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
                listeners.add(cl)
            }
            if (stepDetectorSensor != null) {
                val dl = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        stepsDetected++
                        status = ModuleStatus.LIVE
                    }
                    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                }
                sensorManager.registerListener(dl, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI)
                listeners.add(dl)
            }
            onDispose { listeners.forEach { sensorManager.unregisterListener(it) } }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            onDispose {}
        }
    }

    ModulePanel(label = "Step Counter", status = status, modifier = modifier) {
        ReadoutValue(value = String.format(java.util.Locale.US, "%.0f", totalSteps), unit = "steps (total)")
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Session: $stepsDetected steps detected",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
    }
}
