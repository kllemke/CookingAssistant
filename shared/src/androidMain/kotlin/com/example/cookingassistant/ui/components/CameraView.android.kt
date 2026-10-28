package com.example.cookingassistant.ui.components

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
actual fun CameraView(
    modifier: Modifier
) {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Camera use case
    val previewView = remember { PreviewView(context) }

    // Track if camera initialization failed
    var hasCameraError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit){
        val cameraProvider = cameraProviderFuture.get()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        try {
            // rebind the camera on composable start
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifeCycleOwner,
                cameraSelector,
                preview,
            )
        } catch (e: Exception) {
            // No need for capturing specific exceptions here
            // catching as such prevents the app from crashing
            Log.e("CameraView", "Failed to bind CameraX: ${e.message}")
            hasCameraError = true
        }
    }

    // Clean up camera resources when leaving composition
    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraProviderFuture.get()?.unbindAll()
            } catch (e: Exception) {
                Log.w(
                    "CameraView", "Failed to unbind CameraX on dispose: ${e.message}"
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        if (hasCameraError) {
            Box(contentAlignment = Alignment.Center){
                Text("Unable to open camera preview.")
            }
        } else {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

}
