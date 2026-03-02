package com.markduenas.localmind.billing

data class BillingProduct(
    val id: String,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
    val productType: ProductType,
)

enum class ProductType {
    ONE_TIME,
    SUBSCRIPTION,
}

object ProductIds {
    const val PREMIUM_LIFETIME = "premium_lifetime"
    const val PREMIUM_MONTHLY = "premium_monthly"

    val ALL = listOf(PREMIUM_LIFETIME, PREMIUM_MONTHLY)
}
