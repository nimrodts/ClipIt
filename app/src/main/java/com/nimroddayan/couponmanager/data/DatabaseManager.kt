package com.nimroddayan.couponmanager.data

import android.content.Context
import android.net.Uri
import com.nimroddayan.couponmanager.data.db.AppDatabase
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseManager(private val context: Context) {

    fun exportDatabase(uri: Uri) {
        val tempFile = java.io.File(context.cacheDir, "backup_temp.db")
        if (createBackup(tempFile)) {
            FileInputStream(tempFile).use { input ->
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile.delete()
        }
    }

    fun createBackup(destFile: java.io.File): Boolean {
        return try {
            val db = AppDatabase.getDatabase(context)
            // Force WAL merge and block until done
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)").close()
            AppDatabase.destroyInstance()

            val dbFile = context.getDatabasePath("coupon_database")
            FileInputStream(dbFile).use { input ->
                FileOutputStream(destFile).use { output -> input.copyTo(output) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importDatabase(uri: Uri): Boolean {
        // First, close the existing database to release the lock
        AppDatabase.destroyInstance()

        val dbFile = context.getDatabasePath("coupon_database")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dbFile).use { output -> input.copyTo(output) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun resetDatabase() {
        val db = AppDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch { db.clearAllTables() }
    }

    // Removed standalone checkpoint as it is now integrated into createBackup
    // fun checkpoint() { ... }

    fun replaceDatabase(newDbFile: java.io.File): Boolean {
        AppDatabase.destroyInstance()

        val dbFile = context.getDatabasePath("coupon_database")

        // Delete WAL files to prevent corruption when replacing the main DB file
        java.io.File(dbFile.path + "-wal").delete()
        java.io.File(dbFile.path + "-shm").delete()

        return try {
            java.io.FileInputStream(newDbFile).use { input ->
                java.io.FileOutputStream(dbFile).use { output -> input.copyTo(output) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
