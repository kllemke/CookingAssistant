package com.example.cookingassistant.ui.screens

import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.flow.StateFlow

enum class ScreenLayout {
    /** Standard app chrome: header, bottom bar, screen padding and window insets. */
    Standard,

    /** Full-bleed canvas: no app chrome, no screen padding, no window insets. */
    EdgeToEdge,
}

data class ScreenConfig(
    val title: String? = null,
    val layout: ScreenLayout = ScreenLayout.Standard,
    val showBottomBar: Boolean = true,
    val showSettingsButton: Boolean = false,
) {
    val isEdgeToEdge: Boolean get() = layout == ScreenLayout.EdgeToEdge

    companion object {
        /** Preset for full edge-to-edge camera / image preview screens. */
        val Immersive = ScreenConfig(layout = ScreenLayout.EdgeToEdge)
    }
}

interface AppScreen : Screen {
    /** The flow may be mutated while the screen is visible,
     *  e.g. the camera going edge-to-edge once */
    /** permission is granted. */
    val config: StateFlow<ScreenConfig>
}
