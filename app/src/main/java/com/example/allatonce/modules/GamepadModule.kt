package com.example.allatonce.modules

import android.view.InputDevice
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.*
import com.example.allatonce.ui.components.ModulePanel

@Composable
fun GamepadModule(
    panelMode: PanelMode,
    lastGamepadEvent: String,
    modifier: Modifier = Modifier
) {
    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var gamepads by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            val deviceIds = InputDevice.getDeviceIds()
            val pads = mutableListOf<String>()
            for (id in deviceIds) {
                val device = InputDevice.getDevice(id) ?: continue
                val sources = device.sources
                if (sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD ||
                    sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) {
                    pads.add(device.name)
                }
            }
            gamepads = pads
            status = ModuleStatus.LIVE
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
        }
    }

    ModulePanel(label = "Gamepad / Controller", status = status, modifier = modifier) {
        if (gamepads.isEmpty()) {
            Text("No gamepads connected", style = MaterialTheme.typography.bodyMedium, color = TextDim)
        } else {
            gamepads.forEach { name ->
                Text(text = "🎮 $name", style = MaterialTheme.typography.bodyMedium, color = GreenActive)
            }
        }
        if (lastGamepadEvent.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Last: $lastGamepadEvent",
                style = MaterialTheme.typography.bodySmall,
                color = AmberAccent
            )
        }
    }
}
