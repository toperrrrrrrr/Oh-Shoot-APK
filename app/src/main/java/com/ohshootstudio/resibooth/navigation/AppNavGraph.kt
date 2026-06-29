package com.ohshootstudio.resibooth.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ohshootstudio.resibooth.ui.screens.*
import com.ohshootstudio.resibooth.viewmodel.SessionViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: SessionViewModel,
    isEditingStandby: Boolean = false,
    onExitEditMode: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) { 
            val uiState by viewModel.uiState.collectAsState()
            SplashScreen(appSettings = uiState.appSettings, onTimeout = {
                if (uiState.appSettings.isActivated) {
                    navController.navigate(Screen.Standby.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Activation.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            })
        }
        composable(Screen.Activation.route) {
            val uiState by viewModel.uiState.collectAsState()
            ActivationScreen(onActivate = { key ->
                if (key == "OHSHOOT2024" || key == "ADMIN123") {
                    viewModel.updateSettings(uiState.appSettings.copy(
                        isActivated = true,
                        activationKey = key
                    ))
                    navController.navigate(Screen.Standby.route) {
                        popUpTo(Screen.Activation.route) { inclusive = true }
                    }
                }
            })
        }
        composable(Screen.Standby.route) { 
            val uiState by viewModel.uiState.collectAsState()
            StandbyScreen(
                customLogoUri = uiState.appSettings.customLogoUri,
                standbyImageUri = uiState.appSettings.standbyImageUri,
                isEditing = isEditingStandby,
                offsetX = uiState.appSettings.startButtonOffsetX,
                offsetY = uiState.appSettings.startButtonOffsetY,
                scale = uiState.appSettings.startButtonScale,
                onUpdateLayout = { x, y, s -> 
                    viewModel.updateSettings(uiState.appSettings.copy(
                        startButtonOffsetX = x,
                        startButtonOffsetY = y,
                        startButtonScale = s
                    ))
                },
                onExitEditMode = onExitEditMode,
                onTap = {
                    navController.navigate(Screen.LayoutSelect.route)
                }
            )
        }
        composable(Screen.LayoutSelect.route) { 
            val uiState by viewModel.uiState.collectAsState()
            val template = androidx.compose.runtime.remember(uiState.appSettings.customLayoutTemplate) {
                com.ohshootstudio.resibooth.domain.CustomTemplate.fromJsonString(uiState.appSettings.customLayoutTemplate)
            }
            LayoutSelectScreen(
                customPhotoCount = template.frames.size.coerceAtLeast(1),
                customTemplate = template,
                onLayoutSelected = { option ->
                    viewModel.startSession(option.photoCount, option.type)
                    navController.navigate(Screen.Capture.route)
                },
                onCancel = {
                    viewModel.resetSession()
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Capture.route) { 
            val uiState by viewModel.uiState.collectAsState()
            CaptureScreen(
                maxPhotos = uiState.selectedLayout,
                currentPhotoIndex = uiState.currentShotIndex,
                cameraLensFacing = uiState.appSettings.cameraLensFacing,
                mirrorPreview = uiState.appSettings.mirrorPreview,
                squareMode = uiState.appSettings.squareMode,
                soundsEnabled = uiState.appSettings.soundsEnabled,
                ringLightEnabled = uiState.appSettings.ringLightEnabled,
                onCameraFacingChanged = { facing ->
                    viewModel.updateSettings(uiState.appSettings.copy(cameraLensFacing = facing))
                },
                onPhotoCaptured = { bitmap ->
                    viewModel.savePhoto(uiState.currentShotIndex, bitmap)
                },
                onAllPhotosCaptured = {
                    navController.navigate(Screen.Preview.route)
                },
                onCancel = {
                    viewModel.resetSession()
                    navController.navigate(Screen.Standby.route) {
                        popUpTo(Screen.Standby.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Preview.route) { 
            val uiState by viewModel.uiState.collectAsState()
            PreviewScreen(
                photos = uiState.capturedPhotos,
                previewBitmap = uiState.previewBitmap,
                copyCount = uiState.copyCount,
                squareMode = uiState.appSettings.squareMode,
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
                },
                onCancel = {
                    viewModel.resetSession()
                    navController.navigate(Screen.Standby.route) {
                        popUpTo(Screen.Standby.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Output.route) { 
            val uiState by viewModel.uiState.collectAsState()
            val printerState by viewModel.printerState.collectAsState()
            OutputScreen(
                photos = uiState.capturedPhotos,
                appSettings = uiState.appSettings,
                layoutType = uiState.selectedLayoutType,
                printerState = printerState,
                onRetryConnection = {
                    viewModel.checkPrinterStatus()
                },
                onCancel = {
                    viewModel.resetSession()
                    navController.navigate(Screen.Standby.route) {
                        popUpTo(Screen.Standby.route) { inclusive = true }
                    }
                },
                onPrintConfirmed = {
                    viewModel.printSession { success ->
                        if (success) {
                            navController.navigate(Screen.Done.route)
                        }
                    }
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

