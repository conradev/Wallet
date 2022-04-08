#if os(macOS)

import DictionaryCoding
import Foundation
import ServiceManagement

public protocol IgnoringMethodDeprecation {
    var isRunning: Bool { get }
}

public final class LoginItem: ObservableObject, IgnoringMethodDeprecation {
    private struct Job: Codable {
        var label: String

        enum CodingKeys: String, CodingKey {
            case label = "Label"
        }
    }

    public static let viewService = LoginItem(Wallet.viewServiceBundleIdentifier)

    @available(macOS, deprecated: 10.10)
    private var job: Job? {
        guard
            let dictionaries = SMCopyAllJobDictionaries(kSMDomainUserLaunchd)
                .takeRetainedValue() as? [NSDictionary] else {
            return nil
        }

        return try! dictionaries
            .lazy
            .map { try DictionaryDecoder().decode(Job.self, from: $0) }
            .first { $0.label == bundleIdentifier }
    }

    @available(macOS, deprecated: 10.10)
    public var isRunning: Bool {
        job != nil
    }

    public var bundleIdentifier: String

    public init(_ bundleIdentifier: String) {
        self.bundleIdentifier = bundleIdentifier
    }

    public func start() {
        objectWillChange.send()
        SMLoginItemSetEnabled(bundleIdentifier as CFString, true)
    }

    public func stop() {
        objectWillChange.send()
        SMLoginItemSetEnabled(bundleIdentifier as CFString, false)
    }
}

#endif
