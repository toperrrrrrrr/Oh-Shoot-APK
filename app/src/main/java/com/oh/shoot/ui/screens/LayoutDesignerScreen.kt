package com.oh.shoot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.oh.shoot.domain.CustomFrame
import com.oh.shoot.domain.CustomTemplate
import com.oh.shoot.ui.theme.AccentCream
import com.oh.shoot.ui.theme.AccentGold
import com.oh.shoot.ui.theme.Background
import com.oh.shoot.ui.theme.Surface
import java.util.UUID

@Composable
fun LayoutDesignerScreen(
    initialTemplate: CustomTemplate,
    onSave: (CustomTemplate) -> Unit,
    onCancel: () -> Unit
) {
    var frames by remember { mutableStateOf(initialTemplate.frames) }
    var aspectRatio by remember { mutableStateOf(initialTemplate.aspectRatio) }
    var selectedFrameId by remember { mutableStateOf<String?>(null) }
    
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Layout Designer", style = MaterialTheme.typography.headlineMedium, color = AccentCream)
            Row {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                }
                IconButton(onClick = { onSave(CustomTemplate(frames, aspectRatio)) }) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = AccentGold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (frames.size < 6) {
                        val newFrame = CustomFrame(
                            id = UUID.randomUUID().toString(),
                            x = 0.1f,
                            y = 0.1f,
                            width = 0.4f,
                            height = 0.3f
                        )
                        frames = frames + newFrame
                        selectedFrameId = newFrame.id
                    }
                },
                enabled = frames.size < 6,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Surface)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Frame")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Photo Slot (${frames.size}/6)")
            }

            if (selectedFrameId != null) {
                OutlinedButton(
                    onClick = {
                        frames = frames.filter { it.id != selectedFrameId }
                        selectedFrameId = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete Selected")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f / aspectRatio)
                    .background(Color.White)
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            selectedFrameId?.let { id ->
                                if (zoom != 1f) {
                                    frames = frames.map { f ->
                                        if (f.id == id) {
                                            val newWidth = (f.width * zoom).coerceIn(0.1f, 1f)
                                            val newHeight = (f.height * zoom).coerceIn(0.1f, 1f)
                                            f.copy(width = newWidth, height = newHeight)
                                        } else f
                                    }
                                }
                            }
                        }
                    }
            ) {
                frames.forEach { frame ->
                    val isSelected = frame.id == selectedFrameId
                    val pixelX = frame.x * canvasSize.width
                    val pixelY = frame.y * canvasSize.height
                    val pixelW = frame.width * canvasSize.width
                    val pixelH = frame.height * canvasSize.height

                    Box(
                        modifier = Modifier
                            .offset(
                                x = with(androidx.compose.ui.platform.LocalDensity.current) { pixelX.toDp() },
                                y = with(androidx.compose.ui.platform.LocalDensity.current) { pixelY.toDp() }
                            )
                            .size(
                                width = with(androidx.compose.ui.platform.LocalDensity.current) { pixelW.toDp() },
                                height = with(androidx.compose.ui.platform.LocalDensity.current) { pixelH.toDp() }
                            )
                            .background(if (isSelected) AccentGold.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.8f))
                            .border(2.dp, if (isSelected) AccentGold else Color.DarkGray)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { selectedFrameId = frame.id }
                                ) { change, dragAmount ->
                                    change.consume()
                                    val dx = dragAmount.x / canvasSize.width
                                    val dy = dragAmount.y / canvasSize.height
                                    
                                    frames = frames.map { f ->
                                        if (f.id == frame.id) {
                                            val newX = (f.x + dx).coerceIn(0f, 1f - f.width)
                                            val newY = (f.y + dy).coerceIn(0f, 1f - f.height)
                                            f.copy(x = newX, y = newY)
                                        } else f
                                    }
                                }
                            }
                    ) {
                        Text(
                            text = "${frames.indexOf(frame) + 1}",
                            modifier = Modifier.align(Alignment.Center),
                            color = if (isSelected) AccentGold else Color.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }
}
