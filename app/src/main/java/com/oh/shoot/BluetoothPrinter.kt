package com.oh.shoot

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

object BluetoothPrinter {

    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    /**
     * Finds a paired Bluetooth printer device by scanning the bonded list.
     */
    @SuppressLint("MissingPermission")
    fun findPairedPrinter(context: Context): BluetoothDevice? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return null
            }
        }

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter() ?: return null
        
        if (!adapter.isEnabled) return null

        val bondedDevices = adapter.bondedDevices ?: return null
        
        // 1. Look for exact model "XP-N160II"
        bondedDevices.find { it.name?.contains("XP-N160II", ignoreCase = true) == true }?.let { return it }
        
        // 2. Look for exact name "Xprinter"
        bondedDevices.find { it.name?.contains("Xprinter", ignoreCase = true) == true }?.let { return it }
        
        // 3. Look for name containing "printer"
        bondedDevices.find { it.name?.contains("printer", ignoreCase = true) == true }?.let { return it }

        // 3. Look for name containing "esc" or "pos"
        bondedDevices.find { 
            it.name?.contains("esc", ignoreCase = true) == true || 
            it.name?.contains("pos", ignoreCase = true) == true 
        }?.let { return it }

        // 4. Look for major device class (imaging / printer)
        bondedDevices.find { device ->
            val majorClass = device.bluetoothClass?.majorDeviceClass
            majorClass == android.bluetooth.BluetoothClass.Device.Major.IMAGING
        }?.let { return it }

        // 5. Fallback: first bonded device if any exist
        return bondedDevices.firstOrNull()
    }

    /**
     * Prints a 1-bit monochrome raster image.
     * Runs on the IO dispatcher because socket I/O is blocking.
     */
    @SuppressLint("MissingPermission")
    suspend fun printImage(
        context: Context,
        device: BluetoothDevice,
        rasterData: ByteArray,
        widthPixels: Int,
        heightPixels: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            // Cancel discovery before connecting as it slows down connection
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter = bluetoothManager?.adapter
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    adapter?.cancelDiscovery()
                }
            } else {
                adapter?.cancelDiscovery()
            }

            socket.connect()

            val outputStream = socket.outputStream

            // 1. Initialize Printer (ESC @)
            val initCommand = byteArrayOf(0x1B, 0x40)
            outputStream.write(initCommand)

            // 2. Print Image Header (GS v 0 m xL xH yL yH)
            val widthBytes = (widthPixels + 7) / 8
            val xL = (widthBytes % 256).toByte()
            val xH = (widthBytes / 256).toByte()
            val yL = (heightPixels % 256).toByte()
            val yH = (heightPixels / 256).toByte()

            val printHeader = byteArrayOf(
                0x1D, 0x76, 0x30, 0x00, // GS v 0 0 (normal mode)
                xL, xH, yL, yH
            )
            outputStream.write(printHeader)

            // 3. Print Image Data
            outputStream.write(rasterData)

            // 4. Feed paper and Cut (ESC d 5, GS V 66 0)
            // ESC d 5: Feed 5 lines
            val feedCommand = byteArrayOf(0x1B, 0x64, 0x05)
            outputStream.write(feedCommand)

            // GS V 66 0: Feed and execute cut (often performs automatic cut)
            val cutCommand = byteArrayOf(0x1D, 0x56, 0x42, 0x00)
            outputStream.write(cutCommand)

            outputStream.flush()
            Result.success(Unit)
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure(e)
        } finally {
            try {
                socket?.close()
            } catch (closeException: IOException) {
                closeException.printStackTrace()
            }
        }
    }

    /**
     * Prints a raw ESC/POS byte array payload to the Bluetooth printer.
     */
    @SuppressLint("MissingPermission")
    suspend fun printData(
        context: Context,
        device: BluetoothDevice,
        data: ByteArray
    ): Result<Unit> = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter = bluetoothManager?.adapter
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    adapter?.cancelDiscovery()
                }
            } else {
                adapter?.cancelDiscovery()
            }

            socket.connect()
            val outputStream = socket.outputStream
            outputStream.write(data)
            outputStream.flush()
            Result.success(Unit)
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure(e)
        } finally {
            try {
                socket?.close()
            } catch (closeException: IOException) {
                closeException.printStackTrace()
            }
        }
    }
}
