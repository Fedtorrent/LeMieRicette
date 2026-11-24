package com.fulvio.lemiericette.data.scraper

import com.fulvio.lemiericette.data.remote.scraper.RecipeScraper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeScraperTest {

    @Test
    fun `parseHtmlToRecipe extracts JsonLd recipe correctly`() {
        val jsonLdHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <script type="application/ld+json">
                {
                    "@context": "https://schema.org",
                    "@type": "Recipe",
                    "name": "Pizza Margherita Fatta in Casa",
                    "description": "La classica pizza napoletana con pomodoro e mozzarella.",
                    "prepTime": "PT30M",
                    "cookTime": "PT15M",
                    "recipeYield": "4 porzioni",
                    "recipeCategory": "Pane e Lievitati",
                    "recipeIngredient": [
                        "500g di farina tipo 0",
                        "300ml di acqua tiepida",
                        "7g di lievito di birra secco",
                        "250g di mozzarella di bufala"
                    ],
                    "recipeInstructions": [
                        {"@type": "HowToStep", "text": "Impastare la farina con acqua e lievito."},
                        {"@type": "HowToStep", "text": "Lasciar lievitare per 6 ore."},
                        {"@type": "HowToStep", "text": "Stendere la pasta e cuocere in forno al massimo."}
                    ]
                }
                </script>
            </head>
            <body></body>
            </html>
        """.trimIndent()

        val scraped = RecipeScraper.parseHtmlToRecipe(jsonLdHtml, "https://example.com/pizza")

        assertEquals("Pizza Margherita Fatta in Casa", scraped.title)
        assertEquals("La classica pizza napoletana con pomodoro e mozzarella.", scraped.description)
        assertEquals(30, scraped.prepTimeMin)
        assertEquals(15, scraped.cookTimeMin)
        assertEquals(4, scraped.servings)
        assertTrue(scraped.ingredientsText.contains("500g di farina tipo 0"))
        assertTrue(scraped.instructionsText.contains("Impastare la farina con acqua e lievito."))
    }

    @Test
    fun `parseHtmlToRecipe fallback to OpenGraph meta tags when JsonLd is missing`() {
        val ogHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta property="og:title" content="Risotto ai Funghi Porcini" />
                <meta property="og:description" content="Primo piatto autunnale cremoso e saporito." />
                <meta property="og:image" content="https://example.com/images/risotto.jpg" />
            </head>
            <body>
                <h1>Risotto ai Funghi Porcini</h1>
            </body>
            </html>
        """.trimIndent()

        val scraped = RecipeScraper.parseHtmlToRecipe(ogHtml, "https://example.com/risotto")

        assertEquals("Risotto ai Funghi Porcini", scraped.title)
        assertEquals("Primo piatto autunnale cremoso e saporito.", scraped.description)
        assertEquals("https://example.com/images/risotto.jpg", scraped.imageUrl)
    }
}
