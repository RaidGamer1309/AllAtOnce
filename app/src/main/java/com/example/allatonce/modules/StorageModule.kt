package com.example.allatonce.modules

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.*
import com.example.allatonce.ui.components.ModulePanel

@Composable
fun StorageModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var totalGb by remember { mutableFloatStateOf(0f) }
    var freeGb by remember { mutableFloatStateOf(0f) }
    var usedGb by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            try {
                val stat = StatFs(Environment.getDataDirectory().path)
                totalGb = stat.totalBytes / (1024f * 1024f * 1024f)
                freeGb = stat.availableBytes / (1024f * 1024f * 1024f)
                usedGb = totalGb - freeGb
                status = ModuleStatus.LIVE
            } catch (_: Exception) {
                status = ModuleStatus.ERROR
            }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
        }
    }

    ModulePanel(label = "Internal Storage", status = status, modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "${String.format(java.util.Locale.US, "%.1f", usedGb)} / ${String.format(java.util.Locale.US, "%.1f", totalGb)} GB",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Text(
                text = "${String.format(java.util.Locale.US, "%.1f", freeGb)} GB free",
                style = MaterialTheme.typography.bodyMedium,
                color = GreenActive
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        val usedPercent = if (totalGb > 0) usedGb / totalGb else 0f
        Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
            drawRoundRect(color = PanelBorder, size = Size(size.width, size.height), cornerRadius = CornerRadius.Zero)
            val barColor = when {
                usedPercent > 0.9f -> AlertRed
                usedPercent > 0.75f -> AmberAccent
                else -> SecondaryBlue
            }
            drawRoundRect(color = barColor, size = Size(size.width * usedPercent, size.height), cornerRadius = CornerRadius.Zero)
        }
    }
}
