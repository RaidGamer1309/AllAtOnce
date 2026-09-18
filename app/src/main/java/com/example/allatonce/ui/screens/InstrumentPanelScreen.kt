package com.example.allatonce.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.allatonce.modules.*
import com.example.allatonce.state.AppState
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.*
import com.example.allatonce.ui.components.ManifestoHeader
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun InstrumentPanelScreen(
    appState: AppState,
    lastGamepadEvent: String
) {
    val permissionList = remember {
        buildList {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.READ_PHONE_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissionList)

    fun hasPermission(permission: String): Boolean {
        return permissionsState.permissions.find { it.permission == permission }?.status?.isGranted == true
    }

    val panelMode = appState.panelMode
    val categories = listOf("ALL (33)", "MOTION (4)", "ENVIRONMENT (5)", "OPTICS & AUDIO (7)", "RADIO (5)", "SYSTEM (12)")
    var selectedCategory by remember { mutableStateOf("ALL (33)") }

    val showMotion = selectedCategory == "ALL (33)" || selectedCategory.startsWith("MOTION")
    val showEnvironment = selectedCategory == "ALL (33)" || selectedCategory.startsWith("ENVIRONMENT")
    val showOpticsAudio = selectedCategory == "ALL (33)" || selectedCategory.startsWith("OPTICS")
    val showRadio = selectedCategory == "ALL (33)" || selectedCategory.startsWith("RADIO")
    val showSystem = selectedCategory == "ALL (33)" || selectedCategory.startsWith("SYSTEM")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PanelBackground)
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val allPermissionsGranted = permissionsState.allPermissionsGranted

        // Stitch Cybernetic Header & Master Controls
        ManifestoHeader(
            panelMode = panelMode,
            allPermissionsGranted = allPermissionsGranted,
            onRequestPermissions = {
                permissionsState.launchMultiplePermissionRequest()
            },
            onWake = {
                appState.wake()
            },
            onStop = {
                appState.stop()
            }
        )

        // Stitch Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                Surface(
                    onClick = { selectedCategory = category },
                    color = if (isSelected) Color(0x2600E5FF) else PanelSurface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) ElectricCyan else PanelBorder
                    ),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) ElectricCyan else TextMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // === MOTION & INERTIAL SENSORS (4) ===
        if (showMotion) {
            SectionHeader(title = "MOTION & KINEMATICS")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AccelerometerModule(panelMode = panelMode, modifier = Modifier.weight(1f))
                GyroscopeModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            }
            MagnetometerModule(panelMode = panelMode)
            GravityModule(panelMode = panelMode)
        }

        // === ENVIRONMENT & ATMOSPHERE SENSORS (5) ===
        if (showEnvironment) {
            SectionHeader(title = "ENVIRONMENT & ATMOSPHERE")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BarometerModule(panelMode = panelMode, modifier = Modifier.weight(1f))
                LightModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ProximityModule(panelMode = panelMode, modifier = Modifier.weight(1f))
                TemperatureHumidityModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            }
            StepCounterModule(
                panelMode = panelMode,
                hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    hasPermission(Manifest.permission.ACTIVITY_RECOGNITION)
                } else true
            )
        }

        // === OPTICS & ACOUSTIC SENSORS (7) ===
        if (showOpticsAudio) {
            SectionHeader(title = "OPTICS, IMAGING & AUDIO")
            CameraPreviewModule(
                panelMode = panelMode,
                hasPermission = hasPermission(Manifest.permission.CAMERA)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ColorTemperatureModule(panelMode = panelMode, modifier = Modifier.weight(1f))
                FlickerSensorModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ToFSensorModule(panelMode = panelMode, modifier = Modifier.weight(1f))
                FlashlightModule(
                    panelMode = panelMode,
                    hasPermission = hasPermission(Manifest.permission.CAMERA),
                    modifier = Modifier.weight(1f)
                )
            }
            GlyphLightModule(panelMode = panelMode)
            MicrophoneModule(
                panelMode = panelMode,
                hasPermission = hasPermission(Manifest.permission.RECORD_AUDIO)
            )
            SpeakerModule(panelMode = panelMode)
        }

        // === CONNECTIVITY & RADIO RF (5) ===
        if (showRadio) {
            SectionHeader(title = "CONNECTIVITY & RF TRANSCEIVER")
            GpsModule(
                panelMode = panelMode,
                hasPermission = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            )
            NetworkModule(
                panelMode = panelMode,
                hasPhoneStatePermission = hasPermission(Manifest.permission.READ_PHONE_STATE)
            )
            WifiScanModule(
                panelMode = panelMode,
                hasPermission = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            )
            BluetoothScanModule(
                panelMode = panelMode,
                hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    hasPermission(Manifest.permission.BLUETOOTH_SCAN)
                } else true
            )
            NfcModule(panelMode = panelMode)
        }

        // === SYSTEM, SILICON THERMAL & BIOMETRICS (12) ===
        if (showSystem) {
            SectionHeader(title = "SYSTEM, HARDWARE & SILICON")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HallEffectModule(panelMode = panelMode, modifier = Modifier.weight(1f))
                BatteryModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            }
            HapticsModule(panelMode = panelMode)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TouchSensorModule(panelMode = panelMode, modifier = Modifier.weight(1f))
                ThermistorModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StorageModule(panelMode = panelMode, modifier = Modifier.weight(1f))
                ScreenInfoModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WakeLockModule(panelMode = panelMode, modifier = Modifier.weight(1f))
                BiometricModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            }
            GamepadModule(
                panelMode = panelMode,
                lastGamepadEvent = lastGamepadEvent
            )
            ClipboardModule(panelMode = panelMode)
            HardwareSummaryModule(panelMode = panelMode)
        }

        // Bottom status footer
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "● ALLATONCE RIG BUFFER STABLE · VOLATILE MEMORY ONLY",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "// $title",
            style = MaterialTheme.typography.labelSmall,
            color = ElectricCyan
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(PanelBorderCyan)
        )
    }
}
