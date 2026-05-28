package com.oh.shoot.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

object ImageUtils {

    fun decodeBitmapFromUri(context: Context, uriString: String, maxWidth: Int): Bitmap? {
        return runCatching {
            val uri = Uri.parse(uriString)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }
            val sampleSize = calculateInSampleSize(bounds, maxWidth)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                BitmapFactory.decodeStream(stream, null, options)
            }
        }.getOrNull()
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqWidth || width > reqWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqWidth && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    fun resizeToWidth(bitmap: Bitmap, targetWidth: Int): Bitmap {
        if (bitmap.width <= targetWidth) return bitmap
        val targetHeight = (targetWidth.toDouble() / bitmap.width * bitmap.height).toInt()
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    /**
     * Saves a bitmap to the system gallery (Pictures folder).
     */
    fun saveToGallery(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val resolver = context.contentResolver
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val imageUri = resolver.insert(imageCollection, contentValues) ?: return null

        resolver.openOutputStream(imageUri).use { outputStream ->
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)
        }

        return imageUri
    }
}
