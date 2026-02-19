package com.markduenas.localmind.platform

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual class FileSharer {
    @OptIn(BetaInteropApi::class)
    actual fun share(fileName: String, content: String, mimeType: String) {
        val filePath = NSTemporaryDirectory() + fileName
        val nsString = NSString.create(string = content)
        nsString.writeToFile(filePath, atomically = true)

        val fileUrl = NSURL.fileURLWithPath(filePath)
        val activityVC = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null,
        )

        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootVC?.presentViewController(activityVC, animated = true, completion = null)
    }
}
