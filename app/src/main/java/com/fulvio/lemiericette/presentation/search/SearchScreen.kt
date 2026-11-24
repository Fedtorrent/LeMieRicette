package com.fulvio.lemiericette.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fulvio.lemiericette.core.ui.components.EmptyScreen
import com.fulvio.lemiericette.core.ui.components.ErrorScreen
import com.fulvio.lemiericette.core.ui.components.LoadingScreen
import com.fulvio.lemiericette.core.util.UiState
import com.fulvio.lemiericette.domain.model.Category
import com.fulvio.lemiericette.presentation.recipe_list.components.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onRecipeClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var timeDropdownExpanded by remember { mutableStateOf(false) }
    var sortDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = filterState.searchQuery,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        placeholder = { Text("Cerca titolo o ingrediente...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (filterState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.onQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Cancella")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            keyboardController?.hide()
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // BARRA DEI FILTRI ORIZZONTALE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // FILTRO PREFERITI
                FilterChip(
                    selected = filterState.isFavoritesOnly,
                    onClick = { viewModel.onFavoritesToggle(!filterState.isFavoritesOnly) },
                    label = { Text("Preferiti") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (filterState.isFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (filterState.isFavoritesOnly) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )

                // FILTRO DA PROVARE (isTried == false)
                FilterChip(
                    selected = filterState.isTriedFilter == false,
                    onClick = {
                        if (filterState.isTriedFilter == false) {
                            viewModel.onTriedFilterSelected(null)
                        } else {
                            viewModel.onTriedFilterSelected(false)
                        }
                    },
                    label = { Text("Da provare") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (filterState.isTriedFilter == false) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )

                // FILTRO PROVATE (isTried == true)
                FilterChip(
                    selected = filterState.isTriedFilter == true,
                    onClick = {
                        if (filterState.isTriedFilter == true) {
                            viewModel.onTriedFilterSelected(null)
                        } else {
                            viewModel.onTriedFilterSelected(true)
                        }
                    },
                    label = { Text("Provate") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (filterState.isTriedFilter == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )

                // FILTRO CATEGORIA DROPDOWN
                Box {
                    FilterChip(
                        selected = filterState.selectedCategory != null,
                        onClick = { categoryDropdownExpanded = true },
                        label = {
                            Text(filterState.selectedCategory?.displayName ?: "Categoria")
                        },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tutte le categorie", fontWeight = FontWeight.Bold) },
                            onClick = {
                                viewModel.onCategorySelected(null)
                                categoryDropdownExpanded = false
                            }
                        )
                        Category.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    viewModel.onCategorySelected(cat)
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // FILTRO TEMPO MAX DROPDOWN
                Box {
                    FilterChip(
                        selected = filterState.maxTotalTimeMin != null,
                        onClick = { timeDropdownExpanded = true },
                        label = {
                            Text(
                                if (filterState.maxTotalTimeMin != null) "<= ${filterState.maxTotalTimeMin} min" else "Tempo"
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    )
                    DropdownMenu(
                        expanded = timeDropdownExpanded,
                        onDismissRequest = { timeDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Qualsiasi durata", fontWeight = FontWeight.Bold) },
                            onClick = {
                                viewModel.onMaxTimeSelected(null)
                                timeDropdownExpanded = false
                            }
                        )
                        listOf(15, 30, 45, 60).forEach { mins ->
                            DropdownMenuItem(
                                text = { Text("Fino a $mins min") },
                                onClick = {
                                    viewModel.onMaxTimeSelected(mins)
                                    timeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // FILTRO ORDINAMENTO DROPDOWN
                Box {
                    FilterChip(
                        selected = filterState.sortOption != SortOption.DATE_DESC,
                        onClick = { sortDropdownExpanded = true },
                        label = { Text(filterState.sortOption.displayName) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    )
                    DropdownMenu(
                        expanded = sortDropdownExpanded,
                        onDismissRequest = { sortDropdownExpanded = false }
                    ) {
                        SortOption.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.displayName) },
                                onClick = {
                                    viewModel.onSortOptionSelected(sort)
                                    sortDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // PULSANTE RESETTA FILTRI
                AnimatedVisibility(visible = filterState.hasActiveFilters) {
                    IconButton(onClick = { viewModel.resetFilters() }) {
                        Icon(
                            imageVector = Icons.Default.FilterListOff,
                            contentDescription = "Resetta filtri",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // RISULTATI DI RICERCA
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        LoadingScreen()
                    }
                    is UiState.Error -> {
                        ErrorScreen(message = state.message)
                    }
                    is UiState.Empty -> {
                        EmptyScreen(
                            message = if (filterState.hasActiveFilters) {
                                "Nessuna ricetta trovata per i filtri selezionati"
                            } else {
                                "Nessuna ricetta presente"
                            }
                        )
                    }
                    is UiState.Success -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = state.data,
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
}
