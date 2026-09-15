package com.example.allatonce.modules

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.*
import com.example.allatonce.ui.components.ModulePanel
import kotlin.math.atan2

@Composable
fun MagnetometerModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val magSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }
    val rotSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var heading by remember { mutableFloatStateOf(0f) }
    var magX by remember { mutableFloatStateOf(0f) }
    var magY by remember { mutableFloatStateOf(0f) }
    var magZ by remember { mutableFloatStateOf(0f) }

    val rotationMatrix = remember { FloatArray(9) }
    val orientation = remember { FloatArray(3) }

    DisposableEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            if (magSensor == null) {
                status = ModuleStatus.NOT_SUPPORTED
                return@DisposableEffect onDispose {}
            }

            val listeners = mutableListOf<SensorEventListener>()

            val magListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    magX = event.values[0]
                    magY = event.values[1]
                    magZ = event.values[2]
                    status = ModuleStatus.LIVE
                }
                override fun onAccuracyChanged(s: Sensor?, a: Int) {}
            }
            sensorManager.registerListener(magListener, magSensor, SensorManager.SENSOR_DELAY_UI)
            listeners.add(magListener)

            if (rotSensor != null) {
                val rotListener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        heading = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        if (heading < 0) heading += 360f
                    }
                    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                }
                sensorManager.registerListener(rotListener, rotSensor, SensorManager.SENSOR_DELAY_UI)
                listeners.add(rotListener)
            }

            onDispose {
                listeners.forEach { sensorManager.unregisterListener(it) }
            }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            onDispose {}
        }
    }

    ModulePanel(label = "Magnetometer / Compass", status = status, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${String.format(java.util.Locale.US, "%.0f", heading)}°",
                    style = MaterialTheme.typography.displaySmall,
                    color = TextPrimary
                )
                Text(
                    text = cardinalDirection(heading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmberAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "X:${String.format(java.util.Locale.US, "%.1f", magX)} Y:${String.format(java.util.Locale.US, "%.1f", magY)} Z:${String.format(java.util.Locale.US, "%.1f", magZ)} µT",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
                )
            }

            // Compass needle — real motion representing real data
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(56.dp)) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val radius = size.minDimension / 2 - 4

                    // Outer ring
                    drawCircle(
                        color = PanelBorder,
                        radius = radius,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                    )

                    // Rotating needle
                    rotate(-heading) {
                        // North (red)
                        drawLine(
                            color = AlertRed,
                            start = Offset(cx, cy),
                            end = Offset(cx, cy - radius + 6),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                        // South (dim)
                        drawLine(
                            color = TextDim,
                            start = Offset(cx, cy),
                            end = Offset(cx, cy + radius - 6),
                            strokeWidth = 2f,
                            cap = StrokeCap.Round
                        )
                    }

                    // Center dot
                    drawCircle(color = TextPrimary, radius = 3f)
                }
            }
        }
    }
}

private fun cardinalDirection(degrees: Float): String {
    return when {
        degrees < 22.5f || degrees >= 337.5f -> "N"
        degrees < 67.5f -> "NE"
        degrees < 112.5f -> "E"
        degrees < 157.5f -> "SE"
        degrees < 202.5f -> "S"
        degrees < 247.5f -> "SW"
        degrees < 292.5f -> "W"
        else -> "NW"
    }
}
