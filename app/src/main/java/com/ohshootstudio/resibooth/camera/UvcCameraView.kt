package com.ohshootstudio.resibooth.camera

import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UvcCameraView(
    modifier: Modifier = Modifier,
    onTextureViewCreated: (TextureView) -> Unit = {},
    onFallback: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var triggerFallback by remember { mutableStateOf(false) }

    LaunchedEffect(triggerFallback) {
        if (triggerFallback) {
            onFallback()
        }
    }

    val textureView = remember { 
        TextureView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            addOnLayoutChangeListener { v, left, top, right, bottom, _, _, _, _ ->
                val viewWidth = right - left
                val viewHeight = bottom - top
                if (viewWidth > 0 && viewHeight > 0) {
                    val videoWidth = 1920f
                    val videoHeight = 1080f
                    
                    val matrix = android.graphics.Matrix()
                    val viewRect = android.graphics.RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
                    val bufferRect = android.graphics.RectF(0f, 0f, videoWidth, videoHeight)
                    
                    val centerX = viewRect.centerX()
                    val centerY = viewRect.centerY()
                    
                    bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
                    matrix.setRectToRect(viewRect, bufferRect, android.graphics.Matrix.ScaleToFit.FILL)
                    
                    val scale = maxOf(viewWidth / videoWidth, viewHeight / videoHeight)
                    matrix.postScale(scale, scale, centerX, centerY)
                    
                    (v as TextureView).setTransform(matrix)
                }
            }
        }
    }
    
    LaunchedEffect(textureView) {
        onTextureViewCreated(textureView)
    }
    
    DisposableEffect(Unit) {
        var uvcCamera: UVCCamera? = null
        var surface: Surface? = null
        
        val usbMonitor = USBMonitor(context, object : USBMonitor.OnDeviceConnectListener {
            
            override fun onAttach(device: UsbDevice) {
                // USB device plugged in during session
            }
            
            override fun onDettach(device: UsbDevice) {
                // USB device unplugged
                coroutineScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "External camera disconnected", Toast.LENGTH_SHORT).show()
                    triggerFallback = true
                }
            }
            
            override fun onConnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock, createNew: Boolean) {
                try {
                    uvcCamera = UVCCamera()
                    uvcCamera?.open(ctrlBlock)
                    
                    // Native driver attempts to grab 1080p frame size
                    try {
                        uvcCamera?.setPreviewSize(1920, 1080, UVCCamera.FRAME_FORMAT_MJPEG)
                    } catch (e: Exception) {
                        uvcCamera?.setPreviewSize(1280, 720, UVCCamera.FRAME_FORMAT_MJPEG)
                    }
                    
                    val st = textureView.surfaceTexture
                    if (st != null) {
                        val s = Surface(st)
                        surface = s
                        uvcCamera?.setPreviewDisplay(s)
                        uvcCamera?.startPreview()
                    } else {
                        // TextureView isn't ready yet, defer
                        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                                val s = Surface(st)
                                surface = s
                                uvcCamera?.setPreviewDisplay(s)
                                uvcCamera?.startPreview()
                            }
                            override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {}
                            override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean = true
                            override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                        }
                    }
                } catch (e: Exception) {
                    coroutineScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to initialize external camera", Toast.LENGTH_SHORT).show()
                        triggerFallback = true
                    }
                }
            }
            
            override fun onDisconnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock) {
                uvcCamera?.stopPreview()
                uvcCamera?.close()
                uvcCamera = null
                surface?.release()
                surface = null
            }
            
            override fun onCancel(device: UsbDevice) {
                coroutineScope.launch(Dispatchers.Main) {
                    triggerFallback = true
                }
            }
        })
        
        // Scan for attached USB Video Class devices
        val deviceList = usbMonitor.deviceList
        if (deviceList.isNotEmpty()) {
            // Request Android permission dialog for the capture card
            usbMonitor.requestPermission(deviceList.first())
        } else {
            // No USB camera found, fallback immediately
            coroutineScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "No external camera detected. Check OTG cable.", Toast.LENGTH_SHORT).show()
                triggerFallback = true
            }
        }
        
        onDispose {
            uvcCamera?.stopPreview()
            uvcCamera?.close()
            usbMonitor.destroy()
        }
    }
    
    AndroidView(
        factory = { textureView },
        modifier = modifier
    )
}

