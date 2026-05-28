package com.oh.shoot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.oh.shoot.navigation.AppNavGraph
import com.oh.shoot.printer.PrinterState
import com.oh.shoot.ui.components.SettingsPanel
import com.oh.shoot.ui.theme.OhShootTheme
import com.oh.shoot.viewmodel.SessionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OhShootTheme {
                val navController = rememberNavController()
                val sessionViewModel: SessionViewModel = hiltViewModel()
                val uiState by sessionViewModel.uiState.collectAsState()
                
                var showSettings by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavGraph(navController = navController, viewModel = sessionViewModel)
                        
                        // Persistent Status Dot + Gear Icon
                        val printerState by sessionViewModel.printerState.collectAsState()
                        val dotColor = when (printerState) {
                            is PrinterState.Ready -> Color(0xFF4CAF50) // Green
                            is PrinterState.Connecting -> Color(0xFFFFEB3B) // Yellow
                            is PrinterState.Disconnected -> Color.Gray
                            is PrinterState.Error -> Color(0xFFF44336) // Red
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(dotColor, CircleShape)
                                    .clickable(enabled = printerState is PrinterState.Disconnected) {
                                        sessionViewModel.checkPrinterStatus()
                                    }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { showSettings = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    if (showSettings) {
                        val printerState by sessionViewModel.printerState.collectAsState()
                        SettingsPanel(
                            settings = uiState.appSettings,
                            printerState = printerState,
                            onSettingsChanged = { sessionViewModel.updateSettings(it) },
                            onDismiss = { showSettings = false }
                        )
                    }
                }
            }
        }
    }
}
