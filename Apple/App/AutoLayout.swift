import CoreGraphics
import SwiftUI

#if os(macOS)
import AppKit
#else
import UIKit
#endif

#if os(macOS)
typealias NativeView = NSView
typealias NativeLayoutPriority = NSLayoutConstraint.Priority
typealias NativeViewControllerRepresentable = NSViewControllerRepresentable
#else
typealias NativeView = UIView
typealias NativeLayoutPriority = UILayoutPriority
typealias NativeViewControllerRepresentable = UIViewControllerRepresentable
#endif

extension NSLayoutConstraint {
    func priority(_ priority: NativeLayoutPriority) -> Self {
        self.priority = priority
        return self
    }
}
