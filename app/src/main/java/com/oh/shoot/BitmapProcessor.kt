package com.oh.shoot

import android.graphics.Bitmap
import android.graphics.Color

object BitmapProcessor {

    /**
     * Resizes a bitmap to targetWidth while maintaining aspect ratio.
     */
    fun resize(bitmap: Bitmap, targetWidth: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val targetHeight = (targetWidth.toDouble() / width * height).toInt()
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    /**
     * Converts a color bitmap to a 1-bit monochrome ESC/POS raster byte array.
     * Each byte represents 8 horizontal pixels. A '1' bit is black (ink/heat), '0' is white.
     */
    fun convertToEscPosRaster(bitmap: Bitmap, threshold: Int = 128): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        
        // Calculate the number of bytes required for a single row
        val bytesPerRow = (width + 7) / 8
        val dataSize = bytesPerRow * height
        val rasterData = ByteArray(dataSize)

        var byteIndex = 0
        for (y in 0 until height) {
            for (xByte in 0 until bytesPerRow) {
                var currentByte = 0
                for (bit in 0 until 8) {
                    val pixelX = xByte * 8 + bit
                    if (pixelX < width) {
                        val color = bitmap.getPixel(pixelX, y)
                        // Luminosity-based grayscale conversion
                        val r = Color.red(color)
                        val g = Color.green(color)
                        val b = Color.blue(color)
                        val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                        
                        if (gray < threshold) {
                            // Darker than threshold -> print (set bit to 1)
                            // The leftmost pixel in a byte corresponds to the MSB
                            currentByte = currentByte or (1 shl (7 - bit))
                        }
                    }
                }
                rasterData[byteIndex++] = currentByte.toByte()
            }
        }
        return rasterData
    }

    /**
     * Converts a color bitmap to a 1-bit monochrome ESC/POS raster byte array using
     * the Floyd-Steinberg error diffusion algorithm for higher quality halftone shading.
     */
    fun convertToEscPosRasterDithered(bitmap: Bitmap, contrastFactor: Float = 1.4f): ByteArray {
        val width = bitmap.width
        val height = bitmap.height

        // 1. Convert to grayscale float values [0f..255f] with contrast boost
        val grayValues = FloatArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = bitmap.getPixel(x, y)
                val r = android.graphics.Color.red(color)
                val g = android.graphics.Color.green(color)
                val b = android.graphics.Color.blue(color)
                // Standard luminosity formula
                val lum = 0.299f * r + 0.587f * g + 0.114f * b
                
                // Contrast boost: value = clamp((lum - 128) * factor + 128, 0, 255)
                val contrastLum = ((lum - 128f) * contrastFactor + 128f).coerceIn(0f, 255f)
                grayValues[y * width + x] = contrastLum
            }
        }

        // 2. Floyd-Steinberg error diffusion
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val oldVal = grayValues[index]
                
                // Threshold: round to nearest color (0 = black, 255 = white)
                val newVal = if (oldVal < 128f) 0f else 255f
                grayValues[index] = newVal

                val error = oldVal - newVal

                // Distribute error to 4 neighboring pixels:
                // Right (x+1, y) : weight 7/16
                if (x + 1 < width) {
                    grayValues[index + 1] += error * 7f / 16f
                }
                // Bottom-Left (x-1, y+1) : weight 3/16
                if (x - 1 >= 0 && y + 1 < height) {
                    grayValues[index + width - 1] += error * 3f / 16f
                }
                // Bottom (x, y+1) : weight 5/16
                if (y + 1 < height) {
                    grayValues[index + width] += error * 5f / 16f
                }
                // Bottom-Right (x+1, y+1) : weight 1/16
                if (x + 1 < width && y + 1 < height) {
                    grayValues[index + width + 1] += error * 1f / 16f
                }
            }
        }

        // 3. Pack the dithered array into ESC/POS bytes (8 pixels per byte, MSB-first)
        val bytesPerRow = (width + 7) / 8
        val rasterData = ByteArray(bytesPerRow * height)
        var byteIndex = 0

        for (y in 0 until height) {
            for (xByte in 0 until bytesPerRow) {
                var currentByte = 0
                for (bit in 0 until 8) {
                    val pixelX = xByte * 8 + bit
                    if (pixelX < width) {
                        val valGray = grayValues[y * width + pixelX]
                        if (valGray < 128f) {
                            // Grayscale value < 128 indicates black, so we set the bit to 1
                            currentByte = currentByte or (1 shl (7 - bit))
                        }
                    }
                }
                rasterData[byteIndex++] = currentByte.toByte()
            }
        }

        return rasterData
    }

    /**
     * Combines multiple bitmaps into a 2-column grid.
     */
    fun combineBitmapsToGrid(bitmaps: List<Bitmap>, targetWidth: Int): Bitmap {
        if (bitmaps.isEmpty()) return Bitmap.createBitmap(targetWidth, 1, Bitmap.Config.ARGB_8888)
        
        val colWidth = targetWidth / 2
        // Calculate scaled height for each bitmap to maintain aspect ratio
        val firstBmp = bitmaps[0]
        val scaledHeight = (colWidth.toDouble() / firstBmp.width * firstBmp.height).toInt()
        
        val rows = (bitmaps.size + 1) / 2
        val totalHeight = rows * scaledHeight
        
        val result = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)
        canvas.drawColor(android.graphics.Color.WHITE)
        
        bitmaps.forEachIndexed { index, bitmap ->
            val row = index / 2
            val col = index % 2
            val scaled = Bitmap.createScaledBitmap(bitmap, colWidth, scaledHeight, true)
            canvas.drawBitmap(scaled, (col * colWidth).toFloat(), (row * scaledHeight).toFloat(), null)
        }
        
        return result
    }
}

