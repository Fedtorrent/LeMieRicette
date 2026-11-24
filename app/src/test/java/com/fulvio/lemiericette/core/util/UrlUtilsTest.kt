package com.fulvio.lemiericette.core.util

import com.fulvio.lemiericette.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlUtilsTest {

    @Test
    fun `extractUrlFromText extracts valid http and https urls from shared strings`() {
        val chromeShareText = "Guarda questa ricetta fantastica! https://www.giallozafferano.it/ricette/Spaghetti-alla-Carbonara.html super consigliata"
        val extracted = UrlUtils.extractUrlFromText(chromeShareText)

        assertEquals("https://www.giallozafferano.it/ricette/Spaghetti-alla-Carbonara.html", extracted)
    }

    @Test
    fun `extractUrlFromText returns null when no url is present in shared text`() {
        val plainText = "Ecco la lista degli ingredienti: uova, farina, zucchero."
        assertNull(UrlUtils.extractUrlFromText(plainText))
        assertNull(UrlUtils.extractUrlFromText(""))
        assertNull(UrlUtils.extractUrlFromText(null))
    }

    @Test
    fun `Category fromId maps case-insensitive ids correctly and falls back to ALTRO`() {
        assertEquals(Category.DOLCI, Category.fromId("DOLCI"))
        assertEquals(Category.DOLCI, Category.fromId("dolci"))
        assertEquals(Category.PRIMI, Category.fromId("PRIMI"))
        assertEquals(Category.ALTRO, Category.fromId("CATEGORIA_NON_ESISTENTE"))
    }
}
