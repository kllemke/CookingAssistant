package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.cookingassistant.ui.components.HomeScreenButton
import com.example.cookingassistant.ui.theme.Dimens
import cookingassistant.shared.generated.resources.Res
import cookingassistant.shared.generated.resources.add_photo
import cookingassistant.shared.generated.resources.add_text

class HomeScreen: AppScreen {
    override val title: String = "Zutaten hinzufügen"
    override val showBottomBar: Boolean = true

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Dimens.tileGapVertical)
        ) {
            HomeScreenButton(
                text = "manuell eingeben",
                icon = Res.drawable.add_text,
                onClick = {
                    navigator.push(IngredientsScreen())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("to_enter_manually")
            )
            HomeScreenButton(
                text = "Foto aufnehmen",
                icon = Res.drawable.add_photo,
                onClick = {
                    navigator.push(CameraScreen())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("to_take_picture")
            )
        }
    }
}
