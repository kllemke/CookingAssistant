package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.components.HouseholdGroup
import com.example.cookingassistant.ui.components.IntolerancesList
import com.example.cookingassistant.ui.theme.Dimens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SettingsScreen : AppScreen {
    override val config = MutableStateFlow(ScreenConfig(title = "Settings"))

    @Composable
    override fun Content() {
        val appContainer = LocalAppContainer.current
        val screenModel = appContainer.settingsScreenModel
        val persons by screenModel.persons.collectAsState()
        var selectedName by remember { mutableStateOf<String?>(null) }
        val selectedPerson = persons.find { it.name == selectedName }
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()


        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge),
                modifier = Modifier.fillMaxSize(),
            ) {
                HouseholdGroup(
                    persons = persons,
                    selected = selectedPerson,
                    onSelectPerson = { selectedName = it.name },
                    onToggleIncluded = { screenModel.togglePersonIncluded(it) },
                    onAddPerson = { name ->
                        screenModel.addPerson(name)
                        selectedName = name
                    },
                    onRenamePerson = { person, newName ->
                        screenModel.renamePerson(person.name, newName)
                        selectedName = newName
                    },
                    onRemovePerson = { person ->
                        val index = persons.indexOf(person)
                        screenModel.removePerson(person.name)
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val result = snackbarHostState.showSnackbar(
                                message = "${person.name} removed",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short,
                            )
                            if (result == SnackbarResult.ActionPerformed &&
                                screenModel.getNameError(person.name) == null
                            ) {
                                screenModel.restorePerson(person, index)
                                selectedName = person.name
                            }
                        }
                    },
                    getNameError = screenModel::getNameError,
                )
                if (selectedPerson != null) {
                    IntolerancesList(
                        selectedPerson = selectedPerson,
                        onToggle = { screenModel.toggleIntoleranceForPerson(selectedPerson.name, it) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
