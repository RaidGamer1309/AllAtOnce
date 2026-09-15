package com.example.allatonce.modules

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.ui.components.ModulePanel

@Composable
fun CameraPreviewModule(
    panelMode: PanelMode,
    hasPermission: Boolean,
    modifier: Modifier = Modifier
) {
    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }

    val isLive = panelMode == PanelMode.LIVE && hasPermission

    LaunchedEffect(panelMode, hasPermission) {
        status = when {
            panelMode != PanelMode.LIVE -> if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            !hasPermission -> ModuleStatus.PERMISSION_DENIED
            else -> ModuleStatus.LIVE
        }
    }

    ModulePanel(label = "Front Camera", status = status, modifier = modifier) {
        if (isLive) {
            CameraPreviewContent(modifier = Modifier.fillMaxWidth().height(160.dp))
        }
    }
}

@Composable
private fun CameraPreviewContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        onDispose {
            // Camera is released when ProcessCameraProvider unbinds
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        cameraProviderFuture.get().unbindAll()
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(context))
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview
                    )
                } catch (_: Exception) {}
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}
