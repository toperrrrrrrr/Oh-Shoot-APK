package com.oh.shoot.printer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

object NetworkPrinterManager {

    /**
     * Prints a raw ESC/POS byte array payload to the Network printer, supporting multiple copies.
     */
    suspend fun printData(
        ipAddress: String,
        port: Int,
        data: ByteArray,
        copies: Int = 1,
        delayBetweenCopiesMs: Long = 1000L
    ): Result<Unit> = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        try {
            socket = Socket()
            // Connect with a 5-second timeout
            socket.connect(InetSocketAddress(ipAddress, port), 5000)
            socket.soTimeout = 10000 // 10 seconds read/write timeout

            val outputStream = socket.getOutputStream()

            repeat(copies) { index ->
                outputStream.write(data)
                outputStream.flush()
                
                if (index < copies - 1) {
                    delay(delayBetweenCopiesMs)
                }
            }
            Result.success(Unit)
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure(e)
        } finally {
            try {
                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Tests the TCP connection to the printer port.
     */
    suspend fun testConnection(ipAddress: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        try {
            withTimeout(5000L) {
                socket = Socket()
                socket?.connect(InetSocketAddress(ipAddress, port), 3000)
            }
            true
        } catch (e: Exception) {
            false
        } finally {
            try {
                socket?.close()
            } catch (e: IOException) {
                // Ignore
            }
        }
    }
}
