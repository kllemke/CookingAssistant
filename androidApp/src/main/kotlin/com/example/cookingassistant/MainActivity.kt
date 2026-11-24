package com.example.cookingassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.cookingassistant.data.local.AndroidPersonStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val personStorage = AndroidPersonStorage(applicationContext)
        setContent {
            App(personStorage = personStorage)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}