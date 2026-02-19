package com.markduenas.localmind.ai

import java.io.File

actual fun directorySize(path: String): Long {
    val dir = File(path)
    if (!dir.exists()) return 0L
    return dir.walk().filter { it.isFile }.sumOf { it.length() }
}
