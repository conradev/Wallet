import Cocoa
import SwiftUI
import WalletCore

protocol MainWindowControllerDelegate: AnyObject {
    func mainWindowDidClose(_ sender: Any)
}

class MainWindowController: NSWindowController, NSWindowDelegate {
    weak var delegate: MainWindowControllerDelegate?

    init(observable: MainViewModel.Observable) {
        let window = NSWindow(
            contentViewController: NSHostingController(
                rootView: MainSplitView(observable: observable)
            )
        )
        window.styleMask.update(with: .fullSizeContentView)
        window.titleVisibility = NSWindow.TitleVisibility.hidden
        window.titlebarAppearsTransparent = true
        window.toolbarStyle = .unified
        window.toolbar = NSToolbar()

        super.init(window: window)

        window.delegate = self
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    // MARK: NSWindowDelegate

    func windowShouldClose(_ sender: NSWindow) -> Bool {
        delegate?.mainWindowDidClose(sender)
        return true
    }
}
