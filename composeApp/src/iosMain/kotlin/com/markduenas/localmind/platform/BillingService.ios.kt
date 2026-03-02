package com.markduenas.localmind.platform

import com.markduenas.localmind.billing.BillingProduct
import com.markduenas.localmind.billing.ProductType
import com.markduenas.localmind.billing.PurchaseResult
import platform.Foundation.NSUserDefaults
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val KEY_PREMIUM = "localmind_premium_active"
private const val KEY_PRODUCT_ID = "localmind_premium_product_id"

actual class BillingService {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual suspend fun connect(): Boolean {
        // StoreKit 2 needs no explicit connection
        return true
    }

    actual fun disconnect() {
        // No-op for StoreKit
    }

    actual suspend fun queryProducts(ids: List<String>): List<BillingProduct> =
        suspendCoroutine { cont ->
            StoreKitBridge.fetchProducts(ids) { products ->
                val result = products?.map { dto ->
                    BillingProduct(
                        id = dto.productId,
                        title = dto.title,
                        description = dto.productDescription,
                        formattedPrice = dto.formattedPrice,
                        priceAmountMicros = (dto.priceAmount * 1_000_000).toLong(),
                        priceCurrencyCode = dto.currencyCode,
                        productType = if (dto.isSubscription) ProductType.SUBSCRIPTION else ProductType.ONE_TIME,
                    )
                } ?: emptyList()
                cont.resume(result)
            }
        }

    actual suspend fun purchase(productId: String): PurchaseResult =
        suspendCoroutine { cont ->
            StoreKitBridge.purchase(productId) { statusCode ->
                val result = when (statusCode.toInt()) {
                    0 -> PurchaseResult.Success
                    1 -> PurchaseResult.Cancelled
                    2 -> PurchaseResult.AlreadyOwned
                    else -> PurchaseResult.Error("StoreKit purchase failed (code $statusCode)")
                }
                cont.resume(result)
            }
        }

    actual suspend fun restorePurchases(): Boolean =
        suspendCoroutine { cont ->
            StoreKitBridge.restorePurchases { hasPremium ->
                cont.resume(hasPremium)
            }
        }

    actual fun hasPremiumLocally(): Boolean {
        return defaults.boolForKey(KEY_PREMIUM)
    }

    actual suspend fun verifyEntitlement(): Boolean {
        val hasEntitlement = restorePurchases()
        setPremiumLocal(hasEntitlement, null)
        return hasEntitlement
    }

    internal fun setPremiumLocal(active: Boolean, productId: String?) {
        defaults.setBool(active, forKey = KEY_PREMIUM)
        if (productId != null) {
            defaults.setObject(productId, forKey = KEY_PRODUCT_ID)
        }
    }
}
