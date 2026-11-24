package com.fulvio.lemiericette.presentation.category.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.TakeoutDining
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fulvio.lemiericette.domain.model.Category

@Composable
fun CategoryCard(
    category: Category,
    recipeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = getCategoryIcon(category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(98.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = category.displayName,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (recipeCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = recipeCount.toString(),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

fun getCategoryIcon(category: Category): ImageVector {
    return when (category) {
        Category.ANTIPASTI -> Icons.Default.TakeoutDining
        Category.PRIMI -> Icons.Default.RiceBowl
        Category.SECONDI -> Icons.Default.DinnerDining
        Category.CONTORNI -> Icons.Default.SoupKitchen
        Category.PIATTI_UNICI -> Icons.Default.LunchDining
        Category.DOLCI -> Icons.Default.Cake
        Category.BEVANDE -> Icons.Default.LocalBar
        Category.SALSE_E_CONDIMENTI -> Icons.Default.Restaurant
        Category.PANE_E_LIEVITATI -> Icons.Default.BakeryDining
        Category.ALTRO -> Icons.Default.Category
    }
}
