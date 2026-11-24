package com.fulvio.lemiericette.domain.repository

import com.fulvio.lemiericette.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

/**
 * Interfaccia Repository del layer Domain per la gestione delle ricette.
 */
interface RecipeRepository {
    fun getAllRecipes(): Flow<List<Recipe>>
    fun getRecipesByCategory(categoryId: String): Flow<List<Recipe>>
    fun getRecipeById(id: String): Flow<Recipe?>
    fun searchRecipes(query: String): Flow<List<Recipe>>
    fun getFavoriteRecipes(): Flow<List<Recipe>>
    fun getUntriedRecipes(): Flow<List<Recipe>>
    fun getTriedRecipes(): Flow<List<Recipe>>
    suspend fun insertOrUpdateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(recipe: Recipe)
    suspend fun deleteRecipeById(id: String)
    suspend fun deleteAllRecipes()
    suspend fun toggleFavorite(recipeId: String, isFavorite: Boolean)
    suspend fun toggleTried(recipeId: String, isTried: Boolean)
    suspend fun resetAllTriedStatus()
    fun getCategoryRecipeCounts(): Flow<Map<String, Int>>
}
