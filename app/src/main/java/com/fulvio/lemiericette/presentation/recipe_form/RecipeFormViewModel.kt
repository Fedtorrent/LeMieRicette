package com.fulvio.lemiericette.presentation.recipe_form

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fulvio.lemiericette.core.util.ImageCleanupUtils
import com.fulvio.lemiericette.domain.model.Category
import com.fulvio.lemiericette.domain.model.Nutrition
import com.fulvio.lemiericette.domain.model.Recipe
import com.fulvio.lemiericette.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class RecipeFormState(
    val recipeId: String? = null,
    val category: Category = Category.PRIMI,
    val title: String = "",
    val description: String = "",
    val imagePath: String? = null,
    val prepTimeMin: String = "",
    val cookTimeMin: String = "",
    val servings: String = "",
    val ingredientsText: String = "",
    val instructionsText: String = "",
    val notes: String = "",
    val sourceUrl: String = "",
    val nutritionCalories: String = "",
    val nutritionProtein: String = "",
    val nutritionCarbs: String = "",
    val nutritionFat: String = "",
    val nutritionSugar: String = "",
    val nutritionSalt: String = "",
    val origin: String = "",
    val titleError: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDraftLoaded: Boolean = false
)

@HiltViewModel
class RecipeFormViewModel @Inject constructor(
    private val repository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recipeId: String? = savedStateHandle["recipeId"]

    private val _state = MutableStateFlow(RecipeFormState(recipeId = recipeId))
    val state: StateFlow<RecipeFormState> = _state.asStateFlow()

    init {
        if (!recipeId.isNullOrBlank()) {
            loadRecipe(recipeId)
        }
    }

    private fun loadRecipe(id: String) {
        viewModelScope.launch {
            val recipe = repository.getRecipeById(id).firstOrNull()
            if (recipe != null) {
                _state.update {
                    it.copy(
                        recipeId = recipe.id,
                        category = Category.fromId(recipe.categoryId),
                        title = recipe.title,
                        description = recipe.description,
                        imagePath = recipe.imagePath,
                        prepTimeMin = if (recipe.prepTimeMin > 0) recipe.prepTimeMin.toString() else "",
                        cookTimeMin = if (recipe.cookTimeMin > 0) recipe.cookTimeMin.toString() else "",
                        servings = if (recipe.servings > 0) recipe.servings.toString() else "",
                        ingredientsText = recipe.ingredientsText,
                        instructionsText = recipe.instructionsText,
                        notes = recipe.notes,
                        sourceUrl = recipe.sourceUrl ?: "",
                        nutritionCalories = recipe.nutrition?.calories?.toString() ?: "",
                        nutritionProtein = recipe.nutrition?.proteinGrams?.toString() ?: "",
                        nutritionCarbs = recipe.nutrition?.carbsGrams?.toString() ?: "",
                        nutritionFat = recipe.nutrition?.fatGrams?.toString() ?: "",
                        nutritionSugar = recipe.nutrition?.sugarGrams?.toString() ?: "",
                        nutritionSalt = recipe.nutrition?.saltGrams?.toString() ?: "",
                        origin = recipe.origin,
                        isDraftLoaded = true
                    )
                }
            }
        }
    }

    fun onTitleChanged(value: String) {
        _state.update {
            it.copy(
                title = value,
                titleError = if (value.isBlank()) "Il titolo è obbligatorio" else null
            )
        }
    }

    fun onCategoryChanged(category: Category) {
        _state.update { it.copy(category = category) }
    }

    fun onDescriptionChanged(value: String) {
        _state.update { it.copy(description = value) }
    }

    fun onImagePathChanged(path: String?) {
        _state.update { it.copy(imagePath = path) }
    }

    fun onPrepTimeChanged(value: String) {
        _state.update { it.copy(prepTimeMin = value.filter { char -> char.isDigit() }) }
    }

    fun onCookTimeChanged(value: String) {
        _state.update { it.copy(cookTimeMin = value.filter { char -> char.isDigit() }) }
    }

    fun onServingsChanged(value: String) {
        _state.update { it.copy(servings = value.filter { char -> char.isDigit() }) }
    }

    fun onIngredientsTextChanged(value: String) {
        _state.update { it.copy(ingredientsText = value) }
    }

    fun onInstructionsTextChanged(value: String) {
        _state.update { it.copy(instructionsText = value) }
    }

    fun onNotesChanged(value: String) {
        _state.update { it.copy(notes = value) }
    }

    fun onSourceUrlChanged(value: String) {
        _state.update { it.copy(sourceUrl = value) }
    }

    fun onOriginChanged(value: String) {
        _state.update { it.copy(origin = value) }
    }

    fun onNutritionCaloriesChanged(value: String) {
        _state.update { it.copy(nutritionCalories = value.filter { c -> c.isDigit() }) }
    }

    fun onNutritionProteinChanged(value: String) {
        _state.update { it.copy(nutritionProtein = value) }
    }

    fun onNutritionCarbsChanged(value: String) {
        _state.update { it.copy(nutritionCarbs = value) }
    }

    fun onNutritionFatChanged(value: String) {
        _state.update { it.copy(nutritionFat = value) }
    }

    fun onNutritionSugarChanged(value: String) {
        _state.update { it.copy(nutritionSugar = value) }
    }

    fun onNutritionSaltChanged(value: String) {
        _state.update { it.copy(nutritionSalt = value) }
    }

    fun saveRecipe(context: Context) {
        val currentState = _state.value
        if (currentState.title.isBlank()) {
            _state.update { it.copy(titleError = "Il titolo è obbligatorio") }
            return
        }

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val oldImagePath = currentState.imagePath

            val hasNutrition = currentState.nutritionCalories.isNotBlank() ||
                    currentState.nutritionProtein.isNotBlank() ||
                    currentState.nutritionCarbs.isNotBlank() ||
                    currentState.nutritionFat.isNotBlank() ||
                    currentState.nutritionSugar.isNotBlank() ||
                    currentState.nutritionSalt.isNotBlank()

            val mappedNutrition = if (hasNutrition) {
                Nutrition(
                    calories = currentState.nutritionCalories.toIntOrNull(),
                    proteinGrams = currentState.nutritionProtein.toDoubleOrNull(),
                    carbsGrams = currentState.nutritionCarbs.toDoubleOrNull(),
                    fatGrams = currentState.nutritionFat.toDoubleOrNull(),
                    sugarGrams = currentState.nutritionSugar.toDoubleOrNull(),
                    saltGrams = currentState.nutritionSalt.toDoubleOrNull()
                )
            } else null

            val recipeToSave = Recipe(
                id = currentState.recipeId ?: UUID.randomUUID().toString(),
                categoryId = currentState.category.id,
                title = currentState.title.trim(),
                description = currentState.description.trim(),
                imagePath = currentState.imagePath,
                prepTimeMin = currentState.prepTimeMin.toIntOrNull() ?: 0,
                cookTimeMin = currentState.cookTimeMin.toIntOrNull() ?: 0,
                servings = currentState.servings.toIntOrNull() ?: 0,
                ingredientsText = currentState.ingredientsText.trim(),
                instructionsText = currentState.instructionsText.trim(),
                notes = currentState.notes.trim(),
                sourceUrl = currentState.sourceUrl.trim().ifBlank { null },
                nutrition = mappedNutrition,
                origin = currentState.origin.trim(),
                updatedAt = System.currentTimeMillis()
            )

            repository.insertOrUpdateRecipe(recipeToSave)

            // Se l'immagine è stata modificata o sostituita, cancella la vecchia se non utilizzata
            if (oldImagePath != null && oldImagePath != recipeToSave.imagePath) {
                val activeRecipes = repository.getAllRecipes().first()
                ImageCleanupUtils.deleteLocalImageFile(context, oldImagePath, activeRecipes)
            }

            _state.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}
