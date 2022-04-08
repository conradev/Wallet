import Foundation

public protocol Location {
    static var id: String { get }

    var directURL: URL { get }
    var universalLink: URL { get }

    init?(_ url: URL)
}

extension Location {
    static var baseURL: URL {
        URL(string: "https://conradkramer.com/wallet/")!
    }

    static var urlScheme: String {
        "walleth"
    }

    static var activityType: String {
        "\(Wallet.appBundleIdentifier).\(id)"
    }

    static var universalLink: URL {
        baseURL.appendingPathComponent(id).appendingPathComponent("")
    }

    public static var directURL: URL {
        var components = URLComponents(string: "\(urlScheme)://")!
        components.host = id
        components.path = "/"
        return components.url!
    }

    var userInfo: [String: Any] {
        ["url": universalLink]
    }

    public var userActivity: NSUserActivity {
        let userActivity = NSUserActivity(activityType: Self.activityType)
        userActivity.userInfo = userInfo
        userActivity.requiredUserInfoKeys = Set(userInfo.keys)
        userActivity.targetContentIdentifier = universalLink.absoluteString
        return userActivity
    }

    public init?(_ userActivity: NSUserActivity) {
        guard
            userActivity.activityType == Self.activityType,
            let url = userActivity.userInfo?["url"] as? URL else { return nil }
        self.init(url)
    }
}

public struct PromptLocation: Location {
    public static let id = "prompt"

    public var id: String

    public var universalLink: URL {
        Self.universalLink.appendingPathComponent(id)
    }

    public var directURL: URL {
        Self.directURL.appendingPathComponent(id)
    }

    public init?(_ url: URL) {
        guard
            url.deletingLastPathComponent() == Self.universalLink ||
            url.deletingLastPathComponent() == Self.directURL else { return nil }
        self.id = url.lastPathComponent
    }

    public init(_ prompt: Prompt) {
        self.init(id: prompt.id)
    }

    public init(id: String) {
        self.id = id
    }
}
