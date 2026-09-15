package com.example.allatonce.modules

import android.content.Context
import androidx.biometric.BiometricManager
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
fun BiometricModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val biometricManager = remember { BiometricManager.from(context) }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var capability by remember { mutableStateOf("") }
    var capabilityColor by remember { mutableStateOf(TextDim) }

    LaunchedEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            val result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            when (result) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    capability = "Fingerprint sensor: present + enrolled"
                    capabilityColor = GreenActive
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    capability = "Fingerprint sensor: not present"
                    capabilityColor = TextDim
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    capability = "Fingerprint sensor: present, unavailable"
                    capabilityColor = AmberAccent
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    capability = "Fingerprint sensor: present, not enrolled"
                    capabilityColor = AmberAccent
                }
                else -> {
                    capability = "Fingerprint sensor: unknown state"
                    capabilityColor = TextDim
                }
            }
            status = ModuleStatus.LIVE
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
        }
    }

    ModulePanel(label = "Biometric Sensor", status = status, modifier = modifier) {
        Text(
            text = capability,
            style = MaterialTheme.typography.bodyLarge,
            color = capabilityColor
        )
        Text(
            text = "Capability check only — no biometric data read",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
    }
}
