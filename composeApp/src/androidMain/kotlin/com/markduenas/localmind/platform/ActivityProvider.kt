package com.markduenas.localmind.platform

import android.app.Activity
import java.lang.ref.WeakReference

object ActivityProvider {
    private var activityRef: WeakReference<Activity>? = null

    var activity: Activity?
        get() = activityRef?.get()
        set(value) {
            activityRef = value?.let { WeakReference(it) }
        }
}
