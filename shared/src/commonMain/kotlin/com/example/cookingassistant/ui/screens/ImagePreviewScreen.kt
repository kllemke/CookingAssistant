package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.components.PrimaryButton
import com.example.cookingassistant.ui.components.SecondaryButton
import com.example.cookingassistant.ui.theme.Dimens
import kotlinx.coroutines.flow.MutableStateFlow

// Pass imagePath in the constructor
data class ImagePreviewScreen(val imagePath: String) : AppScreen {
    override val config = MutableStateFlow(ScreenConfig(title = "Check photo"))
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val appContainer = LocalAppContainer.current
        val screenModel = appContainer.imagePreviewScreenModel

        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = Dimens.spacingSmall),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
        ) {
            // Load and display image asynchronously in a clipped & bordered Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(width = Dimens.borderWidth, color = MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.small)
                    .clip(MaterialTheme.shapes.small)
            ) {
                AsyncImage(
                    model = imagePath,
                    contentDescription = "Captured Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("captured_photo"),
                    contentScale = ContentScale.FillBounds
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
            ) {
                // Retake / Back Button
                SecondaryButton(
                    text = "Retake",
                    onClick = {
                        navigator.pop()
                        screenModel.removeDiscardedImage(imagePath)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "image_preview_retake",
                )

                // Confirm Button
                PrimaryButton(
                    text = "Confirm",
                    onClick = {
                        // TODO: save `imagePath` to the local directory if needed
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "image_preview_confirm",
                )
            }
        }
    }
}