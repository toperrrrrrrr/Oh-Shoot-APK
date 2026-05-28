package com.oh.shoot.printer

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PrinterState {
    object Disconnected : PrinterState()
    object Connecting : PrinterState()
    object Ready : PrinterState()
    data class Error(val message: String) : PrinterState()
}

class UsbPrinterManager(private val context: Context) {
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var usbDevice: UsbDevice? = null
    private var usbInterface: UsbInterface? = null
    private var usbEndpoint: UsbEndpoint? = null
    private var usbConnection: UsbDeviceConnection? = null

    private val _printerState = MutableStateFlow<PrinterState>(PrinterState.Disconnected)
    val printerState = _printerState.asStateFlow()

    private val ACTION_USB_PERMISSION = "com.oh.shoot.USB_PERMISSION"

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_USB_PERMISSION -> {
                    synchronized(this) {
                        val device = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        }
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            device?.let { connect(it) }
                        } else {
                            _printerState.value = PrinterState.Error("USB permission denied")
                        }
                    }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    discoverAndConnect()
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                    if (device != null && device == usbDevice) {
                        disconnect()
                    }
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        ContextCompat.registerReceiver(context, usbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    fun discoverAndConnect() {
        val deviceList = usbManager.deviceList
        val printer = deviceList.values.find { 
            it.vendorId == 0x0FE6 || it.deviceClass == UsbConstants.USB_CLASS_PRINTER 
        }

        if (printer != null) {
            if (usbManager.hasPermission(printer)) {
                connect(printer)
            } else {
                val permissionIntent = PendingIntent.getBroadcast(
                    context, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
                )
                usbManager.requestPermission(printer, permissionIntent)
            }
        } else {
            _printerState.value = PrinterState.Disconnected
        }
    }

    private fun connect(device: UsbDevice) {
        _printerState.value = PrinterState.Connecting
        
        // Find printer interface
        val iface = (0 until device.interfaceCount).map { device.getInterface(it) }
            .find { it.interfaceClass == UsbConstants.USB_CLASS_PRINTER }
            ?: device.getInterface(0)

        val endpoint = (0 until iface.endpointCount).map { iface.getEndpoint(it) }
            .find { it.direction == UsbConstants.USB_DIR_OUT && it.type == UsbConstants.USB_ENDPOINT_XFER_BULK }

        if (endpoint == null) {
            _printerState.value = PrinterState.Error("No bulk-out endpoint found")
            return
        }

        val connection = usbManager.openDevice(device)
        if (connection != null && connection.claimInterface(iface, true)) {
            usbDevice = device
            usbInterface = iface
            usbEndpoint = endpoint
            usbConnection = connection
            _printerState.value = PrinterState.Ready
        } else {
            _printerState.value = PrinterState.Error("Failed to open USB device connection")
        }
    }

    fun print(data: ByteArray): Boolean {
        val conn = usbConnection ?: return false
        val ep = usbEndpoint ?: return false

        // Write in chunks
        val chunkSize = 16384
        var offset = 0
        while (offset < data.size) {
            val length = minOf(chunkSize, data.size - offset)
            val result = conn.bulkTransfer(ep, data, offset, length, 5000)
            if (result < 0) {
                Log.e("UsbPrinterManager", "Bulk transfer failed: $result")
                return false
            }
            offset += length
        }
        return true
    }

    fun disconnect() {
        usbConnection?.close()
        usbConnection = null
        usbDevice = null
        _printerState.value = PrinterState.Disconnected
    }

    fun release() {
        disconnect()
        try {
            context.unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            Log.e("UsbPrinterManager", "Failed to unregister receiver", e)
        }
    }
}
