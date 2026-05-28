package com.oh.shoot.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import com.oh.shoot.ui.theme.*

@Composable
fun LayoutSelectScreen(
    onLayoutSelected: (Int) -> Unit
) {
    var selectedId by remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose your layout",
            style = MaterialTheme.typography.headlineMedium,
            color = AccentCream
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LayoutCard(
                modifier = Modifier.weight(1f),
                title = "1 Photo",
                isSelected = selectedId == 1,
                onClick = { selectedId = 1 }
            ) {
                LayoutDiagram(1)
            }
            LayoutCard(
                modifier = Modifier.weight(1f),
                title = "2 Photos",
                isSelected = selectedId == 2,
                onClick = { selectedId = 2 }
            ) {
                LayoutDiagram(2)
            }
            LayoutCard(
                modifier = Modifier.weight(1f),
                title = "Strip",
                isSelected = selectedId == 3,
                onClick = { selectedId = 3 }
            ) {
                LayoutDiagram(3)
            }
            LayoutCard(
                modifier = Modifier.weight(1f),
                title = "2x2 Grid",
                isSelected = selectedId == 4,
                onClick = { selectedId = 4 }
            ) {
                LayoutDiagram(4)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onLayoutSelected(selectedId) },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGold,
                contentColor = Surface
            ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text("CONTINUE", style = MaterialTheme.typography.headlineMedium)
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
                .aspectRatio(0.7f)
                .fillMaxWidth()
        ) {
            content()
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = title, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun LayoutDiagram(id: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        when (id) {
            1 -> Box(
                Modifier
                    .fillMaxSize()
                    .background(Surface2)
                    .border(1.dp, TextMuted)
            )
            2 -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Surface2)
                        .border(1.dp, TextMuted))
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Surface2)
                        .border(1.dp, TextMuted))
            }
            3 -> Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Surface2)
                        .border(1.dp, TextMuted))
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Surface2)
                        .border(1.dp, TextMuted))
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Surface2)
                        .border(1.dp, TextMuted))
            }
            4 -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Surface2)
                            .border(1.dp, TextMuted))
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Surface2)
                            .border(1.dp, TextMuted))
                }
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Surface2)
                            .border(1.dp, TextMuted))
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Surface2)
                            .border(1.dp, TextMuted))
                }
            }
        }
    }
}
