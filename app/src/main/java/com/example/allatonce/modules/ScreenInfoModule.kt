package com.example.allatonce.modules

import android.content.Context
import android.os.Build
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

@Composable
fun ScreenInfoModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var resolution by remember { mutableStateOf("") }
    var refreshRate by remember { mutableFloatStateOf(0f) }
    var densityDpi by remember { mutableIntStateOf(0) }
    var orientation by remember { mutableStateOf("") }

    LaunchedEffect(panelMode, configuration) {
        if (panelMode == PanelMode.LIVE) {
            try {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display: Display = wm.defaultDisplay
                val metrics = android.util.DisplayMetrics()
                @Suppress("DEPRECATION")
                display.getRealMetrics(metrics)
                resolution = "${metrics.widthPixels} × ${metrics.heightPixels}"
                refreshRate = display.refreshRate
                densityDpi = metrics.densityDpi
                orientation = when (configuration.orientation) {
                    android.content.res.Configuration.ORIENTATION_PORTRAIT -> "Portrait"
                    android.content.res.Configuration.ORIENTATION_LANDSCAPE -> "Landscape"
                    else -> "Undefined"
                }
                status = ModuleStatus.LIVE
            } catch (_: Exception) {
                status = ModuleStatus.ERROR
            }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
        }
    }

    ModulePanel(label = "Screen Info", status = status, modifier = modifier) {
        Column {
            DataRow("Resolution", resolution)
            DataRow("Refresh Rate", "${String.format(java.util.Locale.US, "%.0f", refreshRate)} Hz")
            DataRow("Density", "$densityDpi dpi")
            DataRow("Orientation", orientation)
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextDim)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}
