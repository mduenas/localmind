package com.markduenas.localmind.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.SecureRandom

actual class EncryptionKeyProvider(private val context: Context) {

    private companion object {
        const val PREFS_NAME = "localmind_secure_prefs"
        const val KEY_ALIAS = "db_encryption_key"
        const val KEY_LENGTH_BYTES = 32
    }

    actual fun getOrCreateKey(): String {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val prefs = EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
        val existing = prefs.getString(KEY_ALIAS, null)
        if (existing != null) return existing

        val keyBytes = ByteArray(KEY_LENGTH_BYTES)
        SecureRandom().nextBytes(keyBytes)
        val encoded = Base64.encodeToString(keyBytes, Base64.NO_WRAP)
        prefs.edit().putString(KEY_ALIAS, encoded).apply()
        return encoded
    }
}
