package com.fulvio.lemiericette.data.repository

import com.fulvio.lemiericette.data.local.dao.RecipeDao
import com.fulvio.lemiericette.data.mapper.toDomainModel
import com.fulvio.lemiericette.data.mapper.toDomainModelList
import com.fulvio.lemiericette.data.mapper.toEntity
import com.fulvio.lemiericette.di.IoDispatcher
import com.fulvio.lemiericette.domain.model.Recipe
import com.fulvio.lemiericette.domain.repository.RecipeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementazione concreta di RecipeRepository che interagisce con il DAO di Room.
 */
@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RecipeRepository {

    override fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes().map { entities -> entities.toDomainModelList() }
    }

    override fun getRecipesByCategory(categoryId: String): Flow<List<Recipe>> {
        return recipeDao.getRecipesByCategory(categoryId).map { entities -> entities.toDomainModelList() }
    }

    override fun getRecipeById(id: String): Flow<Recipe?> {
        return recipeDao.getRecipeById(id).map { entity -> entity?.toDomainModel() }
    }

    override fun searchRecipes(query: String): Flow<List<Recipe>> {
        return recipeDao.searchRecipes(query).map { entities -> entities.toDomainModelList() }
    }

    override fun getFavoriteRecipes(): Flow<List<Recipe>> {
        return recipeDao.getFavoriteRecipes().map { entities -> entities.toDomainModelList() }
    }

    override fun getUntriedRecipes(): Flow<List<Recipe>> {
        return recipeDao.getUntriedRecipes().map { entities -> entities.toDomainModelList() }
    }

    override fun getTriedRecipes(): Flow<List<Recipe>> {
        return recipeDao.getTriedRecipes().map { entities -> entities.toDomainModelList() }
    }

    override suspend fun insertOrUpdateRecipe(recipe: Recipe) {
        withContext(ioDispatcher) {
            recipeDao.insertOrUpdate(recipe.toEntity())
        }
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        withContext(ioDispatcher) {
            recipeDao.delete(recipe.toEntity())
        }
    }

    override suspend fun deleteRecipeById(id: String) {
        withContext(ioDispatcher) {
            recipeDao.deleteById(id)
        }
    }

    override suspend fun deleteAllRecipes() {
        withContext(ioDispatcher) {
            recipeDao.deleteAllRecipes()
        }
    }

    override suspend fun toggleFavorite(recipeId: String, isFavorite: Boolean) {
        withContext(ioDispatcher) {
            recipeDao.updateFavorite(
                id = recipeId,
                isFavorite = isFavorite,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun toggleTried(recipeId: String, isTried: Boolean) {
        withContext(ioDispatcher) {
            recipeDao.updateTried(
                id = recipeId,
                tried = isTried,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun resetAllTriedStatus() {
        withContext(ioDispatcher) {
            recipeDao.resetAllTriedStatus(updatedAt = System.currentTimeMillis())
        }
    }

    override fun getCategoryRecipeCounts(): Flow<Map<String, Int>> {
        return recipeDao.getCategoryRecipeCounts().map { counts ->
            counts.associate { it.categoryId to it.count }
        }
    }
}
