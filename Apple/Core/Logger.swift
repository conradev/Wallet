@_exported import OSLog

extension Logger {
    public static let subsystem = Wallet.appBundleIdentifier

    public static let `default` = Self(subsystem: subsystem, category: "General")
}
