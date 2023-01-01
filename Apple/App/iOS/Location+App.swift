import UIKit
import WalletUICore

extension Location {
    public func open() {
        let application = UIApplication.shared
        if application.supportsMultipleScenes {
            let options = UIWindowScene.ActivationRequestOptions()
            options.preferredPresentationStyle = .prominent
            application.requestSceneSessionActivation(
                nil,
                userActivity: userActivity,
                options: options,
                errorHandler: nil
            )
        } else {
            application.open(universalLink)
        }
    }
}
