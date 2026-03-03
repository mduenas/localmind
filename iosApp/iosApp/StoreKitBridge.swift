import StoreKit
import ComposeApp
import Foundation

private typealias AsyncTask = _Concurrency.Task

@available(iOS 15.0, *)
enum StoreKitBridgeSetup {

    /// Holds the long-running Transaction.updates task so it isn't deallocated.
    private static var updatesTask: AsyncTask<Void, Never>?

    /// Must be called at app launch to observe transaction updates.
    static func listenForTransactions() {
        updatesTask = AsyncTask(priority: .background) {
            for await result in Transaction.updates {
                if case .verified(let transaction) = result {
                    await transaction.finish()
                }
            }
        }
    }

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
                DispatchQueue.main.async {
                    AsyncTask { @MainActor in
                        do {
                            // Check if already owned (non-consumable)
                            if let existing = await Transaction.latest(for: productId),
                               case .verified(let txn) = existing,
                               txn.revocationDate == nil {
                                completion(2) // alreadyOwned
                                return
                            }

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
                                    completion(3) // error
                                }
                            case .userCancelled:
                                completion(1) // cancelled
                            case .pending:
                                completion(3) // error
                            @unknown default:
                                completion(3) // error
                            }
                        } catch {
                            completion(3) // error
                        }
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
