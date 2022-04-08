import AppKit
import Foundation

class SheetOverlayWindowController: NSWindowController {
    private class BackgroundViewController: NSViewController {
        override func loadView() {
            view = NSView()
        }
    }

    let backingWindow: Window
    let sheetViewController: NSViewController
    private let backgroundViewController: BackgroundViewController

    private var observer: Task<Void, Never>?

    required init(backingWindow: Window, sheetViewController: NSViewController) {
        self.backingWindow = backingWindow
        self.sheetViewController = sheetViewController
        self.backgroundViewController = BackgroundViewController()

        let window = NSWindow(contentViewController: backgroundViewController)
        window.styleMask.subtract([.closable, .miniaturizable, .resizable])
        window.titleVisibility = .hidden
        window.titlebarAppearsTransparent = true
        window.backgroundColor = .clear
        window.isOpaque = false
        window.hasShadow = false
        window.collectionBehavior = [.transient, .fullScreenNone, .ignoresCycle]
        window.ignoresMouseEvents = true
        super.init(window: window)

        update()
        observer = Task { @MainActor [weak self] in
            for await _ in backingWindow.updates(for: [
                .didMove,
                .didResize,
                .didChangeOrder,
                .isOrderedOut,
                .isTerminated,
                .isVisible,
                .isInvisible
            ]) {
                self?.update()
            }
        }
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func present() {
        backgroundViewController.presentAsSheet(sheetViewController)
        update()

        DispatchQueue.main.async {
            NSRunningApplication(processIdentifier: self.backingWindow.processIdentifier)?
                .activate()
        }
    }

    func dismiss() {
        backgroundViewController.dismiss(sheetViewController)
        DispatchQueue.main.async {
            self.close()
        }
    }

    private func update() {
        guard let backingWindow = Window(id: backingWindow.id), backingWindow.isVisible else {
            dismiss()
            close()
            return
        }

        guard let window = window, let screen = window.screen else { return }
        window.setFrame(backingWindow.frame(in: screen), display: false, animate: false)
        window.orderFrontRegardless()
    }

    deinit {
        observer?.cancel()
    }
}
