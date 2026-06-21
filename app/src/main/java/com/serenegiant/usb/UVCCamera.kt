package com.serenegiant.usb

import android.view.Surface

/**
 * STUB CLASS: This serves as a placeholder to allow the project to compile without the native UVC driver.
 * When the real HDMI capture card arrives, you will replace this package with the actual libuvc `.aar` or local module.
 */
class UVCCamera {

    companion object {
        const val FRAME_FORMAT_YUYV = 0
        const val FRAME_FORMAT_MJPEG = 1
    }

    fun open(ctrlBlock: USBMonitor.UsbControlBlock) {
        // Stub implementation
    }

    fun setPreviewSize(width: Int, height: Int, format: Int) {
        // Stub implementation
    }

    fun setPreviewDisplay(surface: Surface) {
        // Stub implementation
    }

    fun startPreview() {
        // Stub implementation
    }

    fun stopPreview() {
        // Stub implementation
    }

    fun close() {
        // Stub implementation
    }
}
