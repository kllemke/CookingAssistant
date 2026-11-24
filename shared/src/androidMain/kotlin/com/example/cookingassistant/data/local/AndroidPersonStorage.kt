package com.example.cookingassistant.data.local

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import com.example.cookingassistant.ui.screenmodel.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

private val json = Json { ignoreUnknownKeys = true }

private object PersonsSerializer : Serializer<List<Person>> {
    override val defaultValue: List<Person> = emptyList()

    override suspend fun readFrom(input: InputStream): List<Person> =
        try {
            json.decodeFromString(input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("Could not read stored persons", e)
        }

    override suspend fun writeTo(t: List<Person>, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(json.encodeToString(t).encodeToByteArray())
        }
    }
}

private val Context.personsDataStore: DataStore<List<Person>> by dataStore(
    fileName = "persons.json",
    serializer = PersonsSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { emptyList() },
)

class AndroidPersonStorage(context: Context) : PersonStorage {
    private val dataStore = context.applicationContext.personsDataStore

    override suspend fun load(): List<Person> = dataStore.data.first()

    override suspend fun save(persons: List<Person>) {
        dataStore.updateData { persons }
    }
}
