package com.example.allatonce.modules

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.*
import com.example.allatonce.ui.components.ModulePanel

@Composable
fun SpeakerModule(panelMode: PanelMode, modifier: Modifier = Modifier) {
    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var playingTone by remember { mutableStateOf<String?>(null) }
    val toneGen = remember { mutableStateOf<ToneGenerator?>(null) }

    LaunchedEffect(panelMode) {
        status = when (panelMode) {
            PanelMode.LIVE -> ModuleStatus.LIVE
            PanelMode.STOPPED -> {
                toneGen.value?.release()
                toneGen.value = null
                playingTone = null
                ModuleStatus.STOPPED
            }
            PanelMode.DORMANT -> ModuleStatus.DORMANT
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            toneGen.value?.release()
            toneGen.value = null
        }
    }

    ModulePanel(label = "Speaker Test", status = status, modifier = modifier) {
        Text(
            text = playingTone ?: "Ready",
            style = MaterialTheme.typography.bodyLarge,
            color = if (playingTone != null) AmberAccent else TextDim
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    try {
                        toneGen.value?.release()
                        val gen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                        toneGen.value = gen
                        gen.startTone(ToneGenerator.TONE_DTMF_0, 500)
                        playingTone = "440 Hz"
                    } catch (_: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberAccent,
                    contentColor = PanelBackground
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f)
            ) {
                Text("440 Hz", style = MaterialTheme.typography.labelLarge, color = PanelBackground)
            }
            Button(
                onClick = {
                    try {
                        toneGen.value?.release()
                        val gen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                        toneGen.value = gen
                        gen.startTone(ToneGenerator.TONE_DTMF_9, 500)
                        playingTone = "1000 Hz"
                    } catch (_: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryBlue,
                    contentColor = PanelBackground
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f)
            ) {
                Text("1000 Hz", style = MaterialTheme.typography.labelLarge, color = PanelBackground)
            }
        }
    }
}
