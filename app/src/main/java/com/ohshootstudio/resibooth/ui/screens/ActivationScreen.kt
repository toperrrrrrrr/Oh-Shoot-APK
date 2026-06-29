package com.ohshootstudio.resibooth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ohshootstudio.resibooth.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationScreen(
    onActivate: (String) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp).fillMaxWidth(0.6f)
        ) {
            Text(
                text = "License Activation",
                style = MaterialTheme.typography.headlineLarge,
                color = AccentGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please enter your early access key to continue.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = key,
                onValueChange = { 
                    key = it.uppercase()
                    error = null
                },
                label = { Text("Activation Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = AccentGold,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedLabelColor = AccentGold
                )
            )

            if (error != null) {
                Text(
                    text = error!!,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp).align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (key.isBlank()) {
                        error = "Key cannot be empty"
                    } else {
                        onActivate(key)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Surface),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("ACTIVATE DEVICE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Device ID: ${android.os.Build.SERIAL.takeIf { it != "unknown" } ?: android.os.Build.MODEL}",
                color = TextMuted.copy(alpha = 0.3f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

