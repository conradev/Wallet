import Intents
import WalletCore

class DisplayAssetIntentHandler: NSObject, DisplayAssetIntentHandling {

    func provideAccountOptionsCollection(for intent: WalletCore.DisplayAssetIntent) async throws -> INObjectCollection<WalletCore.Account> {
        INObjectCollection(items: [
            Account(identifier: "1", display: "First Account")
        ])
    }

    func defaultAccount(for intent: DisplayAssetIntent) -> [Account]? {
        [
            Account(identifier: "1", display: "First Account")
        ]
    }

    func provideAssetOptionsCollection(for intent: WalletCore.DisplayAssetIntent) async throws -> INObjectCollection<WalletCore.Asset> {
        INObjectCollection(items: [])
    }

    func defaultAsset(for intent: DisplayAssetIntent) -> Asset? {
        nil
    }
}
