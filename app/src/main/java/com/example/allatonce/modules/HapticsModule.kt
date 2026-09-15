package com.example.allatonce.modules

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun HapticsModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val hasVibrator = remember { vibrator?.hasVibrator() == true }
    val hasAmplitude = remember {
        vibrator?.hasAmplitudeControl() == true
    }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }

    LaunchedEffect(panelMode) {
        status = when {
            panelMode != PanelMode.LIVE -> if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            !hasVibrator -> ModuleStatus.NOT_SUPPORTED
            else -> ModuleStatus.LIVE
        }
    }

    ModulePanel(label = "Haptics / Vibration", status = status, modifier = modifier) {
        Text(
            text = if (hasAmplitude) "Amplitude control: YES" else "Amplitude control: NO",
            style = MaterialTheme.typography.bodyMedium,
            color = if (hasAmplitude) GreenActive else TextDim
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    try {
                        vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    } catch (_: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = PanelBackground),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f)
            ) {
                Text("SHORT", style = MaterialTheme.typography.labelLarge, color = PanelBackground)
            }
            Button(
                onClick = {
                    try {
                        val pattern = longArrayOf(0, 100, 50, 100, 50, 200)
                        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } catch (_: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryBlue, contentColor = PanelBackground),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f)
            ) {
                Text("PATTERN", style = MaterialTheme.typography.labelLarge, color = PanelBackground)
            }
        }
    }
}
