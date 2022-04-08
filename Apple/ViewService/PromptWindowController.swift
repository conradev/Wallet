import AppKit
import SwiftUI
import WalletUICore

class PromptWindowController: SheetOverlayWindowController, NSWindowDelegate {
    let viewModel: AnyPromptViewModel

    required init(backingWindow: Window, viewModel: AnyPromptViewModel) {
        let sheetViewController = NSHostingController(rootView: PromptView(viewModel: viewModel))

        self.viewModel = viewModel
        super.init(backingWindow: backingWindow, sheetViewController: sheetViewController)

        window?.delegate = self
        viewModel.dismiss = {
            Task { @MainActor in self.dismiss() }
        }
    }

    @available(*, unavailable)
    required init(backingWindow: Window, sheetViewController: NSViewController) {
        fatalError("init(backingWindow:sheetViewController:) has not been implemented")
    }

    // MARK: NSWindowDelegate

    func windowShouldClose(_ sender: NSWindow) -> Bool {
        viewModel.cancel()
        return true
    }
}
