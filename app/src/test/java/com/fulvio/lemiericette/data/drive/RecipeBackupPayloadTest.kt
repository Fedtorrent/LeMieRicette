package com.fulvio.lemiericette.data.drive

import com.fulvio.lemiericette.data.remote.drive.RecipeBackupItem
import com.fulvio.lemiericette.data.remote.drive.RecipeBackupPayload
import com.fulvio.lemiericette.data.remote.drive.toBackupItem
import com.fulvio.lemiericette.data.remote.drive.toDomainModel
import com.fulvio.lemiericette.domain.model.Recipe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeBackupPayloadTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `v2 payload serialization and deserialization works correctly`() {
        val recipe = Recipe(
            id = "test-123",
            categoryId = "PRIMI",
            title = "Carbonara",
            ingredientsText = "- 400g Spaghetti\n- 200g Guanciale",
            instructionsText = "1. Rosolare il guanciale.\n2. Mantecare con uova."
        )

        val payload = RecipeBackupPayload(
            version = 2,
            recipes = listOf(recipe.toBackupItem())
        )

        val jsonStr = json.encodeToString(payload)
        val decodedPayload = json.decodeFromString<RecipeBackupPayload>(jsonStr)
        val restoredRecipe = decodedPayload.recipes.first().toDomainModel()

        assertEquals(recipe.title, restoredRecipe.title)
        assertEquals(recipe.ingredientsText, restoredRecipe.ingredientsText)
        assertEquals(recipe.instructionsText, restoredRecipe.instructionsText)
    }

    @Test
    fun `legacy v1 payload with JSON arrays is retrocompatible with plain text`() {
        val legacyV1Json = """
            {
              "version": 1,
              "exportedAt": 1700000000000,
              "recipes": [
                {
                  "id": "legacy-1",
                  "categoryId": "PRIMI",
                  "title": "Pasta al Pomodoro",
                  "ingredients": [
                    {"name": "Spaghetti", "amount": 350.0, "unit": "g"},
                    {"name": "Passata di pomodoro", "amount": 400.0, "unit": "g"}
                  ],
                  "steps": [
                    {"order": 1, "description": "Soffriggere aglio e olio."},
                    {"order": 2, "description": "Aggiungere il pomodoro e cuocere."}
                  ]
                }
              ]
            }
        """.trimIndent()

        val decodedPayload = json.decodeFromString<RecipeBackupPayload>(legacyV1Json)
        val restoredRecipe = decodedPayload.recipes.first().toDomainModel()

        assertEquals("Pasta al Pomodoro", restoredRecipe.title)
        assertTrue(restoredRecipe.ingredientsText.contains("350.0 g Spaghetti"))
        assertTrue(restoredRecipe.ingredientsText.contains("400.0 g Passata di pomodoro"))
        assertTrue(restoredRecipe.instructionsText.contains("Soffriggere aglio e olio."))
        assertTrue(restoredRecipe.instructionsText.contains("Aggiungere il pomodoro e cuocere."))
    }
}
