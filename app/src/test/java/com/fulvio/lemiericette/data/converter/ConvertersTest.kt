package com.fulvio.lemiericette.data.converter

import com.fulvio.lemiericette.data.local.converter.Converters
import com.fulvio.lemiericette.domain.model.Ingredient
import com.fulvio.lemiericette.domain.model.Nutrition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `ingredient list conversion serializes and deserializes correctly`() {
        val ingredients = listOf(
            Ingredient(amount = 500.0, unit = "g", name = "Farina 00"),
            Ingredient(amount = 250.0, unit = "ml", name = "Acqua")
        )

        val jsonString = converters.fromIngredientList(ingredients)
        val resultList = converters.toIngredientList(jsonString)

        assertEquals(2, resultList.size)
        assertEquals("Farina 00", resultList[0].name)
        assertEquals(500.0, resultList[0].amount)
        assertEquals("g", resultList[0].unit)
    }

    @Test
    fun `step list conversion handles empty and invalid string gracefully`() {
        val emptyJson = converters.toStepList("")
        val invalidJson = converters.toStepList("invalid_json_str")

        assertEquals(0, emptyJson.size)
        assertEquals(0, invalidJson.size)
    }

    @Test
    fun `nutrition object conversion serializes and deserializes null and values`() {
        val nutrition = Nutrition(calories = 450, carbsGrams = 60.0)

        val jsonString = converters.fromNutrition(nutrition)
        val resultNutrition = converters.toNutrition(jsonString)

        assertEquals(450, resultNutrition?.calories)
        assertEquals(60.0, resultNutrition?.carbsGrams)
        assertNull(converters.toNutrition(null))
    }
}
