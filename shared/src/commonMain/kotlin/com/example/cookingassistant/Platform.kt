package com.example.cookingassistant

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform