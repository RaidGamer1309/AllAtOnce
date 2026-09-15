package com.example.allatonce.modules

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.*
import com.example.allatonce.ui.components.ModulePanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.sqrt

@Composable
fun MicrophoneModule(
    panelMode: PanelMode,
    hasPermission: Boolean,
    modifier: Modifier = Modifier
) {
    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var rmsDb by remember { mutableFloatStateOf(0f) }
    var peakDb by remember { mutableFloatStateOf(0f) }

    DisposableEffect(panelMode, hasPermission) {
        if (panelMode == PanelMode.LIVE && hasPermission) {
            var audioRecord: AudioRecord? = null
            val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)
            val job = scope.launch {
                try {
                    val sampleRate = 44100
                    val bufSize = AudioRecord.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufSize
                    )
                    if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                        status = ModuleStatus.ERROR
                        return@launch
                    }
                    audioRecord?.startRecording()
                    status = ModuleStatus.LIVE
                    val buffer = ShortArray(bufSize / 2)

                    while (isActive) {
                        val read = audioRecord?.read(buffer, 0, buffer.size) ?: break
                        if (read > 0) {
                            var sumSquares = 0.0
                            var peak = 0
                            for (i in 0 until read) {
                                val sample = buffer[i].toInt()
                                sumSquares += sample * sample
                                if (kotlin.math.abs(sample) > peak) peak = kotlin.math.abs(sample)
                            }
                            val rms = sqrt(sumSquares / read)
                            rmsDb = if (rms > 0) (20 * log10(rms / 32768.0)).toFloat() else -90f
                            peakDb = if (peak > 0) (20 * log10(peak / 32768.0)).toFloat() else -90f
                        }
                    }
                } catch (e: Exception) {
                    status = ModuleStatus.ERROR
                }
            }

            onDispose {
                job.cancel()
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (_: Exception) {}
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

    ModulePanel(label = "Microphone Level", status = status, modifier = modifier) {
        // Normalize dB to 0..1 range for meter bar (-90dB to 0dB)
        val rmsNorm = ((rmsDb + 90f) / 90f).coerceIn(0f, 1f)
        val peakNorm = ((peakDb + 90f) / 90f).coerceIn(0f, 1f)

        Text(
            text = "${String.format(java.util.Locale.US, "%.1f", rmsDb)} dBFS",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Live meter bar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        ) {
            val barHeight = size.height
            // Background track
            drawRoundRect(
                color = PanelBorder,
                size = Size(size.width, barHeight),
                cornerRadius = CornerRadius.Zero
            )
            // RMS bar
            val rmsColor = when {
                rmsNorm > 0.85f -> AlertRed
                rmsNorm > 0.6f -> AmberAccent
                else -> GreenActive
            }
            drawRoundRect(
                color = rmsColor,
                size = Size(size.width * rmsNorm, barHeight),
                cornerRadius = CornerRadius.Zero
            )
            // Peak marker
            drawLine(
                color = TextPrimary,
                start = Offset(size.width * peakNorm, 0f),
                end = Offset(size.width * peakNorm, barHeight),
                strokeWidth = 2f
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Peak: ${String.format(java.util.Locale.US, "%.1f", peakDb)} dBFS",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
    }
}
