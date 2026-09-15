package com.example.allatonce.modules

import android.content.Context
import android.content.res.Configuration
import android.view.Display
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.*
import com.example.allatonce.ui.components.ModulePanel
import com.example.allatonce.ui.components.ReadoutValue

/**
 * Capacitive Touch Sensor Module
 *
 * Reports multi-touch capability of the capacitive digitizer grid over the
 * display panel. Reads max simultaneous touch points and touch screen type
 * from the device configuration.
 */
@Composable
fun TouchSensorModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val config = LocalConfiguration.current

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var maxTouchPoints by remember { mutableIntStateOf(0) }
    var touchScreenType by remember { mutableStateOf("") }
    var displayRefreshRate by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(panelMode, config) {
        if (panelMode == PanelMode.LIVE) {
            val pm = context.packageManager

            // Touch screen type
            @Suppress("DEPRECATION")
            touchScreenType = when (config.touchscreen) {
                Configuration.TOUCHSCREEN_FINGER -> "Capacitive (finger)"
                Configuration.TOUCHSCREEN_STYLUS -> "Stylus"
                Configuration.TOUCHSCREEN_NOTOUCH -> "No touch"
                else -> "Unknown"
            }

            // Multi-touch capability
            maxTouchPoints = if (pm.hasSystemFeature("android.hardware.touchscreen.multitouch.jazzhand")) {
                // 5+ points (usually 10)
                10
            } else if (pm.hasSystemFeature("android.hardware.touchscreen.multitouch.distinct")) {
                // Distinct 2+ points
                5
            } else if (pm.hasSystemFeature("android.hardware.touchscreen.multitouch")) {
                2
            } else if (pm.hasSystemFeature("android.hardware.touchscreen")) {
                1
            } else {
                0
            }

            // Display refresh rate
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            @Suppress("DEPRECATION")
            val display: Display = windowManager.defaultDisplay
            displayRefreshRate = display.refreshRate

            status = if (maxTouchPoints > 0) ModuleStatus.LIVE else ModuleStatus.NOT_SUPPORTED
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
        }
    }

    ModulePanel(label = "Touch Digitizer", status = status, modifier = modifier) {
        Text(
            text = touchScreenType,
            style = MaterialTheme.typography.bodyLarge,
            color = GreenActive
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ReadoutValue(
                value = "$maxTouchPoints",
                unit = "points"
            )
            ReadoutValue(
                value = String.format(java.util.Locale.US, "%.0f", displayRefreshRate),
                unit = "Hz"
            )
        }
        Text(
            text = "Multi-touch + display refresh rate",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
    }
}
