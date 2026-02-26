package com.markduenas.localmind.platform

expect class PermissionHelper {
    fun hasNotificationPermission(): Boolean
    fun hasMicrophonePermission(): Boolean
    fun hasSpeechRecognitionPermission(): Boolean
}
