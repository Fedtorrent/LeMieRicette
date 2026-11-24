package com.fulvio.lemiericette.presentation.recipe_detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fulvio.lemiericette.core.ui.components.AppTopBar
import com.fulvio.lemiericette.core.ui.components.ErrorScreen
import com.fulvio.lemiericette.core.ui.components.LoadingScreen
import com.fulvio.lemiericette.core.util.UiState
import com.fulvio.lemiericette.domain.model.Category
import com.fulvio.lemiericette.domain.model.Recipe

@Composable
fun RecipeDetailScreen(
    onEditClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDeleted by viewModel.isDeleted.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isDeleted) {
        if (isDeleted) {
            Toast.makeText(context, "Ricetta eliminata", Toast.LENGTH_SHORT).show()
            onBackClick()
        }
    }

    val recipe = (uiState as? UiState.Success)?.data

    Scaffold(
        topBar = {
            AppTopBar(
                title = recipe?.title ?: "Dettaglio Ricetta",
                onBackClick = onBackClick,
                actions = {
                    if (recipe != null) {
                        IconButton(
                            onClick = {
                                viewModel.toggleTried()
                                val msg = if (recipe.isTried) "Ricetta segnata come da provare" else "Ricetta segnata come provata! 🍳"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = if (recipe.isTried) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                contentDescription = if (recipe.isTried) "Segna come da provare" else "Segna come provata",
                                tint = if (recipe.isTried) Color(0xFF4CAF50) else Color(0xFFFFB74D)
                            )
                        }
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Preferito",
                                tint = if (recipe.isFavorite) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = { onEditClick(recipe.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifica"
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Elimina",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                LoadingScreen(modifier = Modifier.padding(innerPadding))
            }
            is UiState.Error -> {
                ErrorScreen(
                    message = state.message,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Success -> {
                RecipeDetailContent(
                    recipe = state.data,
                    onToggleTried = {
                        viewModel.toggleTried()
                        val msg = if (state.data.isTried) "Ricetta segnata come da provare" else "Ricetta segnata come provata! 🍳"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            else -> {}
        }
    }

    // Dialog di Conferma Eliminazione
    if (showDeleteDialog && recipe != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminare la ricetta?") },
            text = { Text("Sei sicuro di voler eliminare definitivamente \"${recipe.title}\"? L'operazione non potrà essere annullata.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRecipe(context)
                    }
                ) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    onToggleTried: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = Category.fromId(recipe.categoryId)
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // HEADER IMMAGINE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            if (!recipe.imagePath.isNullOrBlank()) {
                AsyncImage(
                    model = recipe.imagePath,
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // PULSANTE AZIONE STATO VALIDAZIONE (PROVATA / DA PROVARE)
            OutlinedButton(
                onClick = onToggleTried,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (recipe.isTried) Color(0xFF2E7D32) else Color(0xFFE65100)
                )
            ) {
                Icon(
                    imageVector = if (recipe.isTried) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (recipe.isTried) Color(0xFF2E7D32) else Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (recipe.isTried) "Provata in Cucina (Tocca per cambiare)" else "Da Provare in Cucina (Tocca per segnare come provata)",
                    fontWeight = FontWeight.Bold
                )
            }

            // TITOLO E CATEGORIA
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text(category.displayName) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.RestaurantMenu,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )

                        if (recipe.origin.isNotBlank()) {
                            AssistChip(
                                onClick = {},
                                label = { Text(recipe.origin) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.headlineMedium.copy(lineHeight = 32.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    if (recipe.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = recipe.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // STATISTICHE RAPIDE (Prep, Cottura, Totale, Porzioni)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    icon = Icons.Default.AccessTime,
                    label = "Prep",
                    value = "${recipe.prepTimeMin} min",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.Timer,
                    label = "Cottura",
                    value = "${recipe.cookTimeMin} min",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.Info,
                    label = "Totale",
                    value = "${recipe.totalTimeMin} min",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.People,
                    label = "Porzioni",
                    value = "${recipe.servings}",
                    modifier = Modifier.weight(1f)
                )
            }

            // INGREDIENTI
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ingredienti",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (recipe.ingredientsText.isBlank()) {
                        Text(
                            text = "Nessun ingrediente specificato",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = recipe.ingredientsText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // PASSAGGI PREPARAZIONE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preparazione",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (recipe.instructionsText.isBlank()) {
                        Text(
                            text = "Nessun passaggio di preparazione specificato",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = recipe.instructionsText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // VALORI NUTRIZIONALI (Se presenti)
            recipe.nutrition?.let { nutrition ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Valori Nutrizionali (per porzione)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            nutrition.calories?.let {
                                NutritionBadge(label = "Calorie", value = "$it kcal")
                            }
                            nutrition.proteinGrams?.let {
                                NutritionBadge(label = "Proteine", value = "${it}g")
                            }
                            nutrition.carbsGrams?.let {
                                NutritionBadge(label = "Carboidrati", value = "${it}g")
                            }
                            nutrition.fatGrams?.let {
                                NutritionBadge(label = "Grassi", value = "${it}g")
                            }
                        }
                    }
                }
            }

            // NOTE (Se presenti)
            if (recipe.notes.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Note e Consigli",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = recipe.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // FONTE ORIGINALE WEB (Se presente)
            if (!recipe.sourceUrl.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fonte della Ricetta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = recipe.sourceUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(recipe.sourceUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Impossibile aprire il link", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Apri nel Browser di Sistema")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NutritionBadge(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
