package com.oh.shoot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
                        
                        // Persistent Gear Icon
                        IconButton(
                            onClick = { showSettings = true },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White.copy(alpha = 0.5f)
                            )
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
