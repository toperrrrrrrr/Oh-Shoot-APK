package com.ohshootstudio.resibooth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ohshootstudio.resibooth.ui.theme.*

enum class LayoutType {
    SINGLE,
    STRIP_2,
    STRIP_3,
    STRIP_4,
    GRID_2X2,
    GRID_2X3,
    GRID_2X4,
    CUSTOM
}

data class LayoutOption(
    val type: LayoutType,
    val name: String,
    val photoCount: Int
)

private fun getLayoutOptions(customPhotoCount: Int) = listOf(
    LayoutOption(LayoutType.SINGLE, "Single Photo", 1),
    LayoutOption(LayoutType.STRIP_2, "2-Photo Strip", 2),
    LayoutOption(LayoutType.STRIP_3, "3-Photo Strip", 3),
    LayoutOption(LayoutType.STRIP_4, "4-Photo Strip", 4),
    LayoutOption(LayoutType.GRID_2X2, "2x2 Grid (1:1)", 4),
    LayoutOption(LayoutType.GRID_2X3, "2x3 Grid", 6),
    LayoutOption(LayoutType.GRID_2X4, "2x4 Grid", 8),
    LayoutOption(LayoutType.CUSTOM, "Custom Hybrid", customPhotoCount.coerceAtLeast(1))
)

@Composable
fun LayoutSelectScreen(
    customPhotoCount: Int,
    customTemplate: com.ohshootstudio.resibooth.domain.CustomTemplate?,
    onLayoutSelected: (LayoutOption) -> Unit,
    onCancel: () -> Unit
) {
    val layoutOptions = remember(customPhotoCount) { getLayoutOptions(customPhotoCount) }
    var selectedType by remember { mutableStateOf(layoutOptions.first().type) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Choose your layout",
                style = MaterialTheme.typography.headlineMedium,
                color = AccentCream
            )
            
            TextButton(onClick = onCancel) {
                Text("CANCEL", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(layoutOptions) { option ->
                LayoutCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = option.name,
                    isSelected = selectedType == option.type,
                    onClick = { selectedType = option.type }
                ) {
                    LayoutDiagram(option.type, customTemplate)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val selectedOption = layoutOptions.first { it.type == selectedType }

        Button(
            onClick = { onLayoutSelected(selectedOption) },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGold,
                contentColor = Surface
            ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text("CONTINUE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LayoutCard(
    modifier: Modifier = Modifier,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(
                border = BorderStroke(
                    width = 4.dp,
                    color = if (isSelected) AccentGold else Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(0.8f)
                .fillMaxWidth()
        ) {
            content()
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LayoutDiagram(type: LayoutType, customTemplate: com.ohshootstudio.resibooth.domain.CustomTemplate? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        when (type) {
            LayoutType.SINGLE -> {
                Box(Modifier.fillMaxSize().background(Surface2).border(1.dp, TextMuted))
            }
            LayoutType.STRIP_2 -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.weight(1f).fillMaxWidth().background(Surface2).border(1.dp, TextMuted))
                    Box(Modifier.weight(1f).fillMaxWidth().background(Surface2).border(1.dp, TextMuted))
                }
            }
            LayoutType.STRIP_3 -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.weight(1f).fillMaxWidth().background(Surface2).border(1.dp, TextMuted))
                    Box(Modifier.weight(1f).fillMaxWidth().background(Surface2).border(1.dp, TextMuted))
                    Box(Modifier.weight(1f).fillMaxWidth().background(Surface2).border(1.dp, TextMuted))
                }
            }
            LayoutType.STRIP_4 -> {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(Modifier.weight(1f).fillMaxWidth().background(Surface2).border(1.dp, TextMuted))
                    Box(Modifier.weight(1f).fillMaxWidth().background(Surface2).border(1.dp, TextMuted))
                    Box(Modifier.weight(1f).fillMaxWidth().background(Surface2).border(1.dp, TextMuted))
                    Box(Modifier.weight(1f).fillMaxWidth().background(Surface2).border(1.dp, TextMuted))
                }
            }
            LayoutType.GRID_2X2 -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                    }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                    }
                }
            }
            LayoutType.GRID_2X3 -> {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                    }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                    }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                    }
                }
            }
            LayoutType.GRID_2X4 -> {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                    }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                    }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                    }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                        Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                    }
                }
            }
            LayoutType.CUSTOM -> {
                if (customTemplate != null && customTemplate.frames.isNotEmpty()) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.width / customTemplate.aspectRatio
                        val startY = (size.height - canvasH) / 2f
                        
                        drawRect(
                            color = Surface2, 
                            topLeft = androidx.compose.ui.geometry.Offset(0f, startY), 
                            size = androidx.compose.ui.geometry.Size(canvasW, canvasH)
                        )
                        
                        customTemplate.frames.forEach { frame ->
                            drawRect(
                                color = TextMuted,
                                topLeft = androidx.compose.ui.geometry.Offset(canvasW * frame.x, startY + canvasH * frame.y),
                                size = androidx.compose.ui.geometry.Size(canvasW * frame.width, canvasH * frame.height),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.weight(1.5f).fillMaxWidth().background(Surface2).border(1.dp, TextMuted))
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                            Box(Modifier.weight(1f).fillMaxHeight().background(Surface2).border(1.dp, TextMuted))
                        }
                    }
                }
            }
        }
    }
}

