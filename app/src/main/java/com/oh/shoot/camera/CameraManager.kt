package com.oh.shoot.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(private val context: Context) {

    private var imageCapture: ImageCapture? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraManager", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun takePhoto(squareMode: Boolean = false, onPhotoCaptured: (Bitmap) -> Unit) {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    var bitmap = imageToBitmap(image)
                    image.close()
                    
                    if (bitmap != null) {
                        if (squareMode) {
                            val size = minOf(bitmap.width, bitmap.height)
                            val x = (bitmap.width - size) / 2
                            val y = (bitmap.height - size) / 2
                            bitmap = Bitmap.createBitmap(bitmap, x, y, size, size)
                        }
                        
                        ContextCompat.getMainExecutor(context).execute {
                            onPhotoCaptured(bitmap)
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraManager", "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun imageToBitmap(image: ImageProxy): Bitmap? {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        
        // Handle rotation and mirroring
        val rotation = image.imageInfo.rotationDegrees
        val matrix = Matrix().apply {
            postRotate(rotation.toFloat())
            // Front camera mirroring
            postScale(-1f, 1f)
        }
        
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
