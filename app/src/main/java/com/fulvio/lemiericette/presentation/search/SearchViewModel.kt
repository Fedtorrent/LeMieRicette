package com.fulvio.lemiericette.presentation.search

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOption(val displayName: String) {
    TITLE_ASC("Titolo (A-Z)"),
    TITLE_DESC("Titolo (Z-A)"),
    DATE_DESC("Più recenti"),
    TIME_ASC("Tempo cresc.");
}

data class SearchFilterState(
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
    val isFavoritesOnly: Boolean = false,
    val isTriedFilter: Boolean? = null, // null = Tutti, false = Da provare, true = Provate
    val maxTotalTimeMin: Int? = null,
    val sortOption: SortOption = SortOption.TITLE_ASC
) {
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() || selectedCategory != null || isFavoritesOnly || isTriedFilter != null || maxTotalTimeMin != null || sortOption != SortOption.TITLE_ASC
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialQuery: String? = savedStateHandle["query"]

    private val _searchQuery = MutableStateFlow(initialQuery ?: "")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    private val _isFavoritesOnly = MutableStateFlow(false)
    val isFavoritesOnly: StateFlow<Boolean> = _isFavoritesOnly

    private val _isTriedFilter = MutableStateFlow<Boolean?>(null)
    val isTriedFilter: StateFlow<Boolean?> = _isTriedFilter

    private val _maxTotalTimeMin = MutableStateFlow<Int?>(null)
    val maxTotalTimeMin: StateFlow<Int?> = _maxTotalTimeMin

    private val _sortOption = MutableStateFlow(SortOption.TITLE_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption

    val filterState: StateFlow<SearchFilterState> = combine(
        _searchQuery,
        _selectedCategory,
        _isFavoritesOnly,
        _isTriedFilter,
        _maxTotalTimeMin,
        _sortOption
    ) { flows ->
        SearchFilterState(
            searchQuery = flows[0] as String,
            selectedCategory = flows[1] as Category?,
            isFavoritesOnly = flows[2] as Boolean,
            isTriedFilter = flows[3] as Boolean?,
            maxTotalTimeMin = flows[4] as Int?,
            sortOption = flows[5] as SortOption
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchFilterState(searchQuery = initialQuery ?: "")
    )

    val uiState: StateFlow<UiState<List<Recipe>>> = combine(
        repository.getAllRecipes(),
        filterState
    ) { allRecipes, filters ->
        val filtered = allRecipes.filter { recipe ->
            // Filtro Ricerca globale per Titolo, Descrizione o Ingredienti/Passaggi
            val queryMatch = if (filters.searchQuery.isBlank()) {
                true
            } else {
                val q = filters.searchQuery.lowercase().trim()
                recipe.title.lowercase().contains(q) ||
                        recipe.description.lowercase().contains(q) ||
                        recipe.ingredientsText.lowercase().contains(q) ||
                        recipe.instructionsText.lowercase().contains(q)
            }

            // Filtro Categoria
            val categoryMatch = filters.selectedCategory == null || recipe.categoryId == filters.selectedCategory.id

            // Filtro Preferiti
            val favoritesMatch = !filters.isFavoritesOnly || recipe.isFavorite

            // Filtro Stato Validazione (Da provare / Provate)
            val triedMatch = when (filters.isTriedFilter) {
                null -> true
                true -> recipe.isTried
                false -> !recipe.isTried
            }

            // Filtro Tempo Max Totale
            val timeMatch = filters.maxTotalTimeMin == null || recipe.totalTimeMin <= filters.maxTotalTimeMin

            queryMatch && categoryMatch && favoritesMatch && triedMatch && timeMatch
        }

        // Ordinamento A-Z di default per tutte le liste e filtri
        val sorted = when (filters.sortOption) {
            SortOption.TITLE_ASC -> filtered.sortedBy { it.title.lowercase().trim() }
            SortOption.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase().trim() }
            SortOption.DATE_DESC -> filtered.sortedByDescending { it.updatedAt }
            SortOption.TIME_ASC -> filtered.sortedBy { it.totalTimeMin }
        }

        if (sorted.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(sorted)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    fun onQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: Category?) {
        _selectedCategory.value = category
    }

    fun onFavoritesToggle(isFavoritesOnly: Boolean) {
        _isFavoritesOnly.value = isFavoritesOnly
    }

    fun onTriedFilterSelected(isTried: Boolean?) {
        _isTriedFilter.value = isTried
    }

    fun onMaxTimeSelected(maxMinutes: Int?) {
        _maxTotalTimeMin.value = maxMinutes
    }

    fun onSortOptionSelected(sort: SortOption) {
        _sortOption.value = sort
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _isFavoritesOnly.value = false
        _isTriedFilter.value = null
        _maxTotalTimeMin.value = null
        _sortOption.value = SortOption.TITLE_ASC
    }

    fun toggleFavorite(recipeId: String, currentIsFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(recipeId, !currentIsFavorite)
        }
    }
}
