package com.example.allatonce.modules

import android.content.ComponentName
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.AmberAccent
import com.example.allatonce.theme.ElectricCyan
import com.example.allatonce.theme.GreenActive
import com.example.allatonce.theme.PanelSurface
import com.example.allatonce.theme.TextDim
import com.example.allatonce.ui.components.ModulePanel
import com.nothing.ketchum.Common
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphManager

@Composable
fun GlyphLightModule(
    panelMode: PanelMode,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val isNothingDevice = remember {
        try {
            Build.MANUFACTURER.equals("Nothing", ignoreCase = true) ||
            Build.BRAND.equals("Nothing", ignoreCase = true) ||
            Common.is20111() || Common.is22111() ||
            Common.is23111() || Common.is23113() ||
            Common.is24111() || Common.is25111() ||
            Common.is25111p() || Common.is25131()
        } catch (_: Throwable) {
            Build.MANUFACTURER.equals("Nothing", ignoreCase = true)
        }
    }

    val modelName = remember {
        try {
            when {
                Common.is20111() -> "Nothing Phone (1)"
                Common.is22111() -> "Nothing Phone (2)"
                Common.is23111() -> "Nothing Phone (2a)"
                Common.is23113() -> "Nothing Phone (2a) Plus"
                Common.is24111() -> "Nothing Phone (3)"
                Common.is25111() || Common.is25111p() -> "Nothing Phone (3a)"
                Common.is25131() -> "Nothing Phone (3a) Plus"
                Build.MANUFACTURER.equals("Nothing", ignoreCase = true) -> "Nothing Phone (${Build.MODEL})"
                else -> "${Build.MANUFACTURER} (${Build.MODEL})"
            }
        } catch (_: Throwable) {
            Build.MODEL
        }
    }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var isConnected by remember { mutableStateOf(false) }
    var activePattern by remember { mutableStateOf("OFF") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var glyphManagerInstance by remember { mutableStateOf<GlyphManager?>(null) }

    DisposableEffect(panelMode, isNothingDevice) {
        if (!isNothingDevice) {
            status = ModuleStatus.NOT_SUPPORTED
            return@DisposableEffect onDispose {}
        }

        if (panelMode != PanelMode.LIVE) {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            return@DisposableEffect onDispose {}
        }

        var gm: GlyphManager? = null
        try {
            gm = GlyphManager.getInstance(context.applicationContext)
            glyphManagerInstance = gm

            val callback = object : GlyphManager.Callback {
                override fun onServiceConnected(componentName: ComponentName?) {
                    try {
                        val targetDevice = when {
                            Common.is20111() -> Glyph.DEVICE_20111
                            Common.is22111() -> Glyph.DEVICE_22111
                            Common.is23111() -> Glyph.DEVICE_23111
                            Common.is23113() -> Glyph.DEVICE_23113
                            Common.is24111() -> Glyph.DEVICE_24111
                            Common.is25111() -> Glyph.DEVICE_25111
                            Common.is25111p() -> Glyph.DEVICE_25111p
                            Common.is25131() -> Glyph.DEVICE_25131
                            else -> Glyph.DEVICE_25111
                        }
                        gm.register(targetDevice)
                        gm.openSession()
                        isConnected = true
                        status = ModuleStatus.LIVE
                        errorMsg = null
                    } catch (e: Exception) {
                        errorMsg = e.message ?: "Open session failed"
                        status = ModuleStatus.ERROR
                    }
                }

                override fun onServiceDisconnected(componentName: ComponentName?) {
                    isConnected = false
                    status = ModuleStatus.DORMANT
                }
            }

            gm.init(callback)
        } catch (e: Exception) {
            errorMsg = e.message
            status = ModuleStatus.ERROR
        }

        onDispose {
            try {
                gm?.turnOff()
                gm?.closeSession()
            } catch (_: Exception) {}
            try {
                gm?.unInit()
            } catch (_: Exception) {}
            glyphManagerInstance = null
            isConnected = false
            activePattern = "OFF"
        }
    }

    ModulePanel(
        label = "Glyph Interface (Nothing OS)",
        status = status,
        modifier = modifier
    ) {
        if (!isNothingDevice) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "STATUS: N/A — NON-NOTHING HARDWARE",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmberAccent
                )
                Text(
                    text = "Detected: $modelName. Glyph LED matrix is exclusive to Nothing Phone hardware.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDim
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DEVICE: $modelName",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan
                        )
                        Text(
                            text = if (isConnected) "SERVICE: CONNECTED · ACTIVE: $activePattern"
                                   else if (errorMsg != null) "ERR: $errorMsg"
                                   else "SERVICE: BINDING TO GLYPH SERVICE...",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isConnected) GreenActive else AmberAccent
                        )
                    }
                }

                // Control Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GlyphButton(
                        text = "ALL ON",
                        active = activePattern == "ALL ON",
                        accentColor = GreenActive,
                        modifier = Modifier.weight(1f)
                    ) {
                        glyphManagerInstance?.let { gm ->
                            try {
                                val builder = gm.glyphFrameBuilder
                                val frame = builder.buildChannelA()
                                    .buildChannelB()
                                    .buildChannelC()
                                    .buildChannelD()
                                    .build()
                                gm.toggle(frame)
                                activePattern = "ALL ON"
                            } catch (e: Exception) {
                                errorMsg = e.message
                            }
                        }
                    }

                    GlyphButton(
                        text = "PULSE",
                        active = activePattern == "PULSE",
                        accentColor = ElectricCyan,
                        modifier = Modifier.weight(1f)
                    ) {
                        glyphManagerInstance?.let { gm ->
                            try {
                                val builder = gm.glyphFrameBuilder
                                val frame = builder.buildChannelA()
                                    .buildChannelB()
                                    .buildChannelC()
                                    .buildPeriod(2000)
                                    .buildCycles(10)
                                    .buildInterval(150)
                                    .build()
                                gm.animate(frame)
                                activePattern = "PULSE"
                            } catch (e: Exception) {
                                errorMsg = e.message
                            }
                        }
                    }

                    GlyphButton(
                        text = "OFF",
                        active = activePattern == "OFF",
                        accentColor = Color(0xFFEF4444),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        glyphManagerInstance?.let { gm ->
                            try {
                                gm.turnOff()
                                activePattern = "OFF"
                            } catch (e: Exception) {
                                errorMsg = e.message
                            }
                        }
                    }
                }

                // Individual zone toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GlyphButton(
                        text = "ZONE A",
                        active = activePattern == "ZONE A",
                        accentColor = GreenActive,
                        modifier = Modifier.weight(1f)
                    ) {
                        glyphManagerInstance?.let { gm ->
                            try {
                                val frame = gm.glyphFrameBuilder.buildChannelA().build()
                                gm.toggle(frame)
                                activePattern = "ZONE A"
                            } catch (e: Exception) {
                                errorMsg = e.message
                            }
                        }
                    }

                    GlyphButton(
                        text = "ZONE B",
                        active = activePattern == "ZONE B",
                        accentColor = GreenActive,
                        modifier = Modifier.weight(1f)
                    ) {
                        glyphManagerInstance?.let { gm ->
                            try {
                                val frame = gm.glyphFrameBuilder.buildChannelB().build()
                                gm.toggle(frame)
                                activePattern = "ZONE B"
                            } catch (e: Exception) {
                                errorMsg = e.message
                            }
                        }
                    }

                    GlyphButton(
                        text = "ZONE C",
                        active = activePattern == "ZONE C",
                        accentColor = GreenActive,
                        modifier = Modifier.weight(1f)
                    ) {
                        glyphManagerInstance?.let { gm ->
                            try {
                                val frame = gm.glyphFrameBuilder.buildChannelC().build()
                                gm.toggle(frame)
                                activePattern = "ZONE C"
                            } catch (e: Exception) {
                                errorMsg = e.message
                            }
                        }
                    }
                }

                if (!isConnected) {
                    Text(
                        text = "Tip: If lights don't ignite on Nothing OS, enable debug once via ADB:\n'adb shell settings put global nt_glyph_interface_debug_enable 1'",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDim
                    )
                }
            }
        }
    }
}

@Composable
private fun GlyphButton(
    text: String,
    active: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (active) accentColor.copy(alpha = 0.25f) else PanelSurface)
            .border(
                width = 1.dp,
                color = if (active) accentColor else TextDim.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) accentColor else Color.White
        )
    }
}
