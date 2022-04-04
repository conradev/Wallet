import SwiftUI
import WalletCore

@main
struct WalletApp: App {
    let app = KoinApplication.start()

    var body: some Scene {
        WindowGroup {
            MainView()
        }
    }
}
