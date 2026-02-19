package com.markduenas.localmind.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration
import com.markduenas.localmind.security.EncryptionKeyProvider

actual class DatabaseDriverFactory(
    private val keyProvider: EncryptionKeyProvider,
) {
    actual fun createDriver(): SqlDriver {
        val passphrase = keyProvider.getOrCreateKey()
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
}
