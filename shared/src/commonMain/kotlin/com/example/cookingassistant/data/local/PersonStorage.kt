package com.example.cookingassistant.data.local

import com.example.cookingassistant.ui.screenmodel.Person

interface PersonStorage {
    suspend fun load(): List<Person>
    suspend fun save(persons: List<Person>)
}

class InMemoryPersonStorage : PersonStorage {
    private var persons: List<Person> = emptyList()

    override suspend fun load(): List<Person> = persons

    override suspend fun save(persons: List<Person>) {
        this.persons = persons
    }
}
