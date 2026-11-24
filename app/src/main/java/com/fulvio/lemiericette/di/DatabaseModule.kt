package com.fulvio.lemiericette.di

import android.content.Context
import androidx.room.Room
import com.fulvio.lemiericette.data.local.dao.RecipeDao
import com.fulvio.lemiericette.data.local.database.AppDatabase
import com.fulvio.lemiericette.data.repository.RecipeRepositoryImpl
import com.fulvio.lemiericette.domain.repository.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(
        impl: RecipeRepositoryImpl
    ): RecipeRepository

    companion object {

        @Provides
        @Singleton
        fun provideAppDatabase(
            @ApplicationContext context: Context
        ): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "lemiericette_database"
            )
            .fallbackToDestructiveMigration()
            .build()
        }

        @Provides
        @Singleton
        fun provideRecipeDao(
            database: AppDatabase
        ): RecipeDao {
            return database.recipeDao()
        }
    }
}
