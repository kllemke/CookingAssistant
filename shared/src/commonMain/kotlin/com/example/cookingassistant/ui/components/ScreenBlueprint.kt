package com.example.cookingassistant.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import androidx.compose.foundation.layout.navigationBarsPadding
import com.example.cookingassistant.ui.theme.Dimens


@Composable
fun ScreenBlueprint(
    title: String? = null,
    showBottomBar: Boolean = true,
    navigator: Navigator? = null,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    // TODO: how to regulate border in bottom bar, top bar other optional composables?
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.screenPadding),
        topBar = {
            if (title != null) {
                AppHeader(
                    title = title,
                    modifier = Modifier
                        .statusBarsPadding()
                )
            }
        },
        bottomBar = {
            if (showBottomBar && navigator != null) {
                AppNavigationBar(
                    navigator = navigator,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) { innerPadding ->
        // TODO: how to set something like justify content space between with vertical alignment and flex-direction column?
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
