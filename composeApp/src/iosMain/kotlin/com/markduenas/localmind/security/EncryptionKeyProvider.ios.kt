package com.markduenas.localmind.security

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.darwin.OSStatus
import platform.posix.arc4random_buf

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class EncryptionKeyProvider {

    private companion object {
        const val SERVICE = "com.markduenas.localmind"
        const val ACCOUNT = "db_encryption_key"
        const val KEY_LENGTH_BYTES = 32
    }

    actual fun getOrCreateKey(): String {
        readFromKeychain()?.let { return it }

        val keyBytes = generateRandomBytes(KEY_LENGTH_BYTES)
        val encoded = keyBytes.encodeToHexString()
        writeToKeychain(encoded)
        return encoded
    }

    private fun readFromKeychain(): String? = memScoped {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE,
            kSecAttrAccount to ACCOUNT,
            kSecReturnData to true,
        )
        val cfQuery = CFBridgingRetain(query) as CFDictionaryRef

        val resultPtr = alloc<kotlinx.cinterop.ObjCObjectVar<Any?>>()
        val status: OSStatus = SecItemCopyMatching(cfQuery, resultPtr.ptr)
        CFBridgingRelease(cfQuery)

        if (status != errSecSuccess) return null

        val data = resultPtr.value as? NSData ?: return null
        NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
    }

    private fun writeToKeychain(value: String) {
        val valueData = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val attributes = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE,
            kSecAttrAccount to ACCOUNT,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlock,
            kSecValueData to valueData,
        )
        val cfAttributes = CFBridgingRetain(attributes) as CFDictionaryRef
        SecItemAdd(cfAttributes, null)
        CFBridgingRelease(cfAttributes)
    }

    private fun generateRandomBytes(count: Int): ByteArray {
        val bytes = ByteArray(count)
        arc4random_buf(bytes.refTo(0), count.toULong())
        return bytes
    }

    private fun ByteArray.encodeToHexString(): String =
        joinToString("") { it.toInt().and(0xFF).toString(16).padStart(2, '0') }
}
