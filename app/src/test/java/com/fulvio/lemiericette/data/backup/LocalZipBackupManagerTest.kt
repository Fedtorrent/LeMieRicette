package com.fulvio.lemiericette.data.backup

import com.fulvio.lemiericette.data.local.backup.LocalZipBackupManager
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalZipBackupManagerTest {

    @Test
    fun `generateDefaultBackupFileName returns valid zip extension`() {
        val fileName = LocalZipBackupManager.generateDefaultBackupFileName()

        assertTrue(fileName.startsWith("lemiericette_backup_"))
        assertTrue(fileName.endsWith(".zip"))
    }
}
