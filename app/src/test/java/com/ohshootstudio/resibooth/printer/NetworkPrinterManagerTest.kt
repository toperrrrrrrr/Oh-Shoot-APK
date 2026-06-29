package com.ohshootstudio.resibooth.printer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.fail
import java.net.ServerSocket

class NetworkPrinterManagerTest {

    @Test
    fun testConcurrentPrintData_RaceCondition_Bug_Reproduction() {
        // This test simulates the issue where multiple rapid print requests
        // attempt to open multiple concurrent sockets to the printer.
        
        runBlocking(Dispatchers.IO) {
            // Start a mock server that simulates a single-connection thermal printer
            // It accepts one connection and blocks/rejects others
            var server: ServerSocket? = null
            try {
                server = ServerSocket(0) // Random free port
                val port = server.localPort
                
                // Fire off 3 concurrent print requests
                val jobs = (1..3).map {
                    async {
                        NetworkPrinterManager.printData("127.0.0.1", port, byteArrayOf(0x01))
                    }
                }
                
                // The current implementation of NetworkPrinterManager does not use a Mutex.
                // It will fire all 3 Socket().connect() calls simultaneously.
                // The mock server will only be able to accept one at a time.
                
                // Accept the first connection
                val socket1 = server.accept()
                
                // In a real thermal printer, subsequent connections would be rejected
                // since the printer only supports 1 TCP session on port 9100.
                // This test serves as a harness. Once Mutex is added, we can assert
                // that the connections happen sequentially.
                
                socket1.close()
                
                // Allow jobs to finish (they may fail with timeouts/connection refused depending on OS)
                jobs.awaitAll()
                
            } catch (e: Exception) {
                fail("Test failed: ${e.message}")
            } finally {
                server?.close()
            }
        }
    }
}

