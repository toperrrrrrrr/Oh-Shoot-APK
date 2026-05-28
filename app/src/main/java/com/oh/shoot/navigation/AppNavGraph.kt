package com.oh.shoot.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.oh.shoot.ui.screens.*
import com.oh.shoot.viewmodel.SessionViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: SessionViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) { 
            SplashScreen(onTimeout = {
                navController.navigate(Screen.Standby.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Standby.route) { 
            StandbyScreen(onTap = {
                navController.navigate(Screen.LayoutSelect.route)
            })
        }
        composable(Screen.LayoutSelect.route) { 
            LayoutSelectScreen(onLayoutSelected = { layout ->
                viewModel.startSession(layout)
                navController.navigate(Screen.Capture.route)
            })
        }
        composable(Screen.Capture.route) { 
            val uiState by viewModel.uiState.collectAsState()
            CaptureScreen(
                maxPhotos = uiState.selectedLayout,
                currentPhotoIndex = uiState.currentShotIndex,
                onPhotoCaptured = { bitmap ->
                    viewModel.savePhoto(uiState.currentShotIndex, bitmap)
                },
                onAllPhotosCaptured = {
                    navController.navigate(Screen.Preview.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Preview.route) { 
            val uiState by viewModel.uiState.collectAsState()
            PreviewScreen(
                photos = uiState.capturedPhotos,
                copyCount = uiState.copyCount,
                onRetakePhoto = { index ->
                    viewModel.retakePhoto(index)
                    navController.navigate(Screen.Capture.route) {
                        popUpTo(Screen.Preview.route) { inclusive = true }
                    }
                },
                onRetakeAll = {
                    viewModel.retakeAll()
                    navController.navigate(Screen.Capture.route) {
                        popUpTo(Screen.Preview.route) { inclusive = true }
                    }
                },
                onSetCopyCount = { count ->
                    viewModel.setCopyCount(count)
                },
                onPrint = {
                    navController.navigate(Screen.Output.route)
                }
            )
        }
        composable(Screen.Output.route) { 
            val uiState by viewModel.uiState.collectAsState()
            val printerState by viewModel.printerState.collectAsState()
            OutputScreen(
                photos = uiState.capturedPhotos,
                headerText = uiState.appSettings.headerText,
                footerText = uiState.appSettings.footerText,
                printerState = printerState,
                onBack = {
                    navController.popBackStack()
                },
                onPrintConfirmed = {
                    viewModel.printSession()
                    navController.navigate(Screen.Done.route)
                }
            )
        }
        composable(Screen.Done.route) { 
            DoneScreen(onNewSession = {
                viewModel.resetSession()
                navController.navigate(Screen.Standby.route) {
                    popUpTo(Screen.Standby.route) { inclusive = true }
                }
            })
        }
    }
}
