import StoreKit
import ComposeApp
import Foundation

private typealias AsyncTask = _Concurrency.Task

@available(iOS 15.0, *)
enum StoreKitBridgeSetup {

    static func configure() {
        StoreKitBridge.shared.configure(
            fetchProducts: { ids, completion in
                let stringIds = ids as [String]
                AsyncTask {
                    do {
                        let products = try await Product.products(for: Set(stringIds))
                        let dtos: [ComposeApp.StoreKitProduct] = products.map { product in
                            return ComposeApp.StoreKitProduct(
                                productId: product.id,
                                title: product.displayName,
                                productDescription: product.description,
                                formattedPrice: product.displayPrice,
                                priceAmount: NSDecimalNumber(decimal: product.price).doubleValue,
                                currencyCode: product.priceFormatStyle.currencyCode ?? "",
                                isSubscription: product.type == .autoRenewable
                            )
                        }
                        DispatchQueue.main.async { completion(dtos) }
                    } catch {
                        DispatchQueue.main.async { completion(nil) }
                    }
                }
            },
            purchase: { productId, completion in
                AsyncTask {
                    do {
                        let products = try await Product.products(for: [productId])
                        guard let product = products.first else {
                            DispatchQueue.main.async { completion(3) }
                            return
                        }

                        let result = try await product.purchase()
                        switch result {
                        case .success(let verification):
                            switch verification {
                            case .verified(let transaction):
                                await transaction.finish()
                                DispatchQueue.main.async { completion(0) }
                            case .unverified:
                                DispatchQueue.main.async { completion(3) }
                            }
                        case .userCancelled:
                            DispatchQueue.main.async { completion(1) }
                        case .pending:
                            DispatchQueue.main.async { completion(3) }
                        @unknown default:
                            DispatchQueue.main.async { completion(3) }
                        }
                    } catch {
                        DispatchQueue.main.async { completion(3) }
                    }
                }
            },
            restore: { completion in
                AsyncTask {
                    var hasPremium = false
                    for await result in Transaction.currentEntitlements {
                        if case .verified(_) = result {
                            hasPremium = true
                            break
                        }
                    }
                    DispatchQueue.main.async { completion(KotlinBoolean(bool: hasPremium)) }
                }
            }
        )
    }
}
