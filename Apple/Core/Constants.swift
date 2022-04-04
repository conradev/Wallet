@_implementationOnly import Constants
import Shared

extension Wallet {
    public static let appBundleIdentifier = Constants.AppBundleIdentifier
    public static let appGroupIdentifier = Constants.AppGroupIdentifier
    public static let signerBundleIdentifier = Constants.SignerBundleIdentifier

    public static let localizedAppName = Companion.shared.localizedAppName
}
