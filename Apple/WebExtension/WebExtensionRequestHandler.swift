import WalletCore

extension WebExtensionHost {
    static let app = KoinApplication.start()
}

extension BrowserMessageHost: WebMessageHandler { }

#if os(macOS)
extension ViewServiceConnection: WebMessageHandler { }
#endif

extension WebExtensionHost {
    static let shared: WebExtensionHost = {
        #if os(macOS)
        let handler: ViewServiceConnection = app.inject()
        #else
        let handler: BrowserMessageHost = app.inject()
        #endif

        return WebExtensionHost(handler: handler, promptHost: app.inject())
    }()
}

class WebExtensionRequestHandler: NSObject, NSExtensionRequestHandling {
    func beginRequest(with context: NSExtensionContext) {
        WebExtensionHost.shared.beginRequest(with: context)
    }
}
