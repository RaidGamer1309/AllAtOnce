package com.example.allatonce.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
 * Reusable instrument panel module — square-cornered bordered panel.
 * Displays a small label in the top-left corner and the main content area.
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
        ModuleStatus.DORMANT, ModuleStatus.STOPPED -> BorderDormant
        ModuleStatus.LIVE -> BorderLive
        ModuleStatus.PERMISSION_DENIED -> BorderDenied
        ModuleStatus.NOT_SUPPORTED -> BorderUnsupported
        ModuleStatus.ERROR -> AlertRed
    }

    val statusIndicatorColor = when (status) {
        ModuleStatus.LIVE -> GreenActive
        ModuleStatus.PERMISSION_DENIED -> AlertRed
        ModuleStatus.NOT_SUPPORTED -> TextDim
        ModuleStatus.ERROR -> AlertRed
        else -> TextDim
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PanelSurface,
        border = BorderStroke(1.dp, borderColor),
        shape = MaterialTheme.shapes.small // RectangleShape
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // Header row: label + status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (status == ModuleStatus.LIVE) GreenActive else TextDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = when (status) {
                        ModuleStatus.DORMANT -> "IDLE"
                        ModuleStatus.LIVE -> "LIVE"
                        ModuleStatus.PERMISSION_DENIED -> "DENIED"
                        ModuleStatus.NOT_SUPPORTED -> "N/A"
                        ModuleStatus.ERROR -> "ERR"
                        ModuleStatus.STOPPED -> "STOP"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = statusIndicatorColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Content area
            when (status) {
                ModuleStatus.DORMANT, ModuleStatus.STOPPED -> {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.displaySmall,
                        color = TextDim
                    )
                }
                ModuleStatus.PERMISSION_DENIED -> {
                    Text(
                        text = "Permission denied",
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
                        text = "Not available on this device",
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
                        text = statusMessage.ifBlank { "Error reading sensor" },
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
 * Big numeric readout for use inside a ModulePanel when LIVE.
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
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = TextDim,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

/**
 * Multi-axis readout (X/Y/Z) for motion sensors.
 */
@Composable
fun AxisReadout(
    x: Float,
    y: Float,
    z: Float,
    unit: String,
    format: String = "%.2f"
) {
    Column {
        AxisRow("X", x, unit, format)
        AxisRow("Y", y, unit, format)
        AxisRow("Z", z, unit, format)
    }
}

@Composable
private fun AxisRow(axis: String, value: Float, unit: String, format: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$axis:",
            style = MaterialTheme.typography.bodyMedium,
            color = TextDim
        )
        Text(
            text = "${String.format(java.util.Locale.US, format, value)} $unit",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}
