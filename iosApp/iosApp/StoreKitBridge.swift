import StoreKit
import ComposeApp

@available(iOS 15.0, *)
enum StoreKitBridgeSetup {

    static func configure() {
        StoreKitBridge.shared.configure(
            fetchProducts: { ids, completion in
                let stringIds = ids.compactMap { $0 as? String }
                Task {
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
                        completion(dtos)
                    } catch {
                        completion(nil)
                    }
                }
            },
            purchase: { productId, completion in
                Task {
                    do {
                        let products = try await Product.products(for: [productId])
                        guard let product = products.first else {
                            completion(3) // error
                            return
                        }

                        let result = try await product.purchase()
                        switch result {
                        case .success(let verification):
                            switch verification {
                            case .verified(let transaction):
                                await transaction.finish()
                                completion(0) // success
                            case .unverified:
                                completion(3) // error - verification failed
                            }
                        case .userCancelled:
                            completion(1) // cancelled
                        case .pending:
                            completion(3) // pending treated as error for now
                        @unknown default:
                            completion(3) // error
                        }
                    } catch {
                        completion(3) // error
                    }
                }
            },
            restore: { completion in
                Task {
                    var hasPremium = false
                    for await result in Transaction.currentEntitlements {
                        if case .verified(_) = result {
                            hasPremium = true
                            break
                        }
                    }
                    completion(KotlinBoolean(bool: hasPremium))
                }
            }
        )
    }
}
