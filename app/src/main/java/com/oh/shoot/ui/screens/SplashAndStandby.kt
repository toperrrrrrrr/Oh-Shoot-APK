package com.oh.shoot.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oh.shoot.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "OH Shoot!",
                style = MaterialTheme.typography.displayLarge,
                color = AccentCream
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Print memories — instantly",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted
            )
        }
    }
}

@Composable
fun StandbyScreen(onTap: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "ringPulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .clickable { onTap() },
        contentAlignment = Alignment.Center
    ) {
        // Pulsing Ring
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(ringScale)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = AccentGold.copy(alpha = 0.5f),
                    radius = size.minDimension / 2,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                )
            }
        }

        Text(
            text = "TAP TO START",
            style = MaterialTheme.typography.headlineMedium,
            color = AccentCream,
            letterSpacing = 2.sp
        )
    }
}
