package com.markduenas.localmind.data.repository

import com.markduenas.localmind.billing.BillingProduct
import com.markduenas.localmind.billing.ProductIds
import com.markduenas.localmind.billing.PurchaseResult
import com.markduenas.localmind.platform.BillingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingRepository(
    private val billingService: BillingService,
    private val settingsRepository: SettingsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _products = MutableStateFlow<List<BillingProduct>>(emptyList())
    val products: StateFlow<List<BillingProduct>> = _products.asStateFlow()

    fun initialize() {
        scope.launch {
            try {
                val connected = billingService.connect()
                if (!connected) return@launch

                // Load available products
                val fetched = billingService.queryProducts(ProductIds.ALL)
                _products.value = fetched

                // Re-verify entitlement in background
                val verified = billingService.verifyEntitlement()
                settingsRepository.setPremiumActive(verified)
            } catch (_: Exception) {
                // Non-fatal — user can still use free tier
            }
        }
    }

    suspend fun purchase(productId: String): PurchaseResult {
        val result = billingService.purchase(productId)
        if (result is PurchaseResult.Success || result is PurchaseResult.AlreadyOwned) {
            settingsRepository.setPremiumActive(true, productId)
        }
        return result
    }

    suspend fun restorePurchases(): Boolean {
        val restored = billingService.restorePurchases()
        settingsRepository.setPremiumActive(restored)
        return restored
    }

    fun isPremiumLocally(): Boolean = settingsRepository.premiumActive.value

    fun disconnect() {
        billingService.disconnect()
    }
}
