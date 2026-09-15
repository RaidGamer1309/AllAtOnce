package com.example.allatonce.modules

import android.content.Context
import android.os.PowerManager
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
fun WakeLockModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var isLocked by remember { mutableStateOf(false) }
    val wakeLock = remember {
        powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
            "AllAtOnce:WakeLock"
        )
    }

    LaunchedEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            status = ModuleStatus.LIVE
        } else {
            if (isLocked) {
                try { wakeLock.release() } catch (_: Exception) {}
                isLocked = false
            }
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isLocked) {
                try { wakeLock.release() } catch (_: Exception) {}
            }
        }
    }

    ModulePanel(label = "Screen Wake Lock", status = status, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = if (isLocked) "HELD" else "RELEASED",
                style = MaterialTheme.typography.displaySmall,
                color = if (isLocked) AmberAccent else TextDim
            )
            Button(
                onClick = {
                    if (isLocked) {
                        try { wakeLock.release() } catch (_: Exception) {}
                        isLocked = false
                    } else {
                        try { wakeLock.acquire(10 * 60 * 1000L) } catch (_: Exception) {}
                        isLocked = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLocked) AlertRed else AmberAccent,
                    contentColor = PanelBackground
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = if (isLocked) "RELEASE" else "HOLD",
                    style = MaterialTheme.typography.labelLarge,
                    color = PanelBackground
                )
            }
        }
    }
}
