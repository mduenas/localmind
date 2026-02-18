package com.markduenas.localmind.data.local

import android.content.Context
import android.util.Log
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.markduenas.localmind.security.EncryptionKeyProvider
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.io.File

actual class DatabaseDriverFactory(
    private val context: Context,
    private val keyProvider: EncryptionKeyProvider,
) {
    actual fun createDriver(): SqlDriver {
        System.loadLibrary("sqlcipher")
        val passphrase = keyProvider.getOrCreateKey()
        migrateIfNeeded(passphrase)
        val factory = SupportOpenHelperFactory(passphrase.toByteArray())
        return AndroidSqliteDriver(LocalMindDb.Schema, context, "localmind.db", factory)
    }

    private fun migrateIfNeeded(passphrase: String) {
        val dbFile = context.getDatabasePath("localmind.db")
        if (!dbFile.exists()) return

        if (isEncrypted(dbFile)) return

        Log.i("DatabaseDriverFactory", "Migrating unencrypted database to SQLCipher")
        val tempFile = File(dbFile.parentFile, "localmind_encrypted.db")
        try {
            // Open the unencrypted DB using SQLCipher with an empty passphrase
            val db = net.zetetic.database.sqlcipher.SQLiteDatabase.openDatabase(
                dbFile.absolutePath, "", null,
                net.zetetic.database.sqlcipher.SQLiteDatabase.OPEN_READWRITE, null, null
            )
            // Verify we can read it
            db.rawQuery("SELECT count(*) FROM sqlite_master;", null).use { it.moveToFirst() }

            val escapedPassphrase = passphrase.replace("'", "''")
            db.execSQL("ATTACH DATABASE '${tempFile.absolutePath}' AS encrypted KEY '$escapedPassphrase'")
            db.execSQL("SELECT sqlcipher_export('encrypted')")
            db.execSQL("DETACH DATABASE encrypted")
            db.close()

            dbFile.delete()
            tempFile.renameTo(dbFile)
            Log.i("DatabaseDriverFactory", "Migration to encrypted database complete")
        } catch (e: Exception) {
            Log.e("DatabaseDriverFactory", "Migration failed, deleting unencrypted DB", e)
            // If migration fails, remove the old unencrypted DB so the app can start fresh
            tempFile.delete()
            dbFile.delete()
        }
    }

    private fun isEncrypted(dbFile: File): Boolean {
        return try {
            val header = ByteArray(16)
            dbFile.inputStream().use { it.read(header) }
            val sqliteHeader = "SQLite format 3\u0000"
            String(header, Charsets.US_ASCII) != sqliteHeader
        } catch (_: Exception) {
            false
        }
    }
}
