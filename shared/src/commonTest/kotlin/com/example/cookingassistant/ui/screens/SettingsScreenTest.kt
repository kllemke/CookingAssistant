package com.example.cookingassistant.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.cookingassistant.data.local.PersonStorage
import com.example.cookingassistant.di.AppContainer
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.screenmodel.Person
import com.example.cookingassistant.ui.theme.CookingAssistantTheme
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

/** Test double for [PersonStorage] that starts with [initial] and keeps the last saved list. */
internal class FakePersonStorage(initial: List<Person> = emptyList()) : PersonStorage {
    var stored: List<Person> = initial
        private set

    override suspend fun load(): List<Person> = stored

    override suspend fun save(persons: List<Person>) {
        stored = persons
    }
}

@OptIn(ExperimentalTestApi::class)
class SettingsScreenTest : BaseComposeTest() {

    private fun ComposeUiTest.showSettingsScreen(persons: List<Person> = emptyList()): AppContainer {
        val container = AppContainer(mock(), FakePersonStorage(persons))
        setContent {
            CookingAssistantTheme {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    SettingsScreen().Content()
                }
            }
        }
        return container
    }

    private fun persons(container: AppContainer) =
        container.settingsScreenModel.persons.value

    @Test
    fun addPersonViaUi() = runComposeUiTest {
        val container = showSettingsScreen()

        onNodeWithText("Add person").performClick()
        onNode(hasSetTextAction()).performTextInput("Anna")
        onNodeWithContentDescription("Save name").performClick()

        onNodeWithText("Anna").assertExists()
        onNodeWithText("No intolerances").assertExists()
        onNodeWithText("Intolerances · Anna").assertExists()
        assertEquals(listOf(Person("Anna")), persons(container))
    }

    @Test
    fun duplicateNameShowsErrorAndDisablesSave() = runComposeUiTest {
        showSettingsScreen(listOf(Person("Anna")))

        onNodeWithText("Add person").performClick()
        onNode(hasSetTextAction()).performTextInput("anna")

        onNodeWithText("This name already exists.").assertExists()
        onNodeWithContentDescription("Save name").assertIsNotEnabled()
    }

    @Test
    fun emptyNameShowsError() = runComposeUiTest {
        showSettingsScreen()

        onNodeWithText("Add person").performClick()

        onNodeWithText("the name cannot be empty.").assertExists()
        onNodeWithContentDescription("Save name").assertIsNotEnabled()
    }

    @Test
    fun cancelAddingPerson() = runComposeUiTest {
        val container = showSettingsScreen()

        onNodeWithText("Add person").performClick()
        onNodeWithContentDescription("Cancel").performClick()

        onNodeWithText("Add person").assertExists()
        assertEquals(emptyList(), persons(container))
    }

    @Test
    fun selectingPersonShowsIntolerancesAndTogglingUpdatesSummary() = runComposeUiTest {
        val container = showSettingsScreen(listOf(Person("Anna")))

        onNodeWithText("Intolerances · Anna").assertDoesNotExist()
        onNodeWithText("Anna").performClick()
        onNodeWithText("Intolerances · Anna").assertExists()

        val vegetarianOption = hasText("Vegetarian") and isToggleable()
        onNode(vegetarianOption).assertIsOff().performClick()
        onNode(vegetarianOption).assertIsOn()

        onNodeWithText("No intolerances").assertDoesNotExist()
        assertEquals(listOf("Vegetarian"), persons(container).single().intolerances)
    }

    @Test
    fun renamePersonViaUi() = runComposeUiTest {
        val container = showSettingsScreen(listOf(Person("Anna", intolerances = listOf("Soy"))))

        onNodeWithContentDescription("Rename Anna").performClick()
        onNode(hasSetTextAction()).performTextReplacement("Anne")
        onNodeWithContentDescription("Save name").performClick()

        onNodeWithText("Anne").assertExists()
        onNodeWithText("Intolerances · Anne").assertExists()
        assertEquals(listOf(Person("Anne", intolerances = listOf("Soy"))), persons(container))
    }

    @Test
    fun toggleIncludedViaCheckbox() = runComposeUiTest {
        val container = showSettingsScreen(listOf(Person("Anna")))

        onNode(isToggleable()).assertIsOn().performClick()

        waitForIdle()
        assertEquals(false, persons(container).single().included)
    }

    @Test
    fun removePersonAndUndo() = runComposeUiTest {
        val container = showSettingsScreen(listOf(Person("Anna"), Person("Ben")))

        onNodeWithContentDescription("Rename Anna").performClick()
        onNodeWithContentDescription("Remove Anna").performClick()

        onNodeWithText("Anna removed").assertExists()
        assertEquals(listOf("Ben"), persons(container).map { it.name })

        onNodeWithText("Undo").performClick()

        waitUntil { persons(container).map { it.name } == listOf("Anna", "Ben") }
        onNodeWithText("Anna").assertExists()
    }
}
