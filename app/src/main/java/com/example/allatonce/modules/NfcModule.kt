package com.example.allatonce.modules

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
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
fun NfcModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }

    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var lastTagId by remember { mutableStateOf<String?>(null) }
    var tagCount by remember { mutableIntStateOf(0) }

    DisposableEffect(panelMode) {
        if (panelMode == PanelMode.LIVE) {
            if (nfcAdapter == null) {
                status = ModuleStatus.NOT_SUPPORTED
                return@DisposableEffect onDispose {}
            }
            if (!nfcAdapter.isEnabled) {
                status = ModuleStatus.LIVE // Show as live but indicate NFC is off
            } else {
                status = ModuleStatus.LIVE
            }

            val activity = context as? Activity
            if (activity != null && nfcAdapter.isEnabled) {
                try {
                    val callback = NfcAdapter.ReaderCallback { tag: Tag ->
                        lastTagId = tag.id.joinToString(":") { String.format(java.util.Locale.US, "%02X", it) }
                        tagCount++
                    }
                    nfcAdapter.enableReaderMode(
                        activity,
                        callback,
                        NfcAdapter.FLAG_READER_NFC_A or
                                NfcAdapter.FLAG_READER_NFC_B or
                                NfcAdapter.FLAG_READER_NFC_F or
                                NfcAdapter.FLAG_READER_NFC_V,
                        Bundle()
                    )
                } catch (_: Exception) {}
            }

            onDispose {
                if (activity != null && nfcAdapter.isEnabled) {
                    try { nfcAdapter.disableReaderMode(activity) } catch (_: Exception) {}
                }
            }
        } else {
            status = if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            onDispose {}
        }
    }

    ModulePanel(
        label = "NFC",
        status = status,
        statusMessage = if (nfcAdapter == null) "NFC hardware not present" else "",
        modifier = modifier
    ) {
        if (nfcAdapter != null && !nfcAdapter.isEnabled) {
            Text("NFC is disabled in system settings", style = MaterialTheme.typography.bodyMedium, color = AmberAccent)
        } else {
            Text(
                text = "Tap a tag to read",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim
            )
            if (lastTagId != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tag: $lastTagId",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmberAccent
                )
                Text(
                    text = "Tags read: $tagCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
                )
            }
        }
    }
}
