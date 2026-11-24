package com.fulvio.lemiericette.data.mapper

import com.fulvio.lemiericette.domain.model.Nutrition
import com.fulvio.lemiericette.domain.model.Recipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RecipeMapperTest {

    @Test
    fun `toEntity and toDomainModel converts correctly back and forth`() {
        val recipe = Recipe(
            id = "test-uuid-123",
            categoryId = "PRIMI",
            title = "Spaghetti alla Carbonara",
            description = "Ricetta tradizionale romana",
            imagePath = "/images/carbonara.jpg",
            prepTimeMin = 15,
            cookTimeMin = 10,
            servings = 4,
            ingredientsText = "- 400g Spaghetti\n- 200g Guanciale",
            instructionsText = "1. Rosolare il guanciale in padella.\n2. Mantecare la pasta.",
            notes = "Usare solo tuorli d'uovo freschi",
            nutrition = Nutrition(calories = 650, proteinGrams = 25.0, carbsGrams = 70.0, fatGrams = 30.0),
            origin = "Lazio, Italia",
            isFavorite = true
        )

        val entity = recipe.toEntity()
        val mappedRecipe = entity.toDomainModel()

        assertEquals(recipe.id, entity.id)
        assertEquals(recipe.title, entity.title)
        assertEquals(recipe.ingredientsText, entity.ingredientsText)
        assertEquals(recipe.instructionsText, entity.instructionsText)
        assertEquals(recipe.isFavorite, entity.isFavorite)

        assertEquals(recipe, mappedRecipe)
        assertNotNull(mappedRecipe.nutrition)
        assertEquals(25.0, mappedRecipe.nutrition?.proteinGrams)
    }
}
