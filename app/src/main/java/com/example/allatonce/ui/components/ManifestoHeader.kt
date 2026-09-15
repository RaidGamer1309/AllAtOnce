package com.example.allatonce.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.*

@Composable
fun ManifestoHeader(
    panelMode: PanelMode,
    allPermissionsGranted: Boolean,
    onRequestPermissions: () -> Unit,
    onWake: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Live pulse animation for telemetry status beacon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_alpha"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PanelSurface,
        border = BorderStroke(1.dp, PanelBorderCyan),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header title bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ALLATONCE",
                    style = MaterialTheme.typography.displaySmall,
                    color = ElectricCyan
                )
                Surface(
                    color = Color(0x1F00E5FF),
                    border = BorderStroke(0.5.dp, Color(0x6600E5FF)),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = "RIG v1.0 · 33 CH",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "HARDWARE TELEMETRY & DIAGNOSTIC COCKPIT",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stitch Live Status Pulse Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (panelMode == PanelMode.LIVE) Color(0xFF0F1A17) else Color(0xFF191612),
                border = BorderStroke(
                    1.dp,
                    if (panelMode == PanelMode.LIVE) Color(0x4D00E676) else Color(0x4DFFB300)
                ),
                shape = RoundedCornerShape(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pulsing beacon dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .alpha(if (panelMode == PanelMode.LIVE) pulseAlpha else 1f)
                                .background(
                                    if (panelMode == PanelMode.LIVE) GreenActive else AmberAccent,
                                    CircleShape
                                )
                        )
                        Text(
                            text = if (panelMode == PanelMode.LIVE) "ALL SENSORS LIVE (33/33)" else "SENSORS STANDBY / PAUSED",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (panelMode == PanelMode.LIVE) GreenActive else AmberAccent
                        )
                    }

                    Text(
                        text = if (panelMode == PanelMode.LIVE) "240 Hz SYNC" else "IDLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (panelMode == PanelMode.LIVE) ElectricCyan else TextDim
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Concise telemetry principles
            val manifestoPoints = listOf(
                "Unified Instrument — 33 physical sensors on one screen.",
                "Simultaneous Concurrency — hardware polled in parallel.",
                "Zero Cloud Exfiltration — 100% on-device volatile telemetry."
            )

            manifestoPoints.forEach { point ->
                Text(
                    text = "▸ $point",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Permission Request Banner (if any runtime permissions needed)
            if (!allPermissionsGranted) {
                Button(
                    onClick = onRequestPermissions,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberAccent,
                        contentColor = PanelBackground
                    ),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "⚡ GRANT HARDWARE ACCESS (ALL AT ONCE)",
                        style = MaterialTheme.typography.titleMedium,
                        color = PanelBackground
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Master Telemetry Controls
            when (panelMode) {
                PanelMode.LIVE -> {
                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertRed,
                            contentColor = TextPrimary
                        ),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text(
                            text = "⏹ STOP ALL SENSORS",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                }
                PanelMode.STOPPED, PanelMode.DORMANT -> {
                    Button(
                        onClick = onWake,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenActive,
                            contentColor = PanelBackground
                        ),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text(
                            text = "▶ WAKE ALL SENSORS",
                            style = MaterialTheme.typography.titleMedium,
                            color = PanelBackground
                        )
                    }
                }
            }
        }
    }
}
