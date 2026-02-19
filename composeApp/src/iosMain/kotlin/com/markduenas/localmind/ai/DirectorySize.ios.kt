package com.markduenas.localmind.ai

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSFileSize

@OptIn(ExperimentalForeignApi::class)
actual fun directorySize(path: String): Long {
    val fm = NSFileManager.defaultManager
    val enumerator = fm.enumeratorAtPath(path) ?: return 0L
    var total = 0L
    while (true) {
        val file = enumerator.nextObject() as? String ?: break
        val fullPath = "$path/$file"
        val attrs = fm.attributesOfItemAtPath(fullPath, error = null) ?: continue
        val size = attrs[NSFileSize] as? NSNumber ?: continue
        total += size.longValue
    }
    return total
}
