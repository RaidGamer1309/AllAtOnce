package com.example.allatonce.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.allatonce.modules.*
import com.example.allatonce.state.AppState
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.PanelBackground
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
    // Build permission list based on Android version
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

    // Helper to check individual permission grants
    fun hasPermission(permission: String): Boolean {
        return permissionsState.permissions.find { it.permission == permission }?.status?.isGranted == true
    }

    val panelMode = appState.panelMode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PanelBackground)
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val allPermissionsGranted = permissionsState.allPermissionsGranted

        // Manifesto + control
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

        // === MOTION SENSORS ===
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AccelerometerModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            GyroscopeModule(panelMode = panelMode, modifier = Modifier.weight(1f))
        }

        MagnetometerModule(panelMode = panelMode)

        GravityModule(panelMode = panelMode)

        // === ENVIRONMENT SENSORS ===
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BarometerModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            LightModule(panelMode = panelMode, modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ProximityModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            TemperatureHumidityModule(panelMode = panelMode, modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HallEffectModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            ColorTemperatureModule(panelMode = panelMode, modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FlickerSensorModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            ToFSensorModule(panelMode = panelMode, modifier = Modifier.weight(1f))
        }

        // === PERMISSION-REQUIRED MODULES ===
        StepCounterModule(
            panelMode = panelMode,
            hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hasPermission(Manifest.permission.ACTIVITY_RECOGNITION)
            } else true
        )

        CameraPreviewModule(
            panelMode = panelMode,
            hasPermission = hasPermission(Manifest.permission.CAMERA)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FlashlightModule(
                panelMode = panelMode,
                hasPermission = hasPermission(Manifest.permission.CAMERA),
                modifier = Modifier.weight(1f)
            )
            SpeakerModule(panelMode = panelMode, modifier = Modifier.weight(1f))
        }

        MicrophoneModule(
            panelMode = panelMode,
            hasPermission = hasPermission(Manifest.permission.RECORD_AUDIO)
        )

        HapticsModule(panelMode = panelMode)

        // === LOCATION & CONNECTIVITY ===
        GpsModule(
            panelMode = panelMode,
            hasPermission = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        )

        BatteryModule(panelMode = panelMode)

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

        // === DEVICE INFO & UTILITIES ===
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StorageModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            ScreenInfoModule(panelMode = panelMode, modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WakeLockModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            BiometricModule(panelMode = panelMode, modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TouchSensorModule(panelMode = panelMode, modifier = Modifier.weight(1f))
            ThermistorModule(panelMode = panelMode, modifier = Modifier.weight(1f))
        }

        GamepadModule(
            panelMode = panelMode,
            lastGamepadEvent = lastGamepadEvent
        )

        ClipboardModule(panelMode = panelMode)

        HardwareSummaryModule(panelMode = panelMode)

        // Bottom spacing
        Spacer(modifier = Modifier.height(32.dp))
    }
}
