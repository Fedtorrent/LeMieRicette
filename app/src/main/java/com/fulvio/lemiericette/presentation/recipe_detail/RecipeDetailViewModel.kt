package com.fulvio.lemiericette.presentation.recipe_detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fulvio.lemiericette.core.util.ImageCleanupUtils
import com.fulvio.lemiericette.core.util.UiState
import com.fulvio.lemiericette.domain.model.Recipe
import com.fulvio.lemiericette.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val repository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val recipeId: String = checkNotNull(savedStateHandle["recipeId"])

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    val uiState: StateFlow<UiState<Recipe>> = repository
        .getRecipeById(recipeId)
        .map { recipe ->
            if (recipe != null) {
                UiState.Success(recipe)
            } else {
                if (_isDeleted.value) {
                    UiState.Idle
                } else {
                    UiState.Error("Ricetta non trovata")
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun toggleFavorite() {
        val currentRecipe = (uiState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.toggleFavorite(recipeId, !currentRecipe.isFavorite)
        }
    }

    fun toggleTried() {
        val currentRecipe = (uiState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.toggleTried(recipeId, !currentRecipe.isTried)
        }
    }

    fun deleteRecipe(context: Context) {
        val currentRecipe = (uiState.value as? UiState.Success)?.data
        viewModelScope.launch {
            _isDeleted.value = true
            val activeRecipes = repository.getAllRecipes().first()
            repository.deleteRecipeById(recipeId)
            if (currentRecipe?.imagePath != null) {
                ImageCleanupUtils.deleteLocalImageFile(context, currentRecipe.imagePath, activeRecipes)
            }
        }
    }
}
