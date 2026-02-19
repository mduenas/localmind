package com.markduenas.localmind.platform

expect class FileSharer {
    /**
     * Writes [content] to a temporary file with the given [fileName],
     * then opens the platform share sheet so the user can save or send it.
     */
    fun share(fileName: String, content: String, mimeType: String = "application/json")
}
