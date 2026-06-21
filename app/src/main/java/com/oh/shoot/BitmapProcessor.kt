package com.oh.shoot

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.oh.shoot.ui.screens.LayoutType

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
     * Crops a bitmap to a square aspect ratio.
     */
    fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    /**
     * Applies corner radius to a bitmap.
     */
    fun addCornerRadius(bitmap: Bitmap, radius: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        canvas.drawRoundRect(rectF, radius, radius, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    /**
     * Applies a decorative border to a bitmap.
     */
    fun applyBorder(bitmap: Bitmap, designId: Int): Bitmap {
        if (designId == 0) return bitmap
        
        val borderWidth = (bitmap.width * 0.05f).toInt() // 5% border
        val output = Bitmap.createBitmap(
            bitmap.width + borderWidth * 2,
            bitmap.height + borderWidth * 2,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        
        // Simple colored border based on designId
        val paint = Paint().apply {
            color = when (designId) {
                1 -> Color.BLACK
                2 -> Color.LTGRAY
                else -> Color.WHITE
            }
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, output.width.toFloat(), output.height.toFloat(), paint)
        
        canvas.drawBitmap(bitmap, borderWidth.toFloat(), borderWidth.toFloat(), null)
        
        return output
    }

    /**
     * Combines multiple bitmaps into a grid based on LayoutType.
     */
    fun combineBitmapsToGrid(
        bitmaps: List<Bitmap>, 
        targetWidth: Int, 
        type: LayoutType,
        cornerRadius: Float = 0f,
        squareMode: Boolean = false,
        borderDesignId: Int = 0,
        customTemplate: com.oh.shoot.domain.CustomTemplate? = null
    ): Bitmap {
        if (bitmaps.isEmpty()) return Bitmap.createBitmap(targetWidth, 1, Bitmap.Config.ARGB_8888)

        // Pre-process bitmaps: crop to square if needed, add corner radius, add border
        val processedBitmaps = bitmaps.map { bmp ->
            var b = if (squareMode) cropToSquare(bmp) else bmp
            if (cornerRadius > 0f) b = addCornerRadius(b, cornerRadius)
            if (borderDesignId > 0) b = applyBorder(b, borderDesignId)
            b
        }

        return when (type) {
            LayoutType.SINGLE -> {
                val firstBmp = processedBitmaps[0]
                val scaledHeight = (targetWidth.toDouble() / firstBmp.width * firstBmp.height).toInt()
                
                val result = Bitmap.createBitmap(targetWidth, scaledHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(result)
                canvas.drawColor(Color.WHITE)
                
                val scaled = Bitmap.createScaledBitmap(firstBmp, targetWidth, scaledHeight, true)
                canvas.drawBitmap(scaled, 0f, 0f, null)
                result
            }
            LayoutType.STRIP_2 -> {
                val firstBmp = processedBitmaps[0]
                val scaledHeight = (targetWidth.toDouble() / firstBmp.width * firstBmp.height).toInt()
                val totalHeight = 2 * scaledHeight
                
                val result = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(result)
                canvas.drawColor(Color.WHITE)
                
                processedBitmaps.take(2).forEachIndexed { index, bitmap ->
                    val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, scaledHeight, true)
                    canvas.drawBitmap(scaled, 0f, (index * scaledHeight).toFloat(), null)
                }
                result
            }
            LayoutType.STRIP_3 -> {
                val firstBmp = processedBitmaps[0]
                val scaledHeight = (targetWidth.toDouble() / firstBmp.width * firstBmp.height).toInt()
                val totalHeight = 3 * scaledHeight
                
                val result = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(result)
                canvas.drawColor(Color.WHITE)
                
                processedBitmaps.take(3).forEachIndexed { index, bitmap ->
                    val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, scaledHeight, true)
                    canvas.drawBitmap(scaled, 0f, (index * scaledHeight).toFloat(), null)
                }
                result
            }
            LayoutType.GRID_2X2 -> {
                val colWidth = targetWidth / 2
                val firstBmp = processedBitmaps[0]
                val scaledHeight = (colWidth.toDouble() / firstBmp.width * firstBmp.height).toInt()
                val totalHeight = 2 * scaledHeight
                
                val result = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(result)
                canvas.drawColor(Color.WHITE)
                
                processedBitmaps.take(4).forEachIndexed { index, bitmap ->
                    val row = index / 2
                    val col = index % 2
                    val scaled = Bitmap.createScaledBitmap(bitmap, colWidth, scaledHeight, true)
                    canvas.drawBitmap(scaled, (col * colWidth).toFloat(), (row * scaledHeight).toFloat(), null)
                }
                result
            }
            LayoutType.GRID_2X3 -> {
                val colWidth = targetWidth / 2
                val firstBmp = processedBitmaps[0]
                val scaledHeight = (colWidth.toDouble() / firstBmp.width * firstBmp.height).toInt()
                val totalHeight = 3 * scaledHeight
                
                val result = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(result)
                canvas.drawColor(Color.WHITE)
                
                processedBitmaps.take(6).forEachIndexed { index, bitmap ->
                    val row = index / 2
                    val col = index % 2
                    val scaled = Bitmap.createScaledBitmap(bitmap, colWidth, scaledHeight, true)
                    canvas.drawBitmap(scaled, (col * colWidth).toFloat(), (row * scaledHeight).toFloat(), null)
                }
                result
            }
            LayoutType.CUSTOM -> {
                if (customTemplate == null || customTemplate.frames.isEmpty()) {
                    return Bitmap.createBitmap(targetWidth, targetWidth, Bitmap.Config.ARGB_8888)
                }
                
                val totalHeight = (targetWidth / customTemplate.aspectRatio).toInt()
                val result = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(result)
                canvas.drawColor(Color.WHITE)
                
                customTemplate.frames.forEachIndexed { index, frame ->
                    if (index < processedBitmaps.size) {
                        val bmp = processedBitmaps[index]
                        
                        val pixelX = (frame.x * targetWidth).toInt()
                        val pixelY = (frame.y * totalHeight).toInt()
                        val pixelW = (frame.width * targetWidth).toInt()
                        val pixelH = (frame.height * totalHeight).toInt()
                        
                        val scaled = Bitmap.createScaledBitmap(bmp, pixelW, pixelH, true)
                        canvas.drawBitmap(scaled, pixelX.toFloat(), pixelY.toFloat(), null)
                    }
                }
                result
            }
        }
    }

    /**
     * Converts a color bitmap to a 1-bit monochrome ESC/POS raster byte array using
     * the Floyd-Steinberg error diffusion algorithm for higher quality halftone shading.
     */
    fun convertToEscPosRasterDithered(bitmap: Bitmap, contrastFactor: Float = 1.4f): ByteArray {
        val width = bitmap.width
        val height = bitmap.height

        val grayValues = FloatArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = bitmap.getPixel(x, y)
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)
                val lum = 0.299f * r + 0.587f * g + 0.114f * b
                val contrastLum = ((lum - 128f) * contrastFactor + 128f).coerceIn(0f, 255f)
                grayValues[y * width + x] = contrastLum
            }
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val oldVal = grayValues[index]
                val newVal = if (oldVal < 128f) 0f else 255f
                grayValues[index] = newVal
                val error = oldVal - newVal

                if (x + 1 < width) grayValues[index + 1] += error * 7f / 16f
                if (x - 1 >= 0 && y + 1 < height) grayValues[index + width - 1] += error * 3f / 16f
                if (y + 1 < height) grayValues[index + width] += error * 5f / 16f
                if (x + 1 < width && y + 1 < height) grayValues[index + width + 1] += error * 1f / 16f
            }
        }

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
                            currentByte = currentByte or (1 shl (7 - bit))
                        }
                    }
                }
                rasterData[byteIndex++] = currentByte.toByte()
            }
        }
        return rasterData
    }
}
