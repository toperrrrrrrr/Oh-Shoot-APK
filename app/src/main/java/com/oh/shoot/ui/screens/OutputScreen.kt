package com.oh.shoot.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oh.shoot.printer.PrinterState
import com.oh.shoot.ui.theme.*
import com.oh.shoot.util.ImageUtils
import com.oh.shoot.domain.AppSettings
import com.oh.shoot.BitmapProcessor
import com.oh.shoot.ui.screens.LayoutType

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh

@Composable
fun OutputScreen(
    photos: List<Bitmap?>,
    appSettings: AppSettings,
    layoutType: LayoutType,
    printerState: PrinterState,
    onRetryConnection: () -> Unit,
    onCancel: () -> Unit,
    onPrintConfirmed: () -> Unit
) {
    BackHandler {
        // Block back press
    }

    val context = LocalContext.current
    var headerLogo by remember(appSettings.customLogoUri) { mutableStateOf<Bitmap?>(null) }
    var previewLayoutBitmap by remember(photos, appSettings, layoutType) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(appSettings.customLogoUri) {
        headerLogo = appSettings.customLogoUri?.let { uri ->
            ImageUtils.decodeBitmapFromUri(context, uri, 800)
        }
    }

    LaunchedEffect(photos, appSettings, layoutType) {
        val printable = photos.filterNotNull()
        if (printable.isNotEmpty()) {
            previewLayoutBitmap = BitmapProcessor.combineBitmapsToGrid(
                bitmaps = printable,
                targetWidth = 576,
                type = layoutType,
                cornerRadius = appSettings.printedCornerRadius,
                squareMode = appSettings.squareMode,
                borderDesignId = appSettings.borderDesignId
            )
        }
    }

    val headerText = appSettings.headerText
    val footerText = appSettings.footerText

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ready to print?",
            style = MaterialTheme.typography.headlineMedium,
            color = AccentCream
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Printer Connection Status Chip
        val statusText = when (printerState) {
            is PrinterState.Ready -> "Printer Online"
            is PrinterState.Connecting -> "Connecting..."
            is PrinterState.Disconnected -> "Printer Offline"
            is PrinterState.Error -> "Printer Error: ${printerState.message}"
        }
        val statusColor = when (printerState) {
            is PrinterState.Ready -> Color(0xFF4CAF50)
            is PrinterState.Connecting -> AccentGold
            is PrinterState.Disconnected -> Color.Gray
            is PrinterState.Error -> Color(0xFFF44336)
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, statusColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            
            if (printerState !is PrinterState.Ready && printerState !is PrinterState.Connecting) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onRetryConnection,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry Connection",
                        tint = AccentGold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Print Preview Card
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (headerLogo != null) {
                    Image(
                        bitmap = headerLogo!!.asImageBitmap(),
                        contentDescription = "Print header logo",
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = headerText,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (previewLayoutBitmap != null) {
                    Image(
                        bitmap = previewLayoutBitmap!!.asImageBitmap(),
                        contentDescription = "Layout Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentScale = ContentScale.FillWidth
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = footerText,
                    color = Color.Black,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("CANCEL", color = AccentGold, fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onPrintConfirmed,
                modifier = Modifier.weight(2f).height(56.dp),
                enabled = printerState is PrinterState.Ready,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGold,
                    contentColor = Surface,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(if (printerState is PrinterState.Ready) "PRINT & SAVE" else "PRINTER NOT READY")
            }
        }
    }
}
