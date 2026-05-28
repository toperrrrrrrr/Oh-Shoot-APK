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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oh.shoot.ui.theme.*

data class LayoutOption(
    val id: Int,
    val name: String,
    val columns: Int,
    val rows: Int,
    val photoCount: Int
)

private val layoutOptions = listOf(
    LayoutOption(id = 1, name = "1 Photo", columns = 1, rows = 1, photoCount = 1),
    LayoutOption(id = 2, name = "2 Strip", columns = 1, rows = 2, photoCount = 2),
    LayoutOption(id = 3, name = "3 Strip", columns = 1, rows = 3, photoCount = 3),
    LayoutOption(id = 4, name = "2x2 Grid", columns = 2, rows = 2, photoCount = 4)
)

@Composable
fun LayoutSelectScreen(
    onLayoutSelected: (Int) -> Unit
) {
    var selectedId by remember { mutableIntStateOf(layoutOptions.first().id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose your layout",
            style = MaterialTheme.typography.titleLarge,
            color = AccentCream
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(layoutOptions, key = { it.id }) { option ->
                LayoutCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = option.name,
                    isSelected = selectedId == option.id,
                    onClick = { selectedId = option.id }
                ) {
                    LayoutDiagram(option)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        val selectedOption = layoutOptions.firstOrNull { it.id == selectedId } ?: layoutOptions.first()

        Button(
            onClick = { onLayoutSelected(selectedOption.photoCount) },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 200.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGold,
                contentColor = Surface
            ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text("CONTINUE", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
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
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(0.95f)
                .fillMaxWidth()
        ) {
            content()
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LayoutDiagram(option: LayoutOption) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(option.rows) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    repeat(option.columns) {
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Surface2)
                                .border(1.dp, TextMuted)
                        )
                    }
                }
            }
        }
    }
}
