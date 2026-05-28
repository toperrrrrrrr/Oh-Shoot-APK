package com.oh.shoot.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oh.shoot.BitmapProcessor
import com.oh.shoot.BluetoothPrinter
import com.oh.shoot.domain.AppSettings
import com.oh.shoot.printer.EscPosBuilder
import com.oh.shoot.printer.PrinterState
import com.oh.shoot.printer.UsbPrinterManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val printerManager = UsbPrinterManager(context)
    
    private val _printerState = MutableStateFlow<PrinterState>(PrinterState.Disconnected)
    val printerState: StateFlow<PrinterState> = _printerState.asStateFlow()

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        printerManager.discoverAndConnect()
        
        viewModelScope.launch {
            printerManager.printerState.collect {
                checkPrinterStatus()
            }
        }
    }

    fun checkPrinterStatus() {
        val settings = _uiState.value.appSettings
        if (settings.useBluetoothPrinter) {
            val device = BluetoothPrinter.findPairedPrinter(context)
            if (device != null) {
                _printerState.value = PrinterState.Ready
            } else {
                _printerState.value = PrinterState.Error("No paired Bluetooth printer found")
            }
        } else {
            _printerState.value = printerManager.printerState.value
        }
    }

    fun printSession() {
        val state = _uiState.value
        val photos = state.capturedPhotos.filterNotNull()
        if (photos.isEmpty()) return

        _uiState.update { it.copy(isPrinting = true) }

        viewModelScope.launch(Dispatchers.IO) {
            val builder = EscPosBuilder()
            builder.init()
            builder.center()
            builder.bold(true).text(state.appSettings.headerText).bold(false).newline(1)

            photos.forEach { bitmap ->
                val resized = BitmapProcessor.resize(bitmap, 576)
                val raster = BitmapProcessor.convertToEscPosRasterDithered(resized, state.appSettings.contrastBoost)
                builder.printBitmap(resized, raster).newline(1)
            }

            builder.text(state.appSettings.footerText).newline(1)
            builder.feedAndCut()

            val data = builder.build()
            
            repeat(state.copyCount) {
                if (state.appSettings.useBluetoothPrinter) {
                    val device = BluetoothPrinter.findPairedPrinter(context)
                    if (device != null) {
                        val result = BluetoothPrinter.printData(context, device, data)
                        if (result.isFailure) {
                            Log.e("SessionViewModel", "Bluetooth print failed", result.exceptionOrNull())
                            _printerState.value = PrinterState.Error("Bluetooth print failed: ${result.exceptionOrNull()?.message}")
                        }
                    } else {
                        Log.e("SessionViewModel", "No Bluetooth printer found during printing")
                        _printerState.value = PrinterState.Error("No paired Bluetooth printer found")
                    }
                } else {
                    printerManager.print(data)
                }
            }
            
            _uiState.update { it.copy(isPrinting = false) }
        }
    }

    fun startSession(layout: Int) {
        _uiState.update { 
            it.copy(
                selectedLayout = layout,
                capturedPhotos = List(layout) { null },
                currentShotIndex = 0,
                sessionComplete = false
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
        _uiState.update { it.copy(appSettings = settings) }
        checkPrinterStatus()
    }

    fun resetSession() {
        _uiState.update { SessionUiState(appSettings = it.appSettings) }
    }

    override fun onCleared() {
        super.onCleared()
        printerManager.release()
    }
}
