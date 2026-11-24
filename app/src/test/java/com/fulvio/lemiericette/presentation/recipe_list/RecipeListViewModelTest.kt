package com.fulvio.lemiericette.presentation.recipe_list

import com.fulvio.lemiericette.domain.model.Recipe
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeListViewModelTest {

    @Test
    fun `sorting all recipes alphabetically from A to Z works correctly`() {
        val recipes = listOf(
            Recipe(title = "Zuccotto", categoryId = "DOLCI"),
            Recipe(title = "Antipasto di Mare", categoryId = "ANTIPASTI"),
            Recipe(title = "Carbonara", categoryId = "PRIMI")
        )

        val sorted = recipes.sortedBy { it.title.lowercase().trim() }

        assertEquals("Antipasto di Mare", sorted[0].title)
        assertEquals("Carbonara", sorted[1].title)
        assertEquals("Zuccotto", sorted[2].title)
    }
}
