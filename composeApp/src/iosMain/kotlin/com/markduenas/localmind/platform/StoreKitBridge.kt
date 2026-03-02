package com.markduenas.localmind.platform

/**
 * DTO for passing product data from Swift to Kotlin.
 */
data class StoreKitProduct(
    val productId: String,
    val title: String,
    val productDescription: String,
    val formattedPrice: String,
    val priceAmount: Double,
    val currencyCode: String,
    val isSubscription: Boolean,
)

/**
 * Bridge to StoreKit 2. The Swift side sets the function implementations
 * via [StoreKitBridge.configure] at app startup.
 */
object StoreKitBridge {

    private var fetchProductsImpl: ((List<String>, (List<StoreKitProduct>?) -> Unit) -> Unit)? = null
    private var purchaseImpl: ((String, (Long) -> Unit) -> Unit)? = null
    private var restoreImpl: (((Boolean) -> Unit) -> Unit)? = null

    fun configure(
        fetchProducts: (List<String>, (List<StoreKitProduct>?) -> Unit) -> Unit,
        purchase: (String, (Long) -> Unit) -> Unit,
        restore: ((Boolean) -> Unit) -> Unit,
    ) {
        fetchProductsImpl = fetchProducts
        purchaseImpl = purchase
        restoreImpl = restore
    }

    fun fetchProducts(
        ids: List<String>,
        completion: (List<StoreKitProduct>?) -> Unit,
    ) {
        fetchProductsImpl?.invoke(ids, completion) ?: completion(emptyList())
    }

    fun purchase(
        productId: String,
        completion: (Long) -> Unit,
    ) {
        purchaseImpl?.invoke(productId, completion) ?: completion(3)
    }

    fun restorePurchases(
        completion: (Boolean) -> Unit,
    ) {
        restoreImpl?.invoke(completion) ?: completion(false)
    }
}
