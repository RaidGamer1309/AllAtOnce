package com.example.allatonce.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.theme.*

/**
 * Reusable instrument panel module — Stitch Cybernetic Telemetry specification.
 * Displays a micro-label with hex indicator, status pill, and responsive content area.
 */
@Composable
fun ModulePanel(
    label: String,
    status: ModuleStatus,
    statusMessage: String = "",
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val borderColor = when (status) {
        ModuleStatus.DORMANT, ModuleStatus.STOPPED -> PanelBorder
        ModuleStatus.LIVE -> Color(0x6600E5FF) // Electric cyan hairline glow
        ModuleStatus.PERMISSION_DENIED -> Color(0x99FF1744)
        ModuleStatus.NOT_SUPPORTED -> BorderUnsupported
        ModuleStatus.ERROR -> AlertRed
    }

    val statusIndicatorColor = when (status) {
        ModuleStatus.LIVE -> GreenActive
        ModuleStatus.PERMISSION_DENIED -> AlertRed
        ModuleStatus.NOT_SUPPORTED -> TextDim
        ModuleStatus.ERROR -> AlertRed
        ModuleStatus.STOPPED -> AmberAccent
        else -> TextDim
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (status == ModuleStatus.LIVE) PanelSurfaceRaised else PanelSurface,
        border = BorderStroke(1.dp, borderColor),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // Header row: tactical micro-indicator + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "▸ ",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (status == ModuleStatus.LIVE) ElectricCyan else TextDim
                    )
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (status == ModuleStatus.LIVE) TextPrimary else TextMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = when (status) {
                        ModuleStatus.LIVE -> Color(0x1F00E676)
                        ModuleStatus.PERMISSION_DENIED -> Color(0x1FFF1744)
                        ModuleStatus.ERROR -> Color(0x1FFF1744)
                        ModuleStatus.STOPPED -> Color(0x1FFFFB300)
                        else -> Color(0x1F5A6578)
                    },
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(
                        0.5.dp,
                        when (status) {
                            ModuleStatus.LIVE -> Color(0x6600E676)
                            ModuleStatus.PERMISSION_DENIED -> Color(0x66FF1744)
                            ModuleStatus.ERROR -> Color(0x66FF1744)
                            ModuleStatus.STOPPED -> Color(0x66FFB300)
                            else -> Color(0x335A6578)
                        }
                    )
                ) {
                    Text(
                        text = when (status) {
                            ModuleStatus.DORMANT -> "○ IDLE"
                            ModuleStatus.LIVE -> "● LIVE"
                            ModuleStatus.PERMISSION_DENIED -> "⚠ DENIED"
                            ModuleStatus.NOT_SUPPORTED -> "✕ N/A"
                            ModuleStatus.ERROR -> "⚠ ERR"
                            ModuleStatus.STOPPED -> "⏹ STOP"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = statusIndicatorColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Content area
            when (status) {
                ModuleStatus.DORMANT, ModuleStatus.STOPPED -> {
                    Text(
                        text = "STANDBY",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextDim
                    )
                }
                ModuleStatus.PERMISSION_DENIED -> {
                    Text(
                        text = "Permission required",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlertRed
                    )
                    if (statusMessage.isNotBlank()) {
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim
                        )
                    }
                }
                ModuleStatus.NOT_SUPPORTED -> {
                    Text(
                        text = "Sensor not detected on chip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDim
                    )
                    if (statusMessage.isNotBlank()) {
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim
                        )
                    }
                }
                ModuleStatus.ERROR -> {
                    Text(
                        text = statusMessage.ifBlank { "Error reading hardware bus" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlertRed
                    )
                }
                ModuleStatus.LIVE -> {
                    content()
                }
            }
        }
    }
}

/**
 * Big numeric readout with cybernetic contrast.
 */
@Composable
fun ReadoutValue(
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            color = valueColor
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = unit,
            style = MaterialTheme.typography.labelMedium,
            color = ElectricCyan,
            modifier = Modifier.padding(bottom = 3.dp)
        )
    }
}

/**
 * Multi-axis readout (X/Y/Z) with live horizontal deflection meters.
 */
@Composable
fun AxisReadout(
    x: Float,
    y: Float,
    z: Float,
    unit: String,
    format: String = "%.2f"
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        AxisRow("X", x, unit, format)
        AxisRow("Y", y, unit, format)
        AxisRow("Z", z, unit, format)
    }
}

@Composable
private fun AxisRow(axis: String, value: Float, unit: String, format: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = axis,
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
                modifier = Modifier.width(16.dp)
            )
            // Micro horizontal telemetry deflection bar
            val normalized = ((value.coerceIn(-20f, 20f) + 20f) / 40f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .padding(horizontal = 6.dp)
                    .background(Color(0xFF1F242D))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(normalized)
                        .background(if (kotlin.math.abs(value) > 12f) AmberAccent else ElectricCyan)
                )
            }
        }
        Text(
            text = "${String.format(java.util.Locale.US, format, value)} $unit",
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary
        )
    }
}
