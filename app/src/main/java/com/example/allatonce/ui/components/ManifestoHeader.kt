package com.example.allatonce.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PanelSurface,
        border = BorderStroke(1.dp, PanelBorder),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ALL AT ONCE",
                style = MaterialTheme.typography.displayLarge,
                color = AmberAccent,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "DEVICE TELEMETRY & HARDWARE COCKPIT",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Condensed manifesto points
            val manifestoPoints = listOf(
                "One instrument, not forty apps — every module on one screen.",
                "Simultaneous, not sequential — all hardware reading together.",
                "Consent is loud — all permissions requested at once.",
                "Raw numbers over gauges — actual units and real jitter.",
                "Nothing leaves the device — zero telemetry, zero cloud."
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

            Spacer(modifier = Modifier.height(12.dp))

            // Loud Permission Banner if permissions needed
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
                        .height(48.dp)
                ) {
                    Text(
                        text = "⚡ GRANT HARDWARE ACCESS (ALL AT ONCE)",
                        style = MaterialTheme.typography.titleMedium,
                        color = PanelBackground
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Master Start / Stop control
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
                            .height(44.dp)
                    ) {
                        Text(
                            text = "⏹ STOP ALL SYSTEMS",
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
                            .height(44.dp)
                    ) {
                        Text(
                            text = "▶ WAKE ALL SYSTEMS",
                            style = MaterialTheme.typography.titleMedium,
                            color = PanelBackground
                        )
                    }
                }
            }
        }
    }
}
