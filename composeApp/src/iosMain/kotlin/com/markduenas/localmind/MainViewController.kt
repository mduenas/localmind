package com.markduenas.localmind

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import com.markduenas.localmind.data.repository.BillingRepository
import com.markduenas.localmind.di.initKoin
import com.markduenas.localmind.di.iosModule
import org.koin.mp.KoinPlatform

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin(listOf(iosModule))
    }
) {
    LaunchedEffect(Unit) {
        val billingRepository = KoinPlatform.getKoin().get<BillingRepository>()
        billingRepository.initialize()
    }
    App()
}
