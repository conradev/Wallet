@_implementationOnly import Constants
import Shared

extension Wallet {
    public static let appBundleIdentifier = Constants.AppBundleIdentifier
    static let appGroupIdentifier = Constants.AppGroupIdentifier
    static let viewServiceBundleIdentifier = Constants.ViewServiceBundleIdentifier

    public static let localizedAppName = Companion.shared.localizedAppName
}
