package com.markduenas.localmind.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration
import com.markduenas.localmind.security.EncryptionKeyProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSFileManager
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread

actual class DatabaseDriverFactory(
    private val keyProvider: EncryptionKeyProvider,
) {
    actual fun createDriver(): SqlDriver {
        val passphrase = keyProvider.getOrCreateKey()
        migrateIfNeeded()
        val schema = LocalMindDb.Schema
        val configuration = DatabaseConfiguration(
            name = "localmind.db",
            version = schema.version.toInt(),
            create = { connection ->
                wrapConnection(connection) { schema.create(it) }
            },
            upgrade = { connection, oldVersion, newVersion ->
                wrapConnection(connection) { schema.migrate(it, oldVersion.toLong(), newVersion.toLong()) }
            },
            encryptionConfig = DatabaseConfiguration.Encryption(
                key = passphrase,
            ),
        )
        return NativeSqliteDriver(configuration)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun migrateIfNeeded() {
        val dbPath = getDatabasePath() ?: return
        val fm = NSFileManager.defaultManager
        if (!fm.fileExistsAtPath(dbPath)) return

        // Check if the file is an unencrypted SQLite database
        if (!isUnencrypted(dbPath)) return

        // Delete the old unencrypted database so a fresh encrypted one is created
        fm.removeItemAtPath(dbPath, error = null)
        // Also remove journal/wal files
        fm.removeItemAtPath("$dbPath-journal", error = null)
        fm.removeItemAtPath("$dbPath-wal", error = null)
        fm.removeItemAtPath("$dbPath-shm", error = null)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun isUnencrypted(path: String): Boolean {
        val file = fopen(path, "rb") ?: return false
        val header = ByteArray(16)
        val bytesRead = header.usePinned { pinned ->
            fread(pinned.addressOf(0), 1u, 16u, file)
        }
        fclose(file)
        if (bytesRead < 16u) return false
        return header.decodeToString() == "SQLite format 3\u0000"
    }

    private fun getDatabasePath(): String? {
        @Suppress("UNCHECKED_CAST")
        val paths = NSSearchPathForDirectoriesInDomains(
            NSApplicationSupportDirectory, NSUserDomainMask, true
        ) as? List<String> ?: return null
        val appSupportDir = paths.firstOrNull() ?: return null
        return "$appSupportDir/databases/localmind.db"
    }
}
