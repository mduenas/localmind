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
    @Suppress("UNUSED_PARAMETER") keyProvider: EncryptionKeyProvider,
) {
    actual fun createDriver(): SqlDriver {
        // Delete any leftover encrypted/corrupted databases from previous builds
        deleteIncompatibleDatabase()

        val schema = LocalMindDb.Schema
        val configuration = DatabaseConfiguration(
            name = DB_NAME,
            version = schema.version.toInt(),
            create = { connection ->
                wrapConnection(connection) { schema.create(it) }
            },
            upgrade = { connection, oldVersion, newVersion ->
                wrapConnection(connection) {
                    schema.migrate(it, oldVersion.toLong(), newVersion.toLong())
                }
            },
            // Note: Encryption disabled — SQLDelight 2.0.2's native driver uses the system
            // sqlite3 on iOS which does not support PRAGMA key, even when SQLCipher pod is
            // present. The iOS sandbox provides file-level protection. Encryption can be
            // re-enabled once the project migrates to a SQLCipher-aware driver.
        )
        return try {
            NativeSqliteDriver(configuration)
        } catch (e: Exception) {
            // If open fails (e.g. leftover encrypted file), delete and retry
            deleteDatabaseFiles()
            NativeSqliteDriver(configuration)
        }
    }

    /**
     * Delete databases that can't be opened without encryption —
     * i.e. files whose header does NOT start with the standard SQLite magic.
     * These are leftover encrypted databases from previous builds.
     */
    @OptIn(ExperimentalForeignApi::class)
    private fun deleteIncompatibleDatabase() {
        for (path in getPossibleDatabasePaths()) {
            val fm = NSFileManager.defaultManager
            if (!fm.fileExistsAtPath(path)) continue

            val file = fopen(path, "rb") ?: continue
            val header = ByteArray(16)
            val bytesRead = header.usePinned { pinned ->
                fread(pinned.addressOf(0), 1u, 16u, file)
            }
            fclose(file)

            if (bytesRead < 16u) {
                // Empty or tiny file — delete
                deleteDatabaseFilesAt(path)
                continue
            }

            val isStandardSqlite = header.decodeToString() == "SQLite format 3\u0000"
            if (!isStandardSqlite) {
                // Encrypted or corrupted — delete so plain driver can create fresh
                deleteDatabaseFilesAt(path)
            }
        }
    }

    private fun deleteDatabaseFiles() {
        for (path in getPossibleDatabasePaths()) {
            deleteDatabaseFilesAt(path)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun deleteDatabaseFilesAt(path: String) {
        val fm = NSFileManager.defaultManager
        fm.removeItemAtPath(path, error = null)
        fm.removeItemAtPath("$path-journal", error = null)
        fm.removeItemAtPath("$path-wal", error = null)
        fm.removeItemAtPath("$path-shm", error = null)
    }

    private fun getPossibleDatabasePaths(): List<String> {
        val paths = mutableListOf<String>()

        // sqliter default: NSHomeDirectory()/databases/
        val home = platform.Foundation.NSHomeDirectory()
        paths.add("$home/databases/$DB_NAME")

        // Application Support/databases/
        @Suppress("UNCHECKED_CAST")
        val appSupportPaths = NSSearchPathForDirectoriesInDomains(
            NSApplicationSupportDirectory, NSUserDomainMask, true
        ) as? List<String>
        appSupportPaths?.firstOrNull()?.let { appSupport ->
            paths.add("$appSupport/databases/$DB_NAME")
        }

        // Library/LocalDatabase/
        paths.add("$home/Library/LocalDatabase/$DB_NAME")

        return paths
    }

    companion object {
        private const val DB_NAME = "localmind.db"
    }
}
