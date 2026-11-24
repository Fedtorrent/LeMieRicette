package com.fulvio.lemiericette.presentation.recipe_form

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoLibrary
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fulvio.lemiericette.core.ui.components.AppTopBar
import com.fulvio.lemiericette.core.util.ImageUtils
import com.fulvio.lemiericette.domain.model.Category
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun RecipeFormScreen(
    recipeId: String?,
    onBackClick: () -> Unit,
    viewModel: RecipeFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showNutritionSection by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Requester per lo scorrimento automatico sopra la tastiera
    val infoCardRequester = remember { BringIntoViewRequester() }
    val timesCardRequester = remember { BringIntoViewRequester() }
    val ingredientsCardRequester = remember { BringIntoViewRequester() }
    val instructionsCardRequester = remember { BringIntoViewRequester() }
    val nutritionCardRequester = remember { BringIntoViewRequester() }
    val notesCardRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            Toast.makeText(context, "Ricetta salvata con successo!", Toast.LENGTH_SHORT).show()
            onBackClick()
        }
    }

    // Launchers per Selezione Immagine da Galleria e Fotocamera
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            val savedPath = ImageUtils.saveImageToInternalStorage(context, selectedUri)
            viewModel.onImagePathChanged(savedPath)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val savedPath = ImageUtils.saveImageToInternalStorage(context, tempCameraUri!!)
            viewModel.onImagePathChanged(savedPath)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = ImageUtils.createTempPictureUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permesso fotocamera necessario per scattare una foto", Toast.LENGTH_SHORT).show()
        }
    }

    val screenTitle = if (recipeId != null) "Modifica Ricetta" else "Nuova Ricetta"

    Scaffold(
        topBar = {
            AppTopBar(
                title = screenTitle,
                onBackClick = onBackClick,
                actions = {
                    TextButton(
                        onClick = { viewModel.saveRecipe(context) },
                        enabled = !state.isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Salva",
                            tint = if (state.title.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Salva",
                            fontWeight = FontWeight.Bold,
                            color = if (state.title.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding() // imePadding inserito prima di verticalScroll per espandere il range di scorrimento
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // CARD IMMAGINE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!state.imagePath.isNullOrBlank()) {
                            AsyncImage(
                                model = state.imagePath,
                                contentDescription = "Foto Ricetta",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nessuna foto selezionata",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fotocamera")
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") }
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Galleria")
                        }
                    }
                }
            }

            // CARD INFORMAZIONI PRINCIPALI
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(infoCardRequester),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Informazioni Principali",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Categoria Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.category.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria") },
                            trailingIcon = {
                                IconButton(onClick = { categoryDropdownExpanded = !categoryDropdownExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Category.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = {
                                        viewModel.onCategoryChanged(cat)
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Titolo
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = { viewModel.onTitleChanged(it) },
                        label = { Text("Titolo Ricetta *") },
                        isError = state.titleError != null,
                        supportingText = {
                            if (state.titleError != null) {
                                Text(state.titleError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        delay(200)
                                        infoCardRequester.bringIntoView()
                                    }
                                }
                            }
                    )

                    // Descrizione breve
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.onDescriptionChanged(it) },
                        label = { Text("Descrizione breve") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        delay(200)
                                        infoCardRequester.bringIntoView()
                                    }
                                }
                            }
                    )

                    // Link di Riferimento / Fonte (Opzionale)
                    OutlinedTextField(
                        value = state.sourceUrl,
                        onValueChange = { viewModel.onSourceUrlChanged(it) },
                        label = { Text("Link di Riferimento / Fonte (opzionale)") },
                        placeholder = { Text("https://www.sito.it/ricetta...") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        delay(200)
                                        infoCardRequester.bringIntoView()
                                    }
                                }
                            }
                    )

                    // Origine
                    OutlinedTextField(
                        value = state.origin,
                        onValueChange = { viewModel.onOriginChanged(it) },
                        label = { Text("Origine / Regione / Fonte") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        delay(200)
                                        infoCardRequester.bringIntoView()
                                    }
                                }
                            }
                    )
                }
            }

            // CARD TEMPI E PORZIONI
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(timesCardRequester),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tempi e Porzioni",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.prepTimeMin,
                            onValueChange = { viewModel.onPrepTimeChanged(it) },
                            label = { Text("Prep (min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            delay(200)
                                            timesCardRequester.bringIntoView()
                                        }
                                    }
                                }
                        )

                        OutlinedTextField(
                            value = state.cookTimeMin,
                            onValueChange = { viewModel.onCookTimeChanged(it) },
                            label = { Text("Cottura (min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            delay(200)
                                            timesCardRequester.bringIntoView()
                                        }
                                    }
                                }
                        )

                        OutlinedTextField(
                            value = state.servings,
                            onValueChange = { viewModel.onServingsChanged(it) },
                            label = { Text("Persone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            delay(200)
                                            timesCardRequester.bringIntoView()
                                        }
                                    }
                                }
                        )
                    }
                }
            }

            // CARD INGREDIENTI (TESTO MULTILINEA LIBERO)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(ingredientsCardRequester),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ingredienti",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = state.ingredientsText,
                        onValueChange = { viewModel.onIngredientsTextChanged(it) },
                        label = { Text("Ingredienti") },
                        placeholder = { Text("Scrivi gli ingredienti andando a capo (es. \n- 2 uova\n- 100g farina...)") },
                        minLines = 5,
                        maxLines = 15,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        delay(200)
                                        ingredientsCardRequester.bringIntoView()
                                    }
                                }
                            }
                    )
                }
            }

            // CARD PREPARAZIONE (TESTO MULTILINEA LIBERO)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(instructionsCardRequester),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Preparazione",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = state.instructionsText,
                        onValueChange = { viewModel.onInstructionsTextChanged(it) },
                        label = { Text("Preparazione") },
                        placeholder = { Text("Descrivi i passaggi della preparazione...") },
                        minLines = 6,
                        maxLines = 20,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        delay(200)
                                        instructionsCardRequester.bringIntoView()
                                    }
                                }
                            }
                    )
                }
            }

            // CARD VALORI NUTRIZIONALI (OPZIONALI)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(nutritionCardRequester),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Parametri Nutrizionali (per porzione)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = {
                                showNutritionSection = !showNutritionSection
                                if (showNutritionSection) {
                                    coroutineScope.launch {
                                        delay(200)
                                        nutritionCardRequester.bringIntoView()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (showNutritionSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Espandi nutrizione"
                            )
                        }
                    }

                    AnimatedVisibility(visible = showNutritionSection) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = state.nutritionCalories,
                                    onValueChange = { viewModel.onNutritionCaloriesChanged(it) },
                                    label = { Text("Calorie (kcal)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                coroutineScope.launch {
                                                    delay(200)
                                                    nutritionCardRequester.bringIntoView()
                                                }
                                            }
                                        }
                                )
                                OutlinedTextField(
                                    value = state.nutritionProtein,
                                    onValueChange = { viewModel.onNutritionProteinChanged(it) },
                                    label = { Text("Proteine (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                coroutineScope.launch {
                                                    delay(200)
                                                    nutritionCardRequester.bringIntoView()
                                                }
                                            }
                                        }
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = state.nutritionCarbs,
                                    onValueChange = { viewModel.onNutritionCarbsChanged(it) },
                                    label = { Text("Carboidrati (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                coroutineScope.launch {
                                                    delay(200)
                                                    nutritionCardRequester.bringIntoView()
                                                }
                                            }
                                        }
                                )
                                OutlinedTextField(
                                    value = state.nutritionFat,
                                    onValueChange = { viewModel.onNutritionFatChanged(it) },
                                    label = { Text("Grassi (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                coroutineScope.launch {
                                                    delay(200)
                                                    nutritionCardRequester.bringIntoView()
                                                }
                                            }
                                        }
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = state.nutritionSugar,
                                    onValueChange = { viewModel.onNutritionSugarChanged(it) },
                                    label = { Text("Zuccheri (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                coroutineScope.launch {
                                                    delay(200)
                                                    nutritionCardRequester.bringIntoView()
                                                }
                                            }
                                        }
                                )
                                OutlinedTextField(
                                    value = state.nutritionSalt,
                                    onValueChange = { viewModel.onNutritionSaltChanged(it) },
                                    label = { Text("Sale (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                coroutineScope.launch {
                                                    delay(200)
                                                    nutritionCardRequester.bringIntoView()
                                                }
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }

            // CARD NOTE
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(notesCardRequester),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Note e Consigli",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = { viewModel.onNotesChanged(it) },
                        label = { Text("Note aggiuntive") },
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        delay(200)
                                        notesCardRequester.bringIntoView()
                                    }
                                }
                            }
                    )
                }
            }

            // PULSANTE SALVA IN FONDO
            Button(
                onClick = { viewModel.saveRecipe(context) },
                enabled = !state.isSaving && state.title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (recipeId != null) "Salva Modifiche" else "Salva Ricetta",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
