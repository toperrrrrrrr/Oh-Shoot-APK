package com.ohshootstudio.resibooth.printer

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

class EscPosBuilder {
    private val buffer = ByteArrayOutputStream()

    fun init(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x40))
        return this
    }

    fun center(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x61, 0x01))
        return this
    }

    fun left(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x61, 0x00))
        return this
    }

    fun bold(on: Boolean): EscPosBuilder {
        buffer.write(byteArrayOf(0x1B, 0x45, if (on) 0x01 else 0x00))
        return this
    }

    fun text(str: String): EscPosBuilder {
        buffer.write(str.toByteArray(Charsets.UTF_8))
        return this
    }

    fun newline(n: Int = 1): EscPosBuilder {
        repeat(n) {
            buffer.write(0x0A)
        }
        return this
    }

    fun cutPaper(): EscPosBuilder {
        buffer.write(byteArrayOf(0x1D, 0x56, 0x42, 0x00))
        return this
    }

    fun feedAndCut(): EscPosBuilder {
        newline(4)
        cutPaper()
        return this
    }

    /**
     * Raster bit image (GS v 0)
     */
    fun printBitmap(bitmap: Bitmap, rasterData: ByteArray): EscPosBuilder {
        val width = bitmap.width
        val height = bitmap.height
        val widthBytes = (width + 7) / 8
        
        // GS v 0 m xL xH yL yH
        buffer.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00))
        buffer.write(widthBytes % 256)
        buffer.write(widthBytes / 256)
        buffer.write(height % 256)
        buffer.write(height / 256)
        buffer.write(rasterData)
        return this
    }

    fun build(): ByteArray {
        return buffer.toByteArray()
    }
}

