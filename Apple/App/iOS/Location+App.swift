import UIKit
import WalletUICore

extension Location {
    public func open() {
        let options = UIWindowScene.ActivationRequestOptions()
        options.preferredPresentationStyle = .prominent
        UIApplication.shared.requestSceneSessionActivation(
            nil,
            userActivity: userActivity,
            options: options,
            errorHandler: nil
        )
    }
}
