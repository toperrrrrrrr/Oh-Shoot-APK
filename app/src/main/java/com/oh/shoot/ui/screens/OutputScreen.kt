package com.oh.shoot.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
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

@Composable
fun OutputScreen(
    photos: List<Bitmap?>,
    headerText: String,
    footerText: String,
    printerState: PrinterState,
    onBack: () -> Unit,
    onPrintConfirmed: () -> Unit
) {
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
            is PrinterState.Error -> "Printer Error"
        }
        val statusColor = when (printerState) {
            is PrinterState.Ready -> Color(0xFF4CAF50)
            is PrinterState.Connecting -> AccentGold
            is PrinterState.Disconnected -> Color.Gray
            is PrinterState.Error -> Color(0xFFF44336)
        }
        
        Surface(
            color = statusColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, statusColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = statusText,
                color = statusColor,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
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
                Text(
                    text = headerText,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                photos.filterNotNull().forEach { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
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
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("← EDIT", color = AccentGold)
            }
            
            Button(
                onClick = onPrintConfirmed,
                modifier = Modifier.weight(2f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Surface),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("PRINT & SAVE")
            }
        }
    }
}
