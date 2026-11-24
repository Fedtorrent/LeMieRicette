package com.fulvio.lemiericette.presentation.web_import.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.fulvio.lemiericette.domain.model.Category
import com.fulvio.lemiericette.domain.model.Recipe
import com.fulvio.lemiericette.domain.model.ScrapedRecipe
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPreviewDialog(
    scrapedRecipe: ScrapedRecipe,
    onDismiss: () -> Unit,
    onSaveRecipe: (Recipe) -> Unit
) {
    var title by remember { mutableStateOf(scrapedRecipe.title) }
    var description by remember { mutableStateOf(scrapedRecipe.description) }
    var category by remember { mutableStateOf<Category?>(scrapedRecipe.categoryId?.let { Category.fromId(it) }) }
    var prepTimeMin by remember { mutableStateOf(if (scrapedRecipe.prepTimeMin > 0) scrapedRecipe.prepTimeMin.toString() else "") }
    var cookTimeMin by remember { mutableStateOf(if (scrapedRecipe.cookTimeMin > 0) scrapedRecipe.cookTimeMin.toString() else "") }
    var servings by remember { mutableStateOf(if (scrapedRecipe.servings > 0) scrapedRecipe.servings.toString() else "") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                // HEADER DIALOG
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Anteprima Importazione",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // IMMAGINE ESTRATTA
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!scrapedRecipe.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = scrapedRecipe.imageUrl,
                                contentDescription = title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    // CAMPI EDITABILI CON PLACEHOLDER SE NON ESTRATTI
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titolo Ricetta *") },
                        placeholder = { Text("Inserisci il titolo della ricetta...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // CATEGORIA DROPDOWN (NESSUNA CATEGORIA PRE-EVIDENZIATA SE NON TROVATA)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category?.displayName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria *") },
                            placeholder = { Text("Seleziona categoria...") },
                            trailingIcon = {
                                IconButton(onClick = { categoryDropdownExpanded = !categoryDropdownExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryDropdownExpanded = true }
                        )
                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Category.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = {
                                        category = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrizione") },
                        placeholder = { Text("Descrizione o dettagli della ricetta...") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = prepTimeMin,
                            onValueChange = { prepTimeMin = it.filter { c -> c.isDigit() } },
                            label = { Text("Prep (min)") },
                            placeholder = { Text("Es. 15") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = cookTimeMin,
                            onValueChange = { cookTimeMin = it.filter { c -> c.isDigit() } },
                            label = { Text("Cottura (min)") },
                            placeholder = { Text("Es. 20") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = servings,
                            onValueChange = { servings = it.filter { c -> c.isDigit() } },
                            label = { Text("Persone") },
                            placeholder = { Text("Es. 4") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // RESOCONTO INGREDIENTI
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Ingredienti Estratti",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (scrapedRecipe.ingredientsText.isBlank()) {
                                Text(
                                    text = "Nessun ingrediente estratto dalla pagina.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            } else {
                                Text(
                                    text = scrapedRecipe.ingredientsText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // RESOCONTO PASSAGGI
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Preparazione Estratta",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (scrapedRecipe.instructionsText.isBlank()) {
                                Text(
                                    text = "Nessun passaggio estratto dalla pagina.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            } else {
                                Text(
                                    text = scrapedRecipe.instructionsText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // BOTTOM ACTION BUTTONS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Annulla")
                    }

                    Button(
                        onClick = {
                            val selectedCategory = category ?: Category.ALTRO
                            val recipeToSave = Recipe(
                                id = UUID.randomUUID().toString(),
                                categoryId = selectedCategory.id,
                                title = title.ifBlank { "Ricetta Importata" },
                                description = description,
                                imagePath = scrapedRecipe.imageUrl,
                                prepTimeMin = prepTimeMin.toIntOrNull() ?: 0,
                                cookTimeMin = cookTimeMin.toIntOrNull() ?: 0,
                                servings = servings.toIntOrNull() ?: 0,
                                ingredientsText = scrapedRecipe.ingredientsText,
                                instructionsText = scrapedRecipe.instructionsText,
                                nutrition = scrapedRecipe.nutrition,
                                sourceUrl = scrapedRecipe.sourceUrl
                            )
                            onSaveRecipe(recipeToSave)
                        },
                        enabled = title.isNotBlank() && category != null,
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salva Ricetta")
                    }
                }
            }
        }
    }
}
