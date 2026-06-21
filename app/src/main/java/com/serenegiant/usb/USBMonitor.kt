package com.serenegiant.usb

import android.content.Context
import android.hardware.usb.UsbDevice

/**
 * STUB CLASS: This serves as a placeholder to allow the project to compile without the native UVC driver.
 * When the real HDMI capture card arrives, you will replace this package with the actual libuvc `.aar` or local module.
 */
class USBMonitor(context: Context, listener: OnDeviceConnectListener) {

    interface OnDeviceConnectListener {
        fun onAttach(device: UsbDevice)
        fun onDettach(device: UsbDevice)
        fun onConnect(device: UsbDevice, ctrlBlock: UsbControlBlock, createNew: Boolean)
        fun onDisconnect(device: UsbDevice, ctrlBlock: UsbControlBlock)
        fun onCancel(device: UsbDevice)
    }

    class UsbControlBlock

    val deviceList: List<UsbDevice>
        get() = emptyList()

    fun requestPermission(device: UsbDevice) {
        // Stub implementation
    }

    fun destroy() {
        // Stub implementation
    }
}
