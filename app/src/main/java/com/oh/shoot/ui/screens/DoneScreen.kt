package com.oh.shoot.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oh.shoot.ui.theme.*

@Composable
fun DoneScreen(
    onNewSession: () -> Unit
) {
    var startAnim by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnim = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .background(Success, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "All done!",
            style = MaterialTheme.typography.headlineMedium,
            color = AccentCream
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your photos are printing...",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(48.dp))

        // QR Placeholder
        Box(
            modifier = Modifier
                .size(160.dp)
                .border(
                    width = 2.dp,
                    color = AccentGold.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "QR CODE\nPLACEHOLDER",
                color = AccentGold.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = onNewSession,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Surface),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("NEW SESSION")
        }
    }
}
