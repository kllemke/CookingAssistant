package com.example.cookingassistant.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import cafe.adriel.voyager.core.screen.Screen

class CameraScreen: Screen {
    @Composable
    override fun Content() {
        //TODO: add Screen to take picture with the camera
        Text("Foto Aufnehmen", modifier = Modifier.testTag("take_picture"))
    }
}