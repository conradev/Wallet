import AppKit
import SwiftUI
import WalletCore

protocol OnboardingWindowControllerDelegate: AnyObject {
    func onboardingWindowDidClose(_ sender: Any)
}

class OnboardingWindowController: NSWindowController, NSWindowDelegate {
    weak var delegate: OnboardingWindowControllerDelegate?

    init() {
        let window = NSWindow(contentViewController: NSHostingController(rootView: OnboardingView()))
        window.styleMask.remove(NSWindow.StyleMask.miniaturizable)
        window.styleMask.remove(NSWindow.StyleMask.fullScreen)
        window.styleMask.remove(NSWindow.StyleMask.resizable)
        window.titleVisibility = NSWindow.TitleVisibility.hidden
        window.titlebarAppearsTransparent = true
        window.isMovableByWindowBackground = true

        super.init(window: window)

        window.delegate = self
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func windowShouldClose(_ sender: NSWindow) -> Bool {
        delegate?.onboardingWindowDidClose(sender)
        return true
    }
}
