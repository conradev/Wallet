import Foundation
import SafariServices

extension NSExtensionItem {
    static var placeholder: NSExtensionItem {
        NSExtensionItem(data: "{}".data(using: .utf8)!)
    }

    var isPlacerholder: Bool {
        if let object = userInfo?[SFExtensionMessageKey] as? [String: Any] {
            return object.isEmpty
        } else {
            return false
        }
    }

    var data: Data? {
        get {
            guard let object = userInfo?[SFExtensionMessageKey] as? NSDictionary else { return nil }
            return try! JSONSerialization.data(withJSONObject: object)
        }
        set {
            if let data = newValue {
                userInfo = [
                    // swiftlint:disable:next force_cast
                    SFExtensionMessageKey: try! JSONSerialization.jsonObject(with: data, options: []) as! NSDictionary
                ]
            } else {
                userInfo = nil
            }
        }
    }

    convenience init(data: Data) {
        self.init()
        self.data = data
    }
}
