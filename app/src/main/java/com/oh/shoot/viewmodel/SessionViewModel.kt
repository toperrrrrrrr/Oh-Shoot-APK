package com.oh.shoot.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oh.shoot.BitmapProcessor
import com.oh.shoot.BluetoothPrinter
import com.oh.shoot.data.SettingsRepository
import com.oh.shoot.domain.AppSettings
import com.oh.shoot.printer.EscPosBuilder
import com.oh.shoot.printer.PrinterState
import com.oh.shoot.printer.UsbPrinterManager
import com.oh.shoot.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SessionUiState(
    val selectedLayout: Int = 1,
    val capturedPhotos: List<Bitmap?> = emptyList(),
    val currentShotIndex: Int = 0,
    val copyCount: Int = 1,
    val sessionComplete: Boolean = false,
    val appSettings: AppSettings = AppSettings(),
    val isPrinting: Boolean = false
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val printerManager = UsbPrinterManager(context)
    
    private val _printerState = MutableStateFlow<PrinterState>(PrinterState.Disconnected)
    val printerState: StateFlow<PrinterState> = _printerState.asStateFlow()

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        printerManager.discoverAndConnect()

        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.update { it.copy(appSettings = settings) }
                checkPrinterStatus()
            }
        }

        viewModelScope.launch {
            printerManager.printerState.collect {
                checkPrinterStatus()
            }
        }
    }

    fun checkPrinterStatus() {
        val settings = _uiState.value.appSettings
        if (!settings.useBluetoothPrinter) {
            _printerState.value = printerManager.printerState.value
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _printerState.value = PrinterState.Connecting
            val device = BluetoothPrinter.findPairedPrinter(context)
            if (device == null) {
                _printerState.value = PrinterState.Error("No paired Bluetooth printer found")
                return@launch
            }

            val canConnect = BluetoothPrinter.testConnection(context, device)
            _printerState.value = if (canConnect) {
                PrinterState.Ready
            } else {
                PrinterState.Error("Bluetooth printer is not reachable")
            }
        }
    }

    private fun ensurePrinterReady(): Boolean {
        val current = _printerState.value
        if (current is PrinterState.Ready) {
            return true
        }
        _printerState.value = when (current) {
            is PrinterState.Error -> current
            is PrinterState.Connecting -> PrinterState.Error("Printer still connecting. Try again.")
            is PrinterState.Disconnected -> PrinterState.Error("Printer is disconnected.")
            else -> PrinterState.Error("Printer is not ready.")
        }
        return false
    }

    fun printSession(onFinished: (Boolean) -> Unit) {
        val currentState = _uiState.value
        val photos = currentState.capturedPhotos.filterNotNull()
        if (photos.isEmpty()) {
            onFinished(false)
            return
        }

        if (!ensurePrinterReady()) {
            Log.e("SessionViewModel", "Print blocked: Printer is not ready")
            onFinished(false)
            return
        }

        _uiState.update { it.copy(isPrinting = true) }

        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val builder = EscPosBuilder()
            builder.init()
            builder.center()

            val printWidth = if (state.appSettings.paperWidth80mm) 576 else 384
            val logoUri = state.appSettings.customLogoUri
            val headerLogo = logoUri?.let {
                ImageUtils.decodeBitmapFromUri(context, it, printWidth)
            }?.let { ImageUtils.resizeToWidth(it, printWidth) }

            if (headerLogo != null) {
                val logoRaster = BitmapProcessor.convertToEscPosRasterDithered(
                    headerLogo,
                    state.appSettings.contrastBoost
                )
                builder.printBitmap(headerLogo, logoRaster).newline(1)
            } else {
                builder.bold(true).text(state.appSettings.headerText).bold(false).newline(1)
            }

            val printableBitmaps = if (state.selectedLayout == 4) {
                listOf(BitmapProcessor.combineBitmapsToGrid(photos.take(4), printWidth))
            } else {
                photos.map { bitmap -> BitmapProcessor.resize(bitmap, printWidth) }
            }

            printableBitmaps.forEach { bitmap ->
                val raster = BitmapProcessor.convertToEscPosRasterDithered(bitmap, state.appSettings.contrastBoost)
                builder.printBitmap(bitmap, raster).newline(1)
            }

            builder.text(state.appSettings.footerText).newline(1)
            if (state.appSettings.autoCut) {
                builder.feedAndCut()
            } else {
                builder.newline(6)
            }

            val data = builder.build()

            val savedAll = photos.mapIndexed { index, bitmap ->
                val timestamp = System.currentTimeMillis()
                ImageUtils.saveToGallery(context, bitmap, "OhShoot_${timestamp}_$index")
            }.all { it != null }

            var printSucceeded = true
            repeat(state.copyCount) {
                if (state.appSettings.useBluetoothPrinter) {
                    val device = BluetoothPrinter.findPairedPrinter(context)
                    if (device != null) {
                        val result = BluetoothPrinter.printData(context, device, data)
                        if (result.isFailure) {
                            Log.e("SessionViewModel", "Bluetooth print failed", result.exceptionOrNull())
                            _printerState.value = PrinterState.Error("Bluetooth print failed: ${result.exceptionOrNull()?.message}")
                            printSucceeded = false
                        }
                    } else {
                        Log.e("SessionViewModel", "No Bluetooth printer found during printing")
                        _printerState.value = PrinterState.Error("No paired Bluetooth printer found")
                        printSucceeded = false
                    }
                } else {
                    val success = printerManager.print(data)
                    if (!success) {
                        _printerState.value = PrinterState.Error("USB Print failed. Check cable.")
                        printSucceeded = false
                    }
                }
            }

            if (!savedAll) {
                _printerState.value = PrinterState.Error("Print sent, but failed to save one or more photos to gallery.")
                printSucceeded = false
            }

            _uiState.update { it.copy(isPrinting = false) }
            withContext(Dispatchers.Main) {
                onFinished(printSucceeded)
            }
        }
    }

    fun startSession(layout: Int) {
        _uiState.update { 
            it.copy(
                selectedLayout = layout,
                capturedPhotos = List(layout) { null },
                currentShotIndex = 0,
                sessionComplete = false,
                copyCount = it.appSettings.defaultCopies
            )
        }
    }

    fun savePhoto(index: Int, bitmap: Bitmap) {
        _uiState.update { state ->
            val newPhotos = state.capturedPhotos.toMutableList()
            if (index in newPhotos.indices) {
                newPhotos[index] = bitmap
            }
            state.copy(
                capturedPhotos = newPhotos,
                currentShotIndex = state.currentShotIndex + 1,
                sessionComplete = newPhotos.all { it != null }
            )
        }
    }

    fun retakePhoto(index: Int) {
        _uiState.update { state ->
            val newPhotos = state.capturedPhotos.toMutableList()
            if (index in newPhotos.indices) {
                newPhotos[index] = null
            }
            state.copy(
                capturedPhotos = newPhotos,
                currentShotIndex = index,
                sessionComplete = false
            )
        }
    }

    fun retakeAll() {
        _uiState.update { state ->
            state.copy(
                capturedPhotos = List(state.selectedLayout) { null },
                currentShotIndex = 0,
                sessionComplete = false
            )
        }
    }

    fun setCopyCount(n: Int) {
        _uiState.update { it.copy(copyCount = n.coerceIn(1, 5)) }
    }

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            settingsRepository.saveSettings(settings)
        }
    }

    fun resetSession() {
        _uiState.update { state ->
            SessionUiState(
                appSettings = state.appSettings,
                copyCount = state.appSettings.defaultCopies
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        printerManager.release()
    }
}
