package com.fulvio.lemiericette.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fulvio.lemiericette.core.util.UiState
import com.fulvio.lemiericette.data.local.preferences.UserPreferencesRepository
import com.fulvio.lemiericette.data.local.sample.SampleData
import com.fulvio.lemiericette.domain.model.Category
import com.fulvio.lemiericette.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryItemUiState(
    val category: Category,
    val recipeCount: Int
)

data class CategoriesUiData(
    val categoryItems: List<CategoryItemUiState>,
    val totalRecipesCount: Int
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    init {
        populateSampleDataIfEmpty()
    }

    val uiState: StateFlow<UiState<CategoriesUiData>> = combine(
        repository.getAllRecipes(),
        repository.getCategoryRecipeCounts()
    ) { allRecipes, countsMap ->
        val items = Category.entries.map { category ->
            CategoryItemUiState(
                category = category,
                recipeCount = countsMap[category.id] ?: 0
            )
        }
        UiState.Success(
            CategoriesUiData(
                categoryItems = items,
                totalRecipesCount = allRecipes.size
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    private fun populateSampleDataIfEmpty() {
        viewModelScope.launch {
            val hasPopulated = userPreferencesRepository.hasPopulatedSampleData.first()
            if (!hasPopulated) {
                val recipes = repository.getAllRecipes().first()
                if (recipes.isEmpty()) {
                    SampleData.sampleRecipes.forEach { sampleRecipe ->
                        repository.insertOrUpdateRecipe(sampleRecipe)
                    }
                }
                userPreferencesRepository.setHasPopulatedSampleData(true)
            }
        }
    }
}
