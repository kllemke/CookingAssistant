package com.example.cookingassistant.ui.components

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.takePicture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import androidx.concurrent.futures.await
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File

@Composable
actual fun CameraView(
    modifier: Modifier
) {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Camera use cases
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Track if camera initialization failed
    var hasCameraError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit){
        val cameraProvider = cameraProviderFuture.await()
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
                imageCapture,
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
        // Alternative UI for when camera hardware fails
        if (hasCameraError) {
            Box(contentAlignment = Alignment.Center){
                Text("Unable to open camera preview.")
            }
        } else {
            // Camera Preview
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize(),
            )
            // Trigger button
            IconButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .size(80.dp)
                    .border(2.dp, Color.White, CircleShape),
                onClick = {
                    // Take a photo
                    lifeCycleOwner.lifecycleScope.launch {
                        takePhoto(context, imageCapture, onImageCaptured = { uri ->
                            Log.d("CameraView", "Photo captured at $uri")
                        })
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "take photo",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}
// Taken from Looksy
private suspend fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit
) {
    // ✅ ÄNDERUNG: Speichere im Cache-Verzeichnis statt im externen Medienverzeichnis.
    // Das ist sicherer und erfordert keine speziellen Berechtigungen.
    val photoFile = File(
        context.cacheDir,
        "temp_${System.currentTimeMillis()}.jpg"
    )

    try {
        // Nutze die eingebaute suspend-Funktion von CameraX
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputOptions) // Diese Funktion wartet, bis das Bild gespeichert ist

        // Wenn die obige Zeile ohne Fehler durchläuft, ist das Bild gespeichert.
        val savedUri = Uri.fromFile(photoFile)
        onImageCaptured(savedUri)

    } catch (exc: ImageCaptureException) {
        Log.e("Camera", "Fotoaufnahme fehlgeschlagen: ${exc.message}", exc)
        // Hier könntest du dem Nutzer eine Fehlermeldung anzeigen.
        // Wichtig: Führe UI-Änderungen im Main-Thread aus
        // withContext(Dispatchers.Main) {
        //     Toast.makeText(context, "Fehler: ${exc.message}", Toast.LENGTH_SHORT).show()
        // }
    }
}
