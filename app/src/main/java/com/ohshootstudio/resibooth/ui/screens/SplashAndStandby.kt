package com.ohshootstudio.resibooth.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.core.content.ContextCompat
import com.ohshootstudio.resibooth.BluetoothPrinter
import com.ohshootstudio.resibooth.ui.theme.*
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class CheckState {
    object Idle : CheckState()
    object Checking : CheckState()
    data class Success(val message: String) : CheckState()
    data class Error(val message: String) : CheckState()
}

@Composable
fun SplashScreen(appSettings: com.ohshootstudio.resibooth.domain.AppSettings, onTimeout: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cameraPermissionState by remember { mutableStateOf<CheckState>(CheckState.Idle) }
    var bluetoothPermissionState by remember { mutableStateOf<CheckState>(CheckState.Idle) }
    var printerPairedState by remember { mutableStateOf<CheckState>(CheckState.Idle) }
    var printerConnectionState by remember { mutableStateOf<CheckState>(CheckState.Idle) }

    var triggerDiagnostics by remember { mutableStateOf(0) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        cameraPermissionState = if (cameraGranted) {
            CheckState.Success("Camera access granted")
        } else {
            CheckState.Error("Camera access denied")
        }

        val btGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
        } else {
            true
        }
        bluetoothPermissionState = if (btGranted) {
            CheckState.Success("Bluetooth ready")
        } else {
            CheckState.Error("Bluetooth permission denied")
        }
        // Run diagnostics again to complete the checks
        triggerDiagnostics++
    }

    LaunchedEffect(triggerDiagnostics) {
        if (triggerDiagnostics == 0) return@LaunchedEffect
        
        // 1. Check Camera
        cameraPermissionState = CheckState.Checking
        delay(400)
        val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        
        // 2. Check Bluetooth Connect (SDK 31+)
        bluetoothPermissionState = CheckState.Checking
        val hasBt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (hasCamera && hasBt) {
            cameraPermissionState = CheckState.Success("Camera access granted")
            bluetoothPermissionState = CheckState.Success("Bluetooth ready")
        } else {
            val list = mutableListOf<String>()
            if (!hasCamera) list.add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!hasBt) {
                    list.add(Manifest.permission.BLUETOOTH_CONNECT)
                    list.add(Manifest.permission.BLUETOOTH_SCAN)
                }
            }
            permissionLauncher.launch(list.toTypedArray())
            return@LaunchedEffect
        }

        // 3. Check Printer Connection Based on Settings
        printerPairedState = CheckState.Checking
        printerConnectionState = CheckState.Checking
        delay(400)
        when (appSettings.printerConnectionType) {
            "Network" -> {
                printerPairedState = CheckState.Success("Network Printer Configured")
                val isConnected = com.ohshootstudio.resibooth.printer.NetworkPrinterManager.testConnection(
                    appSettings.networkPrinterIp,
                    appSettings.networkPrinterPort
                )
                if (isConnected) {
                    printerConnectionState = CheckState.Success("Printer online at ${appSettings.networkPrinterIp}")
                } else {
                    printerConnectionState = CheckState.Error("Printer unreachable")
                }
            }
            "Bluetooth" -> {
                val device = BluetoothPrinter.findPairedPrinter(context)
                if (device != null) {
                    printerPairedState = CheckState.Success("Paired: ${device.name ?: "Unknown"}")
                    val isConnected = BluetoothPrinter.testConnection(context, device)
                    if (isConnected) {
                        printerConnectionState = CheckState.Success("Printer online & ready")
                    } else {
                        printerConnectionState = CheckState.Error("Printer offline or unreachable")
                    }
                } else {
                    printerPairedState = CheckState.Error("No paired printer found")
                    printerConnectionState = CheckState.Idle
                }
            }
            else -> {
                printerPairedState = CheckState.Success("USB Selected")
                printerConnectionState = CheckState.Success("USB relies on auto-discovery")
            }
        }
    }

    LaunchedEffect(Unit) {
        triggerDiagnostics++
    }

    // Diagnostics completed success check (non-fatal printer error is ok)
    val isCameraOk = cameraPermissionState is CheckState.Success
    val isBtOk = bluetoothPermissionState is CheckState.Success
    val allDiagnosticsFinished = (cameraPermissionState is CheckState.Success || cameraPermissionState is CheckState.Error) &&
            (bluetoothPermissionState is CheckState.Success || bluetoothPermissionState is CheckState.Error) &&
            (printerPairedState is CheckState.Success || printerPairedState is CheckState.Error) &&
            (printerConnectionState is CheckState.Success || printerConnectionState is CheckState.Error || printerConnectionState is CheckState.Idle)

    val autoProceed = isCameraOk && isBtOk && 
            (printerConnectionState is CheckState.Success || printerPairedState is CheckState.Error || printerConnectionState is CheckState.Error)

    LaunchedEffect(allDiagnosticsFinished, autoProceed) {
        if (allDiagnosticsFinished && autoProceed) {
            delay(1500)
            onTimeout()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(
                text = "OH Shoot!",
                style = MaterialTheme.typography.displayMedium,
                color = AccentCream,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Print memories — instantly",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            // Diagnostic card
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "System Diagnostics",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentGold,
                        fontWeight = FontWeight.Bold
                    )

                    DiagnosticItem("Camera Permission", cameraPermissionState)
                    DiagnosticItem("Bluetooth Services", bluetoothPermissionState)
                    DiagnosticItem("Paired Printer Status", printerPairedState)
                    DiagnosticItem("Printer Online Check", printerConnectionState)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons / Progress
            if (allDiagnosticsFinished) {
                if (isCameraOk) {
                    Button(
                        onClick = onTimeout,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Surface),
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("CONTINUE TO BOOTH", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { triggerDiagnostics++ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336), contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("RETRY DIAGNOSTICS", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                CircularProgressIndicator(
                    color = AccentGold,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun DiagnosticItem(label: String, state: CheckState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
        
        when (state) {
            is CheckState.Idle -> {
                Icon(
                    imageVector = Icons.Default.HourglassEmpty,
                    contentDescription = "Pending",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            is CheckState.Checking -> {
                CircularProgressIndicator(
                    color = AccentGold,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            }
            is CheckState.Success -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.message, 
                        color = Color(0xFF4CAF50), 
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            is CheckState.Error -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.message, 
                        color = Color(0xFFF44336), 
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StandbyScreen(
    customLogoUri: String? = null,
    standbyImageUri: String? = null,
    isEditing: Boolean = false,
    offsetX: Float = 0f,
    offsetY: Float = 0f,
    scale: Float = 1f,
    onUpdateLayout: (Float, Float, Float) -> Unit = { _, _, _ -> },
    onExitEditMode: () -> Unit = {},
    onTap: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ringPulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    var currentOffsetX by remember(offsetX) { mutableStateOf(offsetX) }
    var currentOffsetY by remember(offsetY) { mutableStateOf(offsetY) }
    var currentScale by remember(scale) { mutableStateOf(scale) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .clickable(
                enabled = !isEditing,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onTap() },
        contentAlignment = Alignment.Center
    ) {
        if (!standbyImageUri.isNullOrEmpty()) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(LocalContext.current)
                    .data(android.net.Uri.parse(standbyImageUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "Standby Background",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!customLogoUri.isNullOrEmpty()) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                        .data(android.net.Uri.parse(customLogoUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Custom Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .heightIn(max = 200.dp)
                        .padding(bottom = 32.dp)
                )
            }

            // Pulsing Circle Button
            Box(
                modifier = Modifier
                    .offset { IntOffset(currentOffsetX.roundToInt(), currentOffsetY.roundToInt()) }
                    .size(240.dp)
                    .scale(ringScale * currentScale)
                    .then(
                        if (isEditing) {
                            Modifier.pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    currentScale = (currentScale * zoom).coerceIn(0.3f, 3f)
                                    currentOffsetX += pan.x
                                    currentOffsetY += pan.y
                                }
                            }
                        } else Modifier
                    )
                    .background(if (isEditing) AccentGold.copy(alpha = 0.6f) else AccentGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TAP TO START",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Surface,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        if (isEditing) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(vertical = 16.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Edit Mode: Drag to move, Pinch to scale", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { 
                        currentOffsetX = 0f
                        currentOffsetY = 0f
                        currentScale = 1f
                    }) {
                        Text("Reset", color = Color.White)
                    }
                    Button(
                        onClick = { 
                            onUpdateLayout(currentOffsetX, currentOffsetY, currentScale)
                            onExitEditMode() 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Surface)
                    ) {
                        Text("Save Layout")
                    }
                }
            }
        }
    }
}

