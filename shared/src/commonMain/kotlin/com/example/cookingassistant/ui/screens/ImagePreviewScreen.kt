package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.example.cookingassistant.di.LocalAppContainer

// Pass imagePath in the constructor
data class ImagePreviewScreen(val imagePath: String) : AppScreen {
    override val showBottomBar: Boolean = false
    override val title: String? = null
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val appContainer = LocalAppContainer.current
        val screenModel = appContainer.imagePreviewScreenModel

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Load and display image asynchronously
            AsyncImage(
                model = imagePath,
                contentDescription = "Captured Photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Retake / Back Button
                Button(onClick = {
                    navigator.pop()
                    screenModel.removeDiscardedImage(imagePath)
                }) {
                    Text("Retake")
                }

                // Confirm Button
                Button(onClick = {
                    // TODO: save `imagePath` to the local directory if needed
                }) {
                    Text("Confirm")
                }
            }
        }
    }
}