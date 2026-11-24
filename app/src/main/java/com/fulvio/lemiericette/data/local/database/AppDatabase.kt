package com.fulvio.lemiericette.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fulvio.lemiericette.data.local.converter.Converters
import com.fulvio.lemiericette.data.local.dao.RecipeDao
import com.fulvio.lemiericette.data.local.entity.RecipeEntity

/**
 * Database Room principale dell'applicazione per la persistenza delle ricette.
 */
@Database(
    entities = [RecipeEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}
