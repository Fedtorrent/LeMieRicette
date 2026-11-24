package com.fulvio.lemiericette.core.util

import com.fulvio.lemiericette.domain.model.Recipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ImageCleanupUtilsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `cleanupOrphanImages deletes unused images correctly`() {
        val img1 = tempFolder.newFile("photo1.jpg")
        val img2 = tempFolder.newFile("photo2.jpg")
        val orphan = tempFolder.newFile("orphan.jpg")

        img1.writeText("data1")
        img2.writeText("data2")
        orphan.writeText("data_orphan")

        val activeRecipes = listOf(
            Recipe(title = "R1", categoryId = "PRIMI", imagePath = img1.absolutePath),
            Recipe(title = "R2", categoryId = "SECONDI", imagePath = img2.absolutePath)
        )

        val activeNames = activeRecipes.mapNotNull { it.imagePath }.map { File(it).name.lowercase() }.toSet()
        val allFiles = listOf(img1, img2, orphan)

        var deletedCount = 0
        for (file in allFiles) {
            if (file.name.lowercase() !in activeNames) {
                if (file.delete()) {
                    deletedCount++
                }
            }
        }

        assertEquals(1, deletedCount)
        assertTrue(img1.exists())
        assertTrue(img2.exists())
        assertFalse(orphan.exists())
    }
}
