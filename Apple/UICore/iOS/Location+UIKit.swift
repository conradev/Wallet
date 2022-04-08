import UIKit
import WalletCore

extension Location {
    public init?(_ openURLContext: UIOpenURLContext) {
        self.init(openURLContext.url)
    }

    public init?(_ connectionOptions: UIScene.ConnectionOptions) {
        let locations = [
            connectionOptions.urlContexts.compactMap(Self.init),
            connectionOptions.userActivities.compactMap(Self.init)
        ]
        guard let location = locations.flatMap({ $0 }).first else {
            return nil
        }
        self = location
    }
}
