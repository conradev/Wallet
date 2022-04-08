import Cocoa
import SwiftUI
import WalletUICore

@main
class AppDelegate: NSObject, NSApplicationDelegate, NSWindowDelegate {
    let app = KoinApplication.start()

    @LazyKoin
    var server: ViewServiceServer

    @LazyKoin
    var host: BrowserPromptHost

    func applicationDidFinishLaunching(_ aNotification: Notification) {
        server.start()

        let delegate = self
        host.handler = { prompt in
            Task { @MainActor in delegate.handle(prompt: prompt) }
        }
    }

    func applicationWillTerminate(_ aNotification: Notification) {
    }

    func applicationSupportsSecureRestorableState(_ app: NSApplication) -> Bool {
        return true
    }

    func windowShouldClose(_ sender: NSWindow) -> Bool {
        return false
    }

    private func handle(prompt: Prompt) {
        let bounds = prompt.frame.rect
        guard let window = Window.onScreen
            .filter({ $0.processIdentifier == prompt.pageId.browserPid })
            .min(by: { $0.bounds.distance(bounds) < $1.bounds.distance(bounds) }) else {
            Logger.default.error("Could not find target window to attach to for prompt \(prompt.id)")
            return
        }

        let viewModel = host.viewModel(koin: KoinApplication.shared.koin, prompt: prompt, host: nil)
        let windowController = PromptWindowController(backingWindow: window, viewModel: viewModel)
        windowController.present()
    }
}

extension Frame {
    var rect: CGRect {
        CGRect(x: x, y: y, width: width, height: height)
    }
}

extension CGRect {
    func distance(_ other: CGRect) -> CGFloat {
        abs(width - other.width) +
        abs(height - other.height) +
        abs(minX - other.minX) +
        abs(minY - other.minY)
    }
}
