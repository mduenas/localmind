package com.markduenas.localmind.platform

import platform.Foundation.NSBundle

actual class AppVersionProvider {
    actual fun displayString(): String {
        val bundle = NSBundle.mainBundle
        val marketingVersion = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
        val buildVersion = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String

        return when {
            !marketingVersion.isNullOrBlank() && !buildVersion.isNullOrBlank() -> "$marketingVersion ($buildVersion)"
            !marketingVersion.isNullOrBlank() -> marketingVersion
            !buildVersion.isNullOrBlank() -> buildVersion
            else -> "unknown"
        }
    }
}
