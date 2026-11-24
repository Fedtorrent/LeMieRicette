package com.fulvio.lemiericette.presentation.recipe_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fulvio.lemiericette.core.ui.components.AppTopBar
import com.fulvio.lemiericette.core.ui.components.EmptyScreen
import com.fulvio.lemiericette.core.ui.components.ErrorScreen
import com.fulvio.lemiericette.core.ui.components.LoadingScreen
import com.fulvio.lemiericette.core.util.UiState
import com.fulvio.lemiericette.presentation.category.components.getCategoryIcon
import com.fulvio.lemiericette.presentation.recipe_list.components.RecipeCard

@Composable
fun RecipeListScreen(
    onRecipeClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = viewModel.categoryDisplayName,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // BARRA DI RICERCA CATEGORIALE
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Cerca in ${viewModel.categoryDisplayName}...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cerca"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancella"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            when (val state = uiState) {
                is UiState.Loading -> {
                    LoadingScreen(modifier = Modifier.fillMaxSize())
                }
                is UiState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is UiState.Empty -> {
                    EmptyScreen(
                        message = if (searchQuery.isNotBlank()) {
                            "Nessuna ricetta trovata per \"$searchQuery\" in ${viewModel.categoryDisplayName}"
                        } else if (viewModel.isAllRecipes) {
                            "Nessuna ricetta presente nel ricettario"
                        } else {
                            "Nessuna ricetta in ${viewModel.category.displayName}"
                        },
                        icon = if (viewModel.isAllRecipes) Icons.Default.RestaurantMenu else getCategoryIcon(viewModel.category),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = state.data.recipes,
                            key = { it.id }
                        ) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe.id) },
                                onFavoriteToggle = {
                                    viewModel.toggleFavorite(recipe.id, recipe.isFavorite)
                                }
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
