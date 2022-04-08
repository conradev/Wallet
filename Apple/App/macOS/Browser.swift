import AppKit
import Foundation
import UniformTypeIdentifiers
import WalletCore

protocol Browser {
    static var bundleIdentifier: String { get }

    static var host: NativeMessagingHost { get throws }
    static var hostsDirectoryURL: URL { get }
}

extension Browser {
    static var isInstalled: Bool {
        NSWorkspace.shared.urlForApplication(withBundleIdentifier: bundleIdentifier) != nil
    }

    static func write() throws {
        guard isInstalled else { return }

        let hostsDirectoryURL = hostsDirectoryURL
        let manifestURL = hostsDirectoryURL
            .appendingPathComponent(Wallet.appBundleIdentifier)
            .appendingPathExtension("json")

        try FileManager.default.createDirectory(at: hostsDirectoryURL, withIntermediateDirectories: true)
        try Self.host.write(to: manifestURL)
    }
}

struct Chrome: Browser {
    static var bundleIdentifier: String {
        "com.google.Chrome"
    }

    static var host: NativeMessagingHost {
        get throws {
            var host = try NativeMessagingHost()
            host.allowedOrigins = ["chrome-extension://fpeeneeiicihpbfheodfjolbhabcjnej/"]
            return host
        }
    }

    static var hostsDirectoryURL: URL {
        FileManager.default
            .realHomeDirectoryForCurrentUser
            .appendingPathComponent("Library")
            .appendingPathComponent("Application Support")
            .appendingPathComponent("Google")
            .appendingPathComponent("Chrome")
            .appendingPathComponent("NativeMessagingHosts")
    }
}

struct Firefox: Browser {
    static var bundleIdentifier: String {
        "org.mozilla.firefox"
    }

    static var host: NativeMessagingHost {
        get throws {
            var host = try NativeMessagingHost()
            host.allowedExtensions = ["com.conradkramer.wallet@conradkramer.com"]
            return host
        }
    }

    static var hostsDirectoryURL: URL {
        FileManager.default
            .realHomeDirectoryForCurrentUser
            .appendingPathComponent("Library")
            .appendingPathComponent("Application Support")
            .appendingPathComponent("Mozilla")
            .appendingPathComponent("NativeMessagingHosts")
    }
}

struct NativeMessagingHost: Codable {
    enum Error: Swift.Error {
        case invalidBundle
    }

    enum CodingKeys: String, CodingKey {
        case name
        case description
        case path
        case type
        case allowedOrigins = "allowed_origins"
        case allowedExtensions = "allowed_extensions"
    }

    var name: String
    var description: String
    var path: String
    var type = "stdio"
    var allowedOrigins: [String]?
    var allowedExtensions: [String]?

    init() throws {
        let bundle = Bundle.main
        guard
            let bundleIdentifier = bundle.bundleIdentifier,
            let localizedName = bundle.localizedName,
            let sharedSupportPath = bundle.sharedSupportPath else { throw Error.invalidBundle }

        self.name = bundleIdentifier
        self.description = localizedName
        self.path = URL(fileURLWithPath: sharedSupportPath).appendingPathComponent("WalletNativeMessageHost").path
    }

    func write(to url: URL) throws {
        let data = try JSONEncoder().encode(self)
        try data.write(to: url)
    }
}

extension Bundle {
    var localizedName: String? {
        let displayName = Bundle.main.object(forInfoDictionaryKey: "CFBundleDisplayName") as? String
        let name = Bundle.main.object(forInfoDictionaryKey: kCFBundleNameKey as String) as? String
        return displayName ?? name
    }
}

extension FileManager {
    var realHomeDirectoryForCurrentUser: URL {
        let directory = getpwuid(getuid()).pointee.pw_dir!
        return URL(fileURLWithFileSystemRepresentation: directory, isDirectory: true, relativeTo: nil)
    }
}
