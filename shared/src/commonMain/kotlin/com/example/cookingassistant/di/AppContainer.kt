package com.example.cookingassistant.di

import com.example.cookingassistant.data.remote.api.RecipeApi
import com.example.cookingassistant.data.remote.createHttpClient
import com.example.cookingassistant.data.repository.RecipeRepository
import com.example.cookingassistant.data.repository.RecipeRepositoryImpl
import com.example.cookingassistant.ui.screenmodel.RecipeScreenModel

class AppContainer (
    repository: RecipeRepository? = null
) {
    private val httpClient = createHttpClient()
    private val recipeApi = RecipeApi(httpClient)
    private val recipeRepository = repository ?: RecipeRepositoryImpl(recipeApi)
    val recipeScreenModel = RecipeScreenModel(recipeRepository)
}