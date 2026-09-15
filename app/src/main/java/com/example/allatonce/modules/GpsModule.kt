package com.example.allatonce.modules

import android.annotation.SuppressLint
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
import com.example.allatonce.ui.components.ReadoutValue
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@SuppressLint("MissingPermission")
@Composable
fun GpsModule(
    panelMode: PanelMode,
    hasPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var accuracy by remember { mutableFloatStateOf(0f) }
    var altitude by remember { mutableDoubleStateOf(0.0) }
    var speed by remember { mutableFloatStateOf(0f) }

    DisposableEffect(panelMode, hasPermission) {
        if (panelMode == PanelMode.LIVE && hasPermission) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 2000L
            ).setMinUpdateIntervalMillis(1000L).build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { loc ->
                        latitude = loc.latitude
                        longitude = loc.longitude
                        accuracy = loc.accuracy
                        altitude = loc.altitude
                        speed = loc.speed
                        status = ModuleStatus.LIVE
                    }
                }
            }

            try {
                fusedClient.requestLocationUpdates(locationRequest, callback, android.os.Looper.getMainLooper())
            } catch (_: Exception) {
                status = ModuleStatus.ERROR
            }

            onDispose {
                fusedClient.removeLocationUpdates(callback)
            }
        } else {
            status = when {
                panelMode != PanelMode.LIVE -> if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
                !hasPermission -> ModuleStatus.PERMISSION_DENIED
                else -> ModuleStatus.DORMANT
            }
            onDispose {}
        }
    }

    ModulePanel(label = "GPS Location", status = status, modifier = modifier) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("LAT", style = MaterialTheme.typography.labelSmall, color = TextDim)
                Text(String.format(java.util.Locale.US, "%.6f", latitude), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("LON", style = MaterialTheme.typography.labelSmall, color = TextDim)
                Text(String.format(java.util.Locale.US, "%.6f", longitude), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ReadoutValue(value = String.format(java.util.Locale.US, "%.1f", accuracy), unit = "m accuracy")
                ReadoutValue(value = String.format(java.util.Locale.US, "%.1f", speed), unit = "m/s")
            }
            Text(
                text = "Alt: ${String.format(java.util.Locale.US, "%.0f", altitude)} m",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim
            )
        }
    }
}
