#if os(macOS)

import Foundation
import WalletCore

extension NSExtensionContext {
    private static let key = UnsafeRawPointer(bitPattern: UInt.random(in: 1..<UInt.max))!

    var connection: NSXPCConnection {
        if let connection = objc_getAssociatedObject(self, Self.key) as? NSXPCConnection {
            return connection
        }

        let connection = findInstanceVariable(NSXPCConnection.self, depth: 2).first!
        objc_setAssociatedObject(self, Self.key, connection, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
        return connection
    }
}

#endif
