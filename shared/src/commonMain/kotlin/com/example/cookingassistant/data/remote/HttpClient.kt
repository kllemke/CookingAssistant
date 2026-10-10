package com.example.cookingassistant.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

const val API_KEY = "sk_live_zxL0K87E7DnYyy2zTWyvnXN3IneknsQE32buWlUJ58a2c099"

@OptIn(ExperimentalSerializationApi::class)
fun createHttpClient() = HttpClient {
    install(DefaultRequest) {
        header(
            HttpHeaders.Authorization,
            "Bearer $API_KEY"
        )
    }

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                namingStrategy = JsonNamingStrategy.SnakeCase
            }
        )
    }

    install(Logging) {
        level = LogLevel.INFO
    }
}