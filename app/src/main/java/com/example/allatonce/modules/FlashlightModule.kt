package com.example.allatonce.modules

import android.content.Context
import android.hardware.camera2.CameraManager
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
fun FlashlightModule(
    panelMode: PanelMode,
    hasPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var torchOn by remember { mutableStateOf(false) }
    var hasFlash by remember { mutableStateOf(false) }
    var rearCameraId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            for (id in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)
                val flashAvail = chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE)
                val facing = chars.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                if (flashAvail == true && facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK) {
                    hasFlash = true
                    rearCameraId = id
                    break
                }
            }
        } catch (_: Exception) {}
    }

    LaunchedEffect(panelMode, hasPermission) {
        status = when {
            panelMode != PanelMode.LIVE -> {
                if (torchOn && rearCameraId != null) {
                    try { cameraManager.setTorchMode(rearCameraId!!, false) } catch (_: Exception) {}
                    torchOn = false
                }
                if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            }
            !hasFlash -> ModuleStatus.NOT_SUPPORTED
            else -> ModuleStatus.LIVE
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (torchOn && rearCameraId != null) {
                try { cameraManager.setTorchMode(rearCameraId!!, false) } catch (_: Exception) {}
            }
        }
    }

    ModulePanel(
        label = "Flashlight / Torch",
        status = status,
        statusMessage = if (!hasFlash) "No rear flash detected" else "",
        modifier = modifier
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = if (torchOn) "ON" else "OFF",
                style = MaterialTheme.typography.displaySmall,
                color = if (torchOn) AmberAccent else TextDim
            )
            Button(
                onClick = {
                    rearCameraId?.let { id ->
                        try {
                            torchOn = !torchOn
                            cameraManager.setTorchMode(id, torchOn)
                        } catch (_: Exception) {
                            torchOn = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (torchOn) AlertRed else AmberAccent,
                    contentColor = PanelBackground
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = if (torchOn) "KILL" else "FIRE",
                    style = MaterialTheme.typography.labelLarge,
                    color = PanelBackground
                )
            }
        }
    }
}
