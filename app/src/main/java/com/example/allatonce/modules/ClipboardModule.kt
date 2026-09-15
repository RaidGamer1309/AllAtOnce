package com.example.allatonce.modules

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.*
import com.example.allatonce.ui.components.ModulePanel

@Composable
fun ClipboardModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var clipContent by remember { mutableStateOf<String?>(null) }
    var hasClip by remember { mutableStateOf(false) }

    LaunchedEffect(panelMode) {
        status = when (panelMode) {
            PanelMode.LIVE -> ModuleStatus.LIVE
            PanelMode.STOPPED -> ModuleStatus.STOPPED
            PanelMode.DORMANT -> ModuleStatus.DORMANT
        }
    }

    ModulePanel(label = "Clipboard", status = status, modifier = modifier) {
        Text(
            text = "Explicit read only (Android policy)",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = {
                try {
                    hasClip = clipboardManager.hasPrimaryClip()
                    clipContent = if (hasClip) {
                        clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
                    } else null
                } catch (_: Exception) {
                    clipContent = "(Access denied by system)"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryBlue, contentColor = PanelBackground),
            shape = MaterialTheme.shapes.small
        ) {
            Text("READ CLIPBOARD", style = MaterialTheme.typography.labelLarge, color = PanelBackground)
        }
        if (clipContent != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = clipContent!!,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        } else if (hasClip) {
            Text("(Non-text content)", style = MaterialTheme.typography.bodySmall, color = TextDim)
        }
    }
}
