package com.markduenas.localmind

import androidx.compose.ui.window.ComposeUIViewController
import com.markduenas.localmind.di.initKoin
import com.markduenas.localmind.di.iosModule

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin(listOf(iosModule))
    }
) {
    App()
}
