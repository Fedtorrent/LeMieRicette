package com.fulvio.lemiericette.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fulvio.lemiericette.data.local.entity.RecipeEntity
import com.fulvio.lemiericette.data.local.model.CategoryCount
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) per le operazioni CRUD sulla tabella delle ricette.
 */
@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes ORDER BY title ASC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE categoryId = :categoryId ORDER BY title ASC")
    fun getRecipesByCategory(categoryId: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeById(id: String): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR ingredientsText LIKE '%' || :query || '%' OR instructionsText LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchRecipes(query: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isTried = 0 ORDER BY title ASC")
    fun getUntriedRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isTried = 1 ORDER BY title ASC")
    fun getTriedRecipes(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(recipe: RecipeEntity): Long

    @Delete
    suspend fun delete(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes()

    @Query("UPDATE recipes SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean, updatedAt: Long)

    @Query("UPDATE recipes SET isTried = :tried, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTried(id: String, tried: Boolean, updatedAt: Long)

    @Query("UPDATE recipes SET isTried = 0, updatedAt = :updatedAt")
    suspend fun resetAllTriedStatus(updatedAt: Long)

    @Query("SELECT categoryId, COUNT(*) as count FROM recipes GROUP BY categoryId")
    fun getCategoryRecipeCounts(): Flow<List<CategoryCount>>
}
