package com.example.cookingassistant.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.cookingassistant.data.local.InMemoryPersonStorage
import com.example.cookingassistant.data.local.PersonStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val name: String,
    val included: Boolean = true,
    val intolerances: List<String> = emptyList(),
)

class SettingsScreenModel(
    private val storage: PersonStorage = InMemoryPersonStorage(),
) : ScreenModel {
    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    val persons: StateFlow<List<Person>> = _persons.asStateFlow()

    init {
        screenModelScope.launch {
            _persons.update { storage.load() }
            _persons.collect { storage.save(it) } // Persists every change
        }
    }

    fun getNameError(name: String, currentName: String? = null): String? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return "the name cannot be empty."
        val taken = _persons.value.any {
            it.name != currentName && it.name.equals(trimmed, ignoreCase = true)
        }
        if (taken) return "This name already exists."
        return null
    }

    fun addPerson(name: String) {
        _persons.update { it + Person(name.trim()) }
    }

    fun renamePerson(currentName: String, newName: String) {
        updatePerson(currentName) { it.copy(name = newName.trim()) }
    }

    fun removePerson(name: String) {
        _persons.update { list -> list.filterNot { it.name == name } }
    }

    fun restorePerson(person: Person, index: Int) {
        _persons.update { list ->
            list.toMutableList().apply { add(index.coerceIn(0, size), person) }
        }
    }

    fun toggleIntoleranceForPerson(name: String, intolerance: String) {
        updatePerson(name) {
            it.copy(
                intolerances = if (intolerance in it.intolerances) {
                    it.intolerances - intolerance
                } else {
                    it.intolerances + intolerance
                }
            )
        }
    }

    fun togglePersonIncluded(person: Person) {
        updatePerson(person.name) { it.copy(included = !it.included) }
    }

    private fun updatePerson(name: String, transform: (Person) -> Person) {
        _persons.update { list -> list.map { if (it.name == name) transform(it) else it } }
    }
}
