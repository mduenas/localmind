package com.markduenas.localmind.security

expect class EncryptionKeyProvider {
    fun getOrCreateKey(): String
}
