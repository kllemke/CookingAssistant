package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.screenmodel.RecipeUiState
import cookingassistant.shared.generated.resources.Res
import cookingassistant.shared.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource


data class IngredientsScreen(var initialText: String = "", val painter: Painter? = null, val onTextChange: (String) -> Unit = {}): Screen  {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val appContainer = LocalAppContainer.current

        val screenModel = appContainer.recipeScreenModel

        val recipeUiState by screenModel.recipeUiState.collectAsState()

        var requestSent by remember { mutableStateOf(false) }
        var textState by remember { mutableStateOf(initialText) }

        LaunchedEffect(recipeUiState) {
            val state = recipeUiState

            if (requestSent && state is RecipeUiState.Success) {
                navigator.push(RecipeScreen())
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("enter_ingredients"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Zutaten bearbeiten",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            painter?.let {
                Image(
                    painter = it,
                    contentDescription = "Bild der Zutat",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = textState,
                onValueChange = {
                    textState = it
                    onTextChange(it)
                },
                label = { Text("Zutaten eingeben") },
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8F).testTag("zutaten_input"),
                singleLine = false,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
            ) {
                when (val state = recipeUiState) {
                    RecipeUiState.Loading -> {
                        CircularProgressIndicator()
                        Text("Rezepte werden abgerufen")
                    }

                    is RecipeUiState.Error -> {
                        Text(
                            text = state.message,
                            color = Color.Red
                        )
                    }

                    else -> {}
                }
            }

            Button(
                onClick = {
                    val ingredients = textState.split("\n")
                    screenModel.resetRecipes()
                    screenModel.loadRecipes(ingredients)
                    requestSent = true
                },
                modifier = Modifier.fillMaxWidth().testTag("save_button")
            ) {
                Text(text = "Speichern")
            }
        }
    }
}

@Preview
@Composable
fun PreviewIngredientsScreen(){
    IngredientsScreen(
        initialText = "Tomaten \n Gurken, Salat",
        painter = painterResource(Res.drawable.compose_multiplatform)
    ).Content()
}