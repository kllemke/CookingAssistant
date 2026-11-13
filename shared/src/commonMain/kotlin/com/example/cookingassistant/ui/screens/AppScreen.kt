package com.example.cookingassistant.ui.screens

import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Shared default: the screen never asks for immersive rendering. */
val NeverImmersive: StateFlow<Boolean> = MutableStateFlow(false)

interface AppScreen : Screen {
    val title: String? // Abstract: MUST be assigned a String or null
    val showBottomBar: Boolean // Abstract: MUST be assigned true or false

    /** True while the screen renders edge-to-edge without app chrome. */
    val immersive: StateFlow<Boolean> get() = NeverImmersive
}
