package com.example.cookingassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import androidx.compose.foundation.layout.navigationBarsPadding
import com.example.cookingassistant.ui.screens.ScreenConfig
import com.example.cookingassistant.ui.screens.ScreenLayout
import com.example.cookingassistant.ui.theme.Dimens

@Composable
fun ScreenBlueprint(
    config: ScreenConfig,
    onSettingsClick: () -> Unit = {},
    navigator: Navigator? = null,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val edgeToEdge = config.layout == ScreenLayout.EdgeToEdge

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = if (edgeToEdge) 0.dp else Dimens.screenPadding),
            contentWindowInsets = if (edgeToEdge) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
            topBar = {
                if (!edgeToEdge && config.title != null) {
                    AppHeader(
                        title = config.title,
                        showSettingsButton = config.showSettingsButton,
                        onSettingsClick = onSettingsClick,
                        modifier = Modifier
                            .statusBarsPadding()
                    )
                }
            },
            bottomBar = {
                if (!edgeToEdge && config.showBottomBar && navigator != null) {
                    AppNavigationBar(
                        navigator = navigator,
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            }
        ) { innerPadding ->
            // innerPadding automatically accounts for topBar and bottomBar!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                content(innerPadding)
            }
        }
    }
}
