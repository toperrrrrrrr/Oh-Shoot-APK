package com.ohshootstudio.resibooth.camera

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
    private var cameraProvider: ProcessCameraProvider? = null

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int = 0,
        onCameraFallback: () -> Unit = {}
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            this.cameraProvider = cameraProvider

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val targetSelector = when (lensFacing) {
                1 -> CameraSelector.DEFAULT_BACK_CAMERA
                2 -> CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_EXTERNAL).build()
                else -> CameraSelector.DEFAULT_FRONT_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    targetSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraManager", "Use case binding failed for lensFacing $lensFacing", exc)
                if (lensFacing != 0) { // Fallback to front
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview,
                            imageCapture
                        )
                        ContextCompat.getMainExecutor(context).execute {
                            android.widget.Toast.makeText(context, "External camera unavailable. Falling back to front.", android.widget.Toast.LENGTH_LONG).show()
                            onCameraFallback()
                        }
                    } catch (e2: Exception) {
                        Log.e("CameraManager", "Fallback binding failed", e2)
                    }
                }
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
                            val cropped = Bitmap.createBitmap(bitmap, x, y, size, size)
                            if (cropped != bitmap) {
                                bitmap.recycle()
                            }
                            bitmap = cropped
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

    fun unbindAll() {
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e("CameraManager", "Failed to unbind camera provider", e)
        }
    }
}

