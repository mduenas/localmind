package com.markduenas.localmind.platform

import android.content.Context

actual class AppVersionProvider(private val context: Context) {
    @Suppress("DEPRECATION")
    actual fun displayString(): String {
        return runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName ?: "unknown"
            "$versionName (${packageInfo.versionCode})"
        }.getOrElse { "unknown" }
    }
}
