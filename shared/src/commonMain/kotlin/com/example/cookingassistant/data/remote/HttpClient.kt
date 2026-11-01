package com.example.cookingassistant.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

const val API_KEY = "6600a45668404f3cb4eceff6b794ac6a"

@OptIn(ExperimentalSerializationApi::class)
fun createHttpClient() = HttpClient {
    install(DefaultRequest) {
        header(
            "x-api-key",
            API_KEY
        )
    }

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }

    install(Logging) {
        level = LogLevel.INFO
    }
}