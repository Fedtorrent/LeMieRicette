package com.fulvio.lemiericette.data.rtk

import com.fulvio.lemiericette.data.local.rtk.RtkCategoryDto
import com.fulvio.lemiericette.data.local.rtk.RtkRecipesContainerDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RtkImporterTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `deserializes RecetteTek categories json correctly`() {
        val categoriesJson = """[{"title":"Antipasti","uuid":"62e6aaba-522e-44f9-a93e-1b630019b3f5"},{"title":"Piatti","uuid":"fa22a288-b4de-4b1d-a900-8fb294bc6b7d"}]"""
        val dtos = json.decodeFromString<List<RtkCategoryDto>>(categoriesJson)

        assertEquals(2, dtos.size)
        assertEquals("Antipasti", dtos[0].categoryTitle)
        assertEquals("62e6aaba-522e-44f9-a93e-1b630019b3f5", dtos[0].categoryId)
    }

    @Test
    fun `deserializes RecetteTek recipes_0 json container correctly`() {
        val recipesJson = """
            {
              "value": [
                {
                  "title": "Lasagne alla bolognese",
                  "description": "Le lasagne alla bolognese sono le classiche lasagne al forno.",
                  "preparationTime": "120",
                  "cookingTime": "180",
                  "quantity": "8",
                  "ingredients": "Semola 175 g\nFarina 75 g",
                  "instructions": "Per preparare le lasagne...\n\nTritate la pancetta.",
                  "pictures": ["/data/user/0/fr.recettetek/files/Pictures/4f937104.png"],
                  "url": "https://ricette.giallozafferano.it/Lasagne-alla-Bolognese.html",
                  "uuid": "0cdf8f46-a7c4-484a-9a8c-a595c4f3fbe1"
                }
              ]
            }
        """.trimIndent()

        val container = json.decodeFromString<RtkRecipesContainerDto>(recipesJson)
        val dto = container.value.firstOrNull()

        assertNotNull(dto)
        assertEquals("Lasagne alla bolognese", dto?.title)
        assertEquals("120", dto?.preparationTime)
        assertEquals("180", dto?.cookingTime)
        assertEquals("https://ricette.giallozafferano.it/Lasagne-alla-Bolognese.html", dto?.url)
    }
}
