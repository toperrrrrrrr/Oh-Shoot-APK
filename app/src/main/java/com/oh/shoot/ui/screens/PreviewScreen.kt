package com.oh.shoot.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.oh.shoot.ui.theme.*

@Composable
fun PreviewScreen(
    photos: List<Bitmap?>,
    copyCount: Int,
    onRetakePhoto: (Int) -> Unit,
    onRetakeAll: () -> Unit,
    onSetCopyCount: (Int) -> Unit,
    onPrint: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Review your photos",
            style = MaterialTheme.typography.headlineMedium,
            color = AccentCream
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Photos Grid
        Box(modifier = Modifier.weight(1f)) {
            val columns = if (photos.size == 4) 2 else photos.size
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (columns > 0) columns else 1),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(photos) { index, bitmap ->
                    PhotoThumbnail(
                        bitmap = bitmap,
                        onClick = { onRetakePhoto(index) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Copy Stepper
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Copies:", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = { onSetCopyCount(copyCount - 1) },
                colors = IconButtonDefaults.iconButtonColors(contentColor = AccentGold)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }
            Text(
                text = copyCount.toString(),
                color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(
                onClick = { onSetCopyCount(copyCount + 1) },
                colors = IconButtonDefaults.iconButtonColors(contentColor = AccentGold)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onRetakeAll,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGold),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("RETAKE ALL")
            }
            
            Button(
                onClick = onPrint,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Surface),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("PRINT")
            }
        }
    }
}

@Composable
fun PhotoThumbnail(bitmap: Bitmap?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(8.dp))
            .background(Surface2)
            .clickable { onClick() }
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RETAKE",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
