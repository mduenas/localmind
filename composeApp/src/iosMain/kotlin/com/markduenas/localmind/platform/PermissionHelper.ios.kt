package com.markduenas.localmind.platform

import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.runBlocking

actual class PermissionHelper {
    actual fun hasNotificationPermission(): Boolean {
        var granted = false
        val semaphore = platform.darwin.dispatch_semaphore_create(0)
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
            granted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
            platform.darwin.dispatch_semaphore_signal(semaphore)
        }
        platform.darwin.dispatch_semaphore_wait(semaphore, platform.darwin.DISPATCH_TIME_FOREVER)
        return granted
    }

    actual fun hasMicrophonePermission(): Boolean {
        return AVAudioSession.sharedInstance().recordPermission == AVAudioSessionRecordPermissionGranted
    }
}
