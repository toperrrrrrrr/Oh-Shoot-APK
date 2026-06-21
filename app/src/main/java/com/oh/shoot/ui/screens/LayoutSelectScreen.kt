package com.oh.shoot.ui.screens

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
import com.oh.shoot.ui.theme.*

enum class LayoutType {
    SINGLE,
    STRIP_2,
    STRIP_3,
    GRID_2X2,
    GRID_2X3,
    HYBRID
}

data class LayoutOption(
    val type: LayoutType,
    val name: String,
    val photoCount: Int
)

private val layoutOptions = listOf(
    LayoutOption(LayoutType.SINGLE, "Single Photo", 1),
    LayoutOption(LayoutType.STRIP_2, "2-Photo Strip", 2),
    LayoutOption(LayoutType.STRIP_3, "3-Photo Strip", 3),
    LayoutOption(LayoutType.GRID_2X2, "2x2 Grid (1:1)", 4),
    LayoutOption(LayoutType.GRID_2X3, "2x3 Grid", 6),
    LayoutOption(LayoutType.HYBRID, "Hybrid Grid", 3)
)

@Composable
fun LayoutSelectScreen(
    onLayoutSelected: (LayoutOption) -> Unit,
    onCancel: () -> Unit
) {
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
            modifier = Modifier.fillMaxWidth(),
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
                    LayoutDiagram(option.type)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

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
fun LayoutDiagram(type: LayoutType) {
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
            LayoutType.HYBRID -> {
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
