package com.fulvio.lemiericette.presentation.recipe_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fulvio.lemiericette.core.util.UiState
import com.fulvio.lemiericette.domain.model.Category
import com.fulvio.lemiericette.domain.model.Recipe
import com.fulvio.lemiericette.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeListUiData(
    val categoryName: String,
    val category: Category,
    val recipes: List<Recipe>,
    val searchQuery: String = ""
)

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val repository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val categoryId: String = checkNotNull(savedStateHandle["categoryId"])
    val isAllRecipes: Boolean = categoryId.equals("ALL", ignoreCase = true)
    val category: Category = Category.fromId(categoryId)
    val categoryDisplayName: String = if (isAllRecipes) "Tutte le Ricette" else category.displayName

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val sourceRecipesFlow = if (isAllRecipes) {
        repository.getAllRecipes()
    } else {
        repository.getRecipesByCategory(categoryId)
    }

    val uiState: StateFlow<UiState<RecipeListUiData>> = combine(
        sourceRecipesFlow,
        _searchQuery
    ) { recipes, query ->
        // Ordinamento alfabetico A-Z per la schermata "Tutte le Ricette"
        val sortedRecipes = if (isAllRecipes) {
            recipes.sortedBy { it.title.lowercase().trim() }
        } else {
            recipes
        }

        val filtered = if (query.isBlank()) {
            sortedRecipes
        } else {
            val q = query.trim().lowercase()
            sortedRecipes.filter { recipe ->
                recipe.title.lowercase().contains(q) ||
                recipe.description.lowercase().contains(q) ||
                recipe.ingredientsText.lowercase().contains(q) ||
                recipe.instructionsText.lowercase().contains(q)
            }
        }

        if (filtered.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(
                RecipeListUiData(
                    categoryName = categoryDisplayName,
                    category = category,
                    recipes = filtered,
                    searchQuery = query
                )
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(recipeId: String, currentIsFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(recipeId, !currentIsFavorite)
        }
    }
}
