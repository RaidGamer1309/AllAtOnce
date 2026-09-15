package com.example.allatonce.modules

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
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
fun NetworkModule(
    panelMode: PanelMode,
    hasPhoneStatePermission: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val connectivityManager = remember { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var networkType by remember { mutableStateOf("None") }
    var isMetered by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }
    var signalDbm by remember { mutableIntStateOf(0) }
    var hasSignal by remember { mutableStateOf(false) }

    DisposableEffect(panelMode, hasPhoneStatePermission) {
        if (panelMode == PanelMode.LIVE) {
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                    isConnected = true
                    networkType = when {
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
                        else -> "Other"
                    }
                    isMetered = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                    status = ModuleStatus.LIVE
                }
                override fun onLost(network: Network) {
                    isConnected = false
                    networkType = "Disconnected"
                    status = ModuleStatus.LIVE
                }
            }

            val request = NetworkRequest.Builder().build()
            try {
                connectivityManager.registerNetworkCallback(request, networkCallback)
            } catch (_: Exception) {}

            // Signal strength via TelephonyManager
            @Suppress("DEPRECATION")
            var phoneListener: PhoneStateListener? = null
            if (hasPhoneStatePermission) {
                try {
                    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                    phoneListener = object : PhoneStateListener() {
                        @Deprecated("Deprecated in API 31")
                        override fun onSignalStrengthsChanged(ss: SignalStrength) {
                            try {
                                signalDbm = ss.level
                                hasSignal = true
                            } catch (_: Exception) {}
                        }
                    }
                    @Suppress("DEPRECATION")
                    telephonyManager?.listen(phoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
                } catch (_: Exception) {}
            }

            onDispose {
                try { connectivityManager.unregisterNetworkCallback(networkCallback) } catch (_: Exception) {}
                if (phoneListener != null) {
                    try {
                        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                        @Suppress("DEPRECATION")
                        tm?.listen(phoneListener, PhoneStateListener.LISTEN_NONE)
                    } catch (_: Exception) {}
                }
            }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            onDispose {}
        }
    }

    ModulePanel(label = "Network", status = status, modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = networkType,
                style = MaterialTheme.typography.displaySmall,
                color = if (isConnected) GreenActive else AlertRed
            )
            Text(
                text = if (isMetered) "METERED" else "UNMETERED",
                style = MaterialTheme.typography.labelLarge,
                color = if (isMetered) AmberAccent else TextDim
            )
        }
        if (hasSignal) {
            Text(
                text = "Signal level: $signalDbm / 4",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim
            )
        }
        if (!hasPhoneStatePermission) {
            Text(
                text = "Signal strength unavailable (READ_PHONE_STATE denied)",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim
            )
        }
    }
}
