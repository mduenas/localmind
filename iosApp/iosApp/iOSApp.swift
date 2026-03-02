import SwiftUI
import UserNotifications
import ComposeApp

@main
struct iOSApp: App {
    init() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { _, _ in }
        // Initialize StoreKit bridge so Kotlin can call into StoreKit 2
        if #available(iOS 15.0, *) {
            StoreKitBridgeSetup.configure()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
