package com.oh.shoot.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.oh.shoot.domain.AppSettings
import com.oh.shoot.printer.PrinterState
import com.oh.shoot.ui.theme.AccentGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel(
    settings: AppSettings,
    printerState: PrinterState,
    onSettingsChanged: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val context = LocalContext.current

        var hasBtPermission by remember {
            mutableStateOf(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }
            )
        }

        val btPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasBtPermission = granted
            if (granted) {
                onSettingsChanged(settings.copy(useBluetoothPrinter = true))
            }
        }

        val logoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                onSettingsChanged(settings.copy(customLogoUri = uri.toString()))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AccentGold
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Printer Status Indicator
            val statusText = when (printerState) {
                is PrinterState.Ready -> "Online"
                is PrinterState.Connecting -> "Connecting..."
                is PrinterState.Disconnected -> "Disconnected"
                is PrinterState.Error -> "Error: ${printerState.message}"
            }
            val statusColor = when (printerState) {
                is PrinterState.Ready -> Color(0xFF4CAF50)
                is PrinterState.Connecting -> AccentGold
                is PrinterState.Disconnected -> Color.Gray
                is PrinterState.Error -> Color(0xFFF44336)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Printer Status: ", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Camera Facing
            SettingRow(label = "Front Camera") {
                Switch(
                    checked = settings.cameraFacingFront,
                    onCheckedChange = { onSettingsChanged(settings.copy(cameraFacingFront = it)) }
                )
            }

            // Mirror
            SettingRow(label = "Mirror Preview") {
                Switch(
                    checked = settings.mirrorPreview,
                    onCheckedChange = { onSettingsChanged(settings.copy(mirrorPreview = it)) }
                )
            }

            // Bluetooth Printer Row
            SettingRow(label = "Use Bluetooth Printer") {
                Switch(
                    checked = settings.useBluetoothPrinter,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasBtPermission) {
                                btPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                            } else {
                                onSettingsChanged(settings.copy(useBluetoothPrinter = true))
                            }
                        } else {
                            onSettingsChanged(settings.copy(useBluetoothPrinter = false))
                        }
                    }
                )
            }

            SettingRow(label = "Capture Sounds") {
                Switch(
                    checked = settings.soundsEnabled,
                    onCheckedChange = { onSettingsChanged(settings.copy(soundsEnabled = it)) }
                )
            }

            // Paper Width
            SettingRow(label = "Paper Width (80mm)") {
                Switch(
                    checked = settings.paperWidth80mm,
                    onCheckedChange = { onSettingsChanged(settings.copy(paperWidth80mm = it)) }
                )
            }

            // Auto Cut
            SettingRow(label = "Auto Cut Paper") {
                Switch(
                    checked = settings.autoCut,
                    onCheckedChange = { onSettingsChanged(settings.copy(autoCut = it)) }
                )
            }

            // Default Copies
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Default Copies", style = MaterialTheme.typography.bodyLarge)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { 
                        onSettingsChanged(settings.copy(defaultCopies = (settings.defaultCopies - 1).coerceAtLeast(1))) 
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = settings.defaultCopies.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = { 
                        onSettingsChanged(settings.copy(defaultCopies = (settings.defaultCopies + 1).coerceAtMost(5))) 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }

            // Business Mode
            var expandedMode by remember { mutableStateOf(false) }
            val modes = listOf("Rental", "Vendo", "Demo")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Business Mode", style = MaterialTheme.typography.bodyLarge)
                Box {
                    TextButton(onClick = { expandedMode = true }) {
                        Text(text = settings.businessMode, style = MaterialTheme.typography.bodyLarge, color = AccentGold)
                    }
                    DropdownMenu(
                        expanded = expandedMode,
                        onDismissRequest = { expandedMode = false }
                    ) {
                        modes.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode) },
                                onClick = {
                                    onSettingsChanged(settings.copy(businessMode = mode))
                                    expandedMode = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Custom Print Logo", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { logoPickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (settings.customLogoUri != null) "Change Logo" else "Upload Logo")
                }
                if (settings.customLogoUri != null) {
                    OutlinedButton(
                        onClick = { onSettingsChanged(settings.copy(customLogoUri = null)) }
                    ) {
                        Text("Clear")
                    }
                }
            }

            if (settings.customLogoUri != null) {
                Text(
                    text = "Logo set — used on print header instead of text",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentGold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contrast
            Text("Contrast Boost: ${String.format("%.1f", settings.contrastBoost)}", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = settings.contrastBoost,
                onValueChange = { onSettingsChanged(settings.copy(contrastBoost = it)) },
                valueRange = 0.5f..2.0f,
                steps = 15
            )

            // Header Text (fallback when no logo)
            OutlinedTextField(
                value = settings.headerText,
                onValueChange = { onSettingsChanged(settings.copy(headerText = it)) },
                label = { Text("Header Text (fallback)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = settings.customLogoUri == null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Text
            OutlinedTextField(
                value = settings.footerText,
                onValueChange = { onSettingsChanged(settings.copy(footerText = it)) },
                label = { Text("Footer Text") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        content()
    }
}
