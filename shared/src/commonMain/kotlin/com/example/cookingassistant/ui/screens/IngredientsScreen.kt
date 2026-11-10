package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.components.PrimaryButton
import com.example.cookingassistant.ui.screenmodel.RecipeUiState
import com.example.cookingassistant.ui.theme.Dimens
import cookingassistant.shared.generated.resources.Res
import cookingassistant.shared.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource


data class IngredientsScreen(
    var initialText: String = "",
    val painter: Painter? = null,
    val onTextChange: (String) -> Unit = {}
): AppScreen  {
    override val title: String = "Zutaten bearbeiten"
    override val showBottomBar: Boolean = true
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
                .testTag("enter_ingredients"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            painter?.let {
                Image(
                    painter = it,
                    contentDescription = "Bild der Zutat",
                    modifier = Modifier
                        .size(Dimens.imagePlaceholder)
                        .padding(bottom = Dimens.spacingMedium)
                )
            }

            OutlinedTextField(
                value = textState,
                onValueChange = {
                    textState = it
                    onTextChange(it)
                },
                label = { Text("Zutaten eingeben") },
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("zutaten_input"),
                singleLine = false,
            )

            Spacer(modifier = Modifier.height(Dimens.spacingLarge))

            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingLarge, Alignment.CenterHorizontally)
            ) {
                when (val state = recipeUiState) {
                    RecipeUiState.Loading -> {
                        CircularProgressIndicator()
                        Text("Rezepte werden abgerufen")
                    }

                    is RecipeUiState.Error -> {
                        Text(
                            text = state.message.ifBlank { "Rezepte konnten nicht geladen werden" },
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    else -> {}
                }
            }
            PrimaryButton(
                text = "Speichern",
                onClick = {
                    val ingredients = textState.split("\n")
                    screenModel.resetRecipes()
                    screenModel.loadRecipes(ingredients)
                    requestSent = true
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "save_button",
            )
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