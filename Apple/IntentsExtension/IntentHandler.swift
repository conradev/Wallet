import Intents
import WalletCore

class IntentHandler: INExtension {
    let app = KoinApplication.start()

    override func handler(for intent: INIntent) -> Any {
        let koin = app.koin
        switch intent {
        case is DisplayAssetIntent:
            return DisplayAssetIntentHandler()
        default:
            return self
        }
    }
}
