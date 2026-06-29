package com.ohshootstudio.resibooth.ui.screens

import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwitchCamera
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ohshootstudio.resibooth.audio.CaptureSoundPlayer
import com.ohshootstudio.resibooth.camera.CameraManager
import com.ohshootstudio.resibooth.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

@Composable
fun CaptureScreen(
    maxPhotos: Int,
    currentPhotoIndex: Int,
    cameraLensFacing: Int,
    mirrorPreview: Boolean,
    squareMode: Boolean,
    soundsEnabled: Boolean,
    ringLightEnabled: Boolean,
    onCameraFacingChanged: (Int) -> Unit,
    onPhotoCaptured: (Bitmap) -> Unit,
    onAllPhotosCaptured: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    val cameraManager = remember { CameraManager(context) }
    val previewView = remember { 
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val soundPlayer = remember { CaptureSoundPlayer(context) }

    // Block standard back navigation
    BackHandler {
        // Do nothing, force use of Cancel button
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPlayer.release()
            cameraManager.unbindAll()
            cameraManager.shutdown()
            // Reset brightness when leaving capture screen
            activity?.window?.attributes = activity?.window?.attributes?.apply {
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
        }
    }

    var hasPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }
    
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    var countdownValue by remember { mutableIntStateOf(0) }
    var isCapturing by remember { mutableStateOf(false) }
    var triggerFlash by remember { mutableStateOf(false) }
    var uvcTextureView by remember { mutableStateOf<android.view.TextureView?>(null) }

    val currentFlashAlpha by animateFloatAsState(
        targetValue = if (triggerFlash) 1f else 0f,
        animationSpec = tween(150),
        label = "flash",
        finishedListener = { if (it == 1f) triggerFlash = false }
    )

    if (hasPermission) {
        if (cameraLensFacing == 2) {
            // Native UVC driver doesn't use CameraManager.startCamera
        } else {
            LaunchedEffect(previewView, cameraLensFacing) {
                cameraManager.startCamera(
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    lensFacing = cameraLensFacing,
                    onCameraFallback = {
                        scope.launch {
                            onCameraFacingChanged(0) // Fall back to front
                        }
                    }
                )
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .then(
            if (ringLightEnabled) {
                Modifier.border(20.dp, Color.White)
            } else {
                Modifier
            }
        )
    ) {
        if (hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (cameraLensFacing == 2) {
                    com.ohshootstudio.resibooth.camera.UvcCameraView(
                        modifier = Modifier
                            .then(
                                if (squareMode) {
                                    Modifier.aspectRatio(1f)
                                } else {
                                    Modifier.fillMaxSize()
                                }
                            )
                            .then(
                                if (mirrorPreview) {
                                    Modifier.graphicsLayer(scaleX = -1f)
                                } else {
                                    Modifier
                                }
                            ),
                        onTextureViewCreated = { uvcTextureView = it },
                        onFallback = {
                            scope.launch {
                                onCameraFacingChanged(0)
                            }
                        }
                    )
                } else {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier
                            .then(
                                if (squareMode) {
                                    Modifier.aspectRatio(1f)
                                } else {
                                    Modifier.fillMaxSize()
                                }
                            )
                            .then(
                                if (mirrorPreview) {
                                    Modifier.graphicsLayer(scaleX = -1f)
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
            }
        }

        // Overlays
        CaptureOverlays(
            currentCount = currentPhotoIndex + 1,
            maxCount = maxPhotos
        )

        // Shutter Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp) // Adjusted for ring light
        ) {
            ShutterButton(
                enabled = !isCapturing,
                onClick = {
                    scope.launch {
                        isCapturing = true
                        // Countdown 3, 2, 1
                        for (i in 3 downTo 1) {
                            if (!isActive) return@launch
                            countdownValue = i
                            if (soundsEnabled) {
                                soundPlayer.playTick()
                            }
                            delay(1000)
                        }
                        if (!isActive) return@launch
                        countdownValue = 0
                        
                        try {
                            // Flash Effect & Max Brightness
                            activity?.window?.attributes = activity?.window?.attributes?.apply {
                                screenBrightness = 1f
                            }
                            triggerFlash = true
                            
                            if (soundsEnabled) {
                                soundPlayer.playShutter()
                            }
                            
                            if (cameraLensFacing == 2) {
                                var finalBitmap = uvcTextureView?.bitmap
                                if (finalBitmap != null) {
                                    if (squareMode) {
                                        val width = finalBitmap.width
                                        val height = finalBitmap.height
                                        val minDim = minOf(width, height)
                                        val xOffset = (width - minDim) / 2
                                        val yOffset = (height - minDim) / 2
                                        val cropped = Bitmap.createBitmap(finalBitmap, xOffset, yOffset, minDim, minDim)
                                        if (cropped != finalBitmap) {
                                            finalBitmap.recycle()
                                        }
                                        finalBitmap = cropped
                                    }
                                    onPhotoCaptured(finalBitmap)
                                }
                                isCapturing = false
                                if (currentPhotoIndex + 1 >= maxPhotos) {
                                    onAllPhotosCaptured()
                                }
                            } else {
                                cameraManager.takePhoto(squareMode = squareMode) { bitmap ->
                                    onPhotoCaptured(bitmap)
                                    isCapturing = false
                                    if (currentPhotoIndex + 1 >= maxPhotos) {
                                        onAllPhotosCaptured()
                                    }
                                }
                            }
                        } finally {
                            // Restore brightness
                            activity?.window?.attributes = activity?.window?.attributes?.apply {
                                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                            }
                        }
                    }
                }
            )
        }

        // Camera Switch Button
        if (!isCapturing) {
            IconButton(
                onClick = { 
                    val nextFacing = (cameraLensFacing + 1) % 3
                    onCameraFacingChanged(nextFacing)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 60.dp, end = 60.dp)
                    .size(64.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.SwitchCamera,
                    contentDescription = "Switch Camera",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Countdown Overlay
        if (countdownValue > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = countdownValue.toString(),
                    color = Color.White,
                    fontSize = 140.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }

        // Flash Effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(currentFlashAlpha)
                .background(Color.White)
        )
        
        // Cancel Button (explicit reset)
        Text(
            text = "CANCEL",
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(44.dp) // Adjusted for ring light
                .clickable { onCancel() },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ShutterButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(if (enabled) AccentGold else Color.Gray)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .border(2.dp, Surface, CircleShape)
        )
    }
}

@Composable
fun CaptureOverlays(currentCount: Int, maxCount: Int) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Corner Brackets
        Canvas(modifier = Modifier.fillMaxSize().padding(48.dp)) {
            val strokeWidth = 4.dp.toPx()
            val lineLength = 40.dp.toPx()
            
            // Top Left
            drawLine(AccentCream, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(lineLength, 0f), strokeWidth = strokeWidth)
            drawLine(AccentCream, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(0f, lineLength), strokeWidth = strokeWidth)
            
            // Top Right
            drawLine(AccentCream, start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset(size.width - lineLength, 0f), strokeWidth = strokeWidth)
            drawLine(AccentCream, start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset(size.width, lineLength), strokeWidth = strokeWidth)
            
            // Bottom Left
            drawLine(AccentCream, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(lineLength, size.height), strokeWidth = strokeWidth)
            drawLine(AccentCream, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(0f, size.height - lineLength), strokeWidth = strokeWidth)
            
            // Bottom Right
            drawLine(AccentCream, start = androidx.compose.ui.geometry.Offset(size.width, size.height), end = androidx.compose.ui.geometry.Offset(size.width - lineLength, size.height), strokeWidth = strokeWidth)
            drawLine(AccentCream, start = androidx.compose.ui.geometry.Offset(size.width, size.height), end = androidx.compose.ui.geometry.Offset(size.width, size.height - lineLength), strokeWidth = strokeWidth)
        }
        
        // Counter Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(32.dp)
                .background(Surface.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "$currentCount / $maxCount",
                color = AccentCream,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

