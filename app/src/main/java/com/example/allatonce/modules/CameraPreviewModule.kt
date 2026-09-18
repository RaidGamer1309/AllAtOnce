package com.example.allatonce.modules

import android.content.Context
import android.hardware.camera2.CameraManager
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ConcurrentCamera
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import com.example.allatonce.theme.AmberAccent
import com.example.allatonce.theme.ElectricCyan
import com.example.allatonce.theme.GreenActive
import com.example.allatonce.theme.PanelSurface
import com.example.allatonce.theme.TextDim
import com.example.allatonce.ui.components.ModulePanel

enum class CameraMode {
    DUAL, // All cameras / dual concurrent
    BACK, // Back camera
    FRONT // Front camera
}

@Composable
fun CameraPreviewModule(
    panelMode: PanelMode,
    hasPermission: Boolean,
    modifier: Modifier = Modifier
) {
    var status by remember { mutableStateOf(ModuleStatus.DORMANT) }
    var selectedMode by remember { mutableStateOf(CameraMode.DUAL) }

    val isLive = panelMode == PanelMode.LIVE && hasPermission

    LaunchedEffect(panelMode, hasPermission) {
        status = when {
            panelMode != PanelMode.LIVE -> if (panelMode == PanelMode.STOPPED) ModuleStatus.STOPPED else ModuleStatus.DORMANT
            !hasPermission -> ModuleStatus.PERMISSION_DENIED
            else -> ModuleStatus.LIVE
        }
    }

    ModulePanel(
        label = "Optical Sensor Array (Cameras)",
        status = status,
        modifier = modifier
    ) {
        if (isLive) {
            CameraMultiStreamContent(
                selectedMode = selectedMode,
                onModeChange = { selectedMode = it }
            )
        } else if (!hasPermission) {
            Text(
                text = "CAMERA PERMISSION REQUIRED TO INITIALIZE OPTICAL SENSORS",
                style = MaterialTheme.typography.labelSmall,
                color = AmberAccent
            )
        }
    }
}

@Composable
private fun CameraMultiStreamContent(
    selectedMode: CameraMode,
    onModeChange: (CameraMode) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isConcurrentSupported by remember { mutableStateOf<Boolean?>(null) }
    var cameraHardwareCount by remember { mutableIntStateOf(0) }

    // Discover camera hardware capabilities
    LaunchedEffect(Unit) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            cameraHardwareCount = cameraManager?.cameraIdList?.size ?: 0

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val provider = cameraProviderFuture.get()
                    val concurrent = provider.availableConcurrentCameraInfos
                    isConcurrentSupported = concurrent.isNotEmpty()
                } catch (_: Exception) {
                    isConcurrentSupported = false
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (_: Exception) {
            isConcurrentSupported = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Mode Selector Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraTabButton(
                text = "ALL / DUAL",
                active = selectedMode == CameraMode.DUAL,
                modifier = Modifier.weight(1f)
            ) {
                onModeChange(CameraMode.DUAL)
            }

            CameraTabButton(
                text = "BACK (REAR)",
                active = selectedMode == CameraMode.BACK,
                modifier = Modifier.weight(1f)
            ) {
                onModeChange(CameraMode.BACK)
            }

            CameraTabButton(
                text = "FRONT (SELFIE)",
                active = selectedMode == CameraMode.FRONT,
                modifier = Modifier.weight(1f)
            ) {
                onModeChange(CameraMode.FRONT)
            }
        }

        // Status / Hardware capability telemetry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "LENSES DETECTED: $cameraHardwareCount",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan
            )
            Text(
                text = when (isConcurrentSupported) {
                    true -> "ISP: DUAL-STREAM READY"
                    false -> "ISP: SINGLE-STREAM ONLY"
                    null -> "ISP: PROBING..."
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isConcurrentSupported == true) GreenActive else AmberAccent
            )
        }

        // Camera Video Feeds
        when {
            selectedMode == CameraMode.DUAL && isConcurrentSupported == true -> {
                // Dual concurrent preview streams simultaneously
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ConcurrentDualCameraPreview(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            selectedMode == CameraMode.DUAL && isConcurrentSupported == false -> {
                // Fallback: Hardware ISP cannot stream dual cameras at once
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "● HARDWARE LIMIT: Device ISP does not support concurrent dual stream. Showing Back Camera (tap FRONT to switch).",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberAccent
                    )
                    SingleCameraPreview(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        selector = CameraSelector.DEFAULT_BACK_CAMERA,
                        label = "BACK CAMERA (PRIMARY)",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }
            selectedMode == CameraMode.BACK -> {
                SingleCameraPreview(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    selector = CameraSelector.DEFAULT_BACK_CAMERA,
                    label = "BACK CAMERA (MAIN)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
            selectedMode == CameraMode.FRONT -> {
                SingleCameraPreview(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    selector = CameraSelector.DEFAULT_FRONT_CAMERA,
                    label = "FRONT CAMERA (SELFIE)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }
    }
}

@Composable
private fun SingleCameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    selector: CameraSelector,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.clip(RoundedCornerShape(4.dp)).background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
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
                        cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Overlay HUD
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "● $label",
                style = MaterialTheme.typography.labelSmall,
                color = GreenActive
            )
        }
    }
}

@Composable
private fun ConcurrentDualCameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier = Modifier
) {
    var bindError by remember { mutableStateOf<String?>(null) }

    if (bindError != null) {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(4.dp))
                .background(PanelSurface)
                .padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CONCURRENT BIND FAILED: $bindError",
                style = MaterialTheme.typography.labelSmall,
                color = AmberAccent
            )
            Text(
                text = "Device ISP does not allow concurrent streaming of these lenses.",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim
            )
        }
        return
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val container = android.widget.LinearLayout(ctx).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    weightSum = 2f
                }

                val backView = PreviewView(ctx).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                        marginEnd = 4
                    }
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                val frontView = PreviewView(ctx).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                        marginStart = 4
                    }
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                container.addView(backView)
                container.addView(frontView)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val backPreview = Preview.Builder().build().also {
                            it.surfaceProvider = backView.surfaceProvider
                        }
                        val frontPreview = Preview.Builder().build().also {
                            it.surfaceProvider = frontView.surfaceProvider
                        }

                        val backConfig = ConcurrentCamera.SingleCameraConfig(
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            UseCaseGroup.Builder().addUseCase(backPreview).build(),
                            lifecycleOwner
                        )
                        val frontConfig = ConcurrentCamera.SingleCameraConfig(
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            UseCaseGroup.Builder().addUseCase(frontPreview).build(),
                            lifecycleOwner
                        )

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(listOf(backConfig, frontConfig))
                    } catch (e: Exception) {
                        bindError = e.message
                    }
                }, ContextCompat.getMainExecutor(ctx))

                container
            }
        )

        // Labels overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "● BACK (REAR)",
                    style = MaterialTheme.typography.labelSmall,
                    color = GreenActive
                )
            }
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "● FRONT (SELFIE)",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan
                )
            }
        }
    }
}

@Composable
private fun CameraTabButton(
    text: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (active) GreenActive.copy(alpha = 0.2f) else PanelSurface)
            .border(
                width = 1.dp,
                color = if (active) GreenActive else TextDim.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) GreenActive else Color.White
        )
    }
}
