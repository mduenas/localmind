package com.markduenas.localmind.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.markduenas.localmind.security.EncryptionKeyProvider

actual class DatabaseDriverFactory(
    private val keyProvider: EncryptionKeyProvider,
) {
    actual fun createDriver(): SqlDriver {
        val passphrase = keyProvider.getOrCreateKey()
        return NativeSqliteDriver(LocalMindDb.Schema, "localmind.db").also { driver ->
            val escapedPassphrase = passphrase.replace("'", "''")
            driver.execute(null, "PRAGMA key = '$escapedPassphrase';", 0)
        }
    }
}
