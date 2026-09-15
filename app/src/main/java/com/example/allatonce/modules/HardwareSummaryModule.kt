package com.example.allatonce.modules

import android.app.ActivityManager
import android.content.Context
import android.os.Build
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

@Composable
fun HardwareSummaryModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }

    var model by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var device by remember { mutableStateOf("") }
    var board by remember { mutableStateOf("") }
    var soc by remember { mutableStateOf("") }
    var cpuCores by remember { mutableIntStateOf(0) }
    var totalRamMb by remember { mutableLongStateOf(0L) }
    var availRamMb by remember { mutableLongStateOf(0L) }
    var androidVersion by remember { mutableStateOf("") }
    var sdkLevel by remember { mutableIntStateOf(0) }

    LaunchedEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            model = Build.MODEL
            manufacturer = Build.MANUFACTURER
            device = Build.DEVICE
            board = Build.BOARD
            soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Build.SOC_MODEL
            } else {
                Build.HARDWARE
            }
            cpuCores = Runtime.getRuntime().availableProcessors()
            androidVersion = Build.VERSION.RELEASE
            sdkLevel = Build.VERSION.SDK_INT

            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            totalRamMb = memInfo.totalMem / (1024 * 1024)
            availRamMb = memInfo.availMem / (1024 * 1024)

            status = ModuleStatus.LIVE
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
        }
    }

    ModulePanel(label = "Hardware Summary", status = status, modifier = modifier) {
        Column {
            HwRow("Model", "$manufacturer $model")
            HwRow("Device", device)
            HwRow("Board", board)
            HwRow("SoC", soc)
            HwRow("CPU Cores", "$cpuCores")
            HwRow("RAM", "${availRamMb}MB free / ${totalRamMb}MB total")
            HwRow("Android", "$androidVersion (SDK $sdkLevel)")
        }
    }
}

@Composable
private fun HwRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextDim)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}
