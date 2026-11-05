package com.example.cookingassistant.ui.screens

import cafe.adriel.voyager.core.screen.Screen

interface AppScreen : Screen {
    val title: String? // Abstract: MUST be assigned a String or null
    val showBottomBar: Boolean // Abstract: MUST be assigned true or false
}