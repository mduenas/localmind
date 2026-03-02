package com.markduenas.localmind.platform

import com.markduenas.localmind.billing.BillingProduct
import com.markduenas.localmind.billing.PurchaseResult

expect class BillingService {
    suspend fun connect(): Boolean
    fun disconnect()
    suspend fun queryProducts(ids: List<String>): List<BillingProduct>
    suspend fun purchase(productId: String): PurchaseResult
    suspend fun restorePurchases(): Boolean
    fun hasPremiumLocally(): Boolean
    suspend fun verifyEntitlement(): Boolean
}
