package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.cookingassistant.ui.components.HomeScreenButton
import cookingassistant.shared.generated.resources.Res
import cookingassistant.shared.generated.resources.add_photo
import cookingassistant.shared.generated.resources.add_text

class HomeScreen: Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Zutaten hinzufügen",
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                HomeScreenButton(
                    text = "manuell eingeben",
                    icon = Res.drawable.add_text,
                    onClick = {
                        navigator.push(IngredientsScreen())
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f).testTag("to_enter_manually")
                )
                Spacer(modifier = Modifier.height(10.dp))
                HomeScreenButton(
                    text = "Foto aufnehmen",
                    icon = Res.drawable.add_photo,
                    onClick = {
                        navigator.push(CameraScreen())
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f).testTag("to_take_picture")
                )
            }
        }
    }
}