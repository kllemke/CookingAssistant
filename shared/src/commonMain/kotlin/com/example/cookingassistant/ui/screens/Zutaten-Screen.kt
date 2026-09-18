package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
import cookingassistant.shared.generated.resources.Res
import cookingassistant.shared.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
private val GreenColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32), // Dunkelgrün
    onPrimary = Color.White,
    secondary = Color(0xFF4CAF50), // Mittelgrün
    onSecondary = Color.White,
    surface = Color(0xFFF1F8E9), // Sehr helles Grün für den Hintergrund
    onSurface = Color(0xFF1B5E20)
)

@Composable
fun MaterialScreen(
    initialText: String = "",
    painter: Painter? = null,
    onTextChange: (String) -> Unit = {}
) {
    var textState by remember { mutableStateOf(initialText) }

    MaterialTheme(colorScheme = GreenColorScheme) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Zutaten bearbeiten",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (painter != null) {
                Image(
                    painter = painter,
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
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9F).testTag("zutaten_input"),
                singleLine = false,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Hier die Weiterleitung zur Rezeptliste hinzufügen.
                    println("Gespeichert: $textState")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Speichern")
            }
        }
    }
}

@Preview
@Composable
fun PreviewMaterialScreen() {
    MaterialScreen(
        initialText = "Tomaten \n Gurken, Salat",
        painter = painterResource(Res.drawable.compose_multiplatform)
    )
}
