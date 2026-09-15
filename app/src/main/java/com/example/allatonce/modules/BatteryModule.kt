package com.example.allatonce.modules

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.*
import com.example.allatonce.ui.components.ModulePanel

@Composable
fun BatteryModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var level by remember { mutableIntStateOf(0) }
    var temperature by remember { mutableFloatStateOf(0f) }
    var voltage by remember { mutableIntStateOf(0) }
    var isCharging by remember { mutableStateOf(false) }
    var chargePlug by remember { mutableStateOf("") }
    var health by remember { mutableStateOf("Unknown") }

    DisposableEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    val lvl = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    level = if (scale > 0) (lvl * 100) / scale else lvl
                    temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                    voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                    isCharging = plugged != 0
                    chargePlug = when (plugged) {
                        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                        else -> "None"
                    }
                    health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)) {
                        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
                        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                        else -> "Unknown"
                    }
                    status = ModuleStatus.LIVE
                }
            }
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(receiver, filter)
            onDispose { context.unregisterReceiver(receiver) }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            onDispose {}
        }
    }

    ModulePanel(label = "Battery", status = status, modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "$level%",
                style = MaterialTheme.typography.displaySmall,
                color = when {
                    level <= 15 -> AlertRed
                    level <= 30 -> AmberAccent
                    else -> GreenActive
                }
            )
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(
                    text = if (isCharging) "⚡ $chargePlug" else "Discharging",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCharging) GreenActive else TextDim
                )
                Text(
                    text = "${temperature}°C  ${voltage}mV",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Battery bar
        Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
            drawRoundRect(color = PanelBorder, size = Size(size.width, size.height), cornerRadius = CornerRadius.Zero)
            val barColor = when {
                level <= 15 -> AlertRed
                level <= 30 -> AmberAccent
                else -> GreenActive
            }
            drawRoundRect(color = barColor, size = Size(size.width * (level / 100f), size.height), cornerRadius = CornerRadius.Zero)
        }
        Text("Health: $health", style = MaterialTheme.typography.bodySmall, color = TextDim)
    }
}
