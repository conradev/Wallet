import Foundation
import WalletCore

protocol WebMessageHandler {
    func send(data: Foundation.Data, browserPid: Int32)
    func setReceiver(receiver: @escaping (Foundation.Data) -> Void)
}

class WebExtensionHost {
    let handler: WebMessageHandler
    let promptHost: BrowserPromptHost
    #if !os(macOS)
    let connection = AppConnection()
    #endif

    private var contexts: [NSExtensionContext] = []
    private var responses: [Foundation.Data] = []

    init(handler: WebMessageHandler, promptHost: BrowserPromptHost) {
        self.handler = handler
        self.promptHost = promptHost

        handler.setReceiver { [weak self] response in
            self?.handleResponse(data: response)
        }

        #if !os(macOS)
        promptHost.handler = { prompt in
            Task { [weak self] in try await self?.show(prompt: prompt) }
        }
        #endif
    }

    #if !os(macOS)
    private func show(prompt: Prompt) async throws {
        Task {
            do {
                try await connection.send(message: .prompt(prompt.id))
            } catch AppConnection.ConnectionError.requestTimedOut {
                guard let host = handler as? BrowserMessageHost else { return }
                host.openURL(prompt: prompt, url: PromptLocation(prompt).directURL.absoluteString)
            }
        }
    }
    #endif

    private func handleResponse(data: Foundation.Data) {
        responses.insert(data, at: 0)
        sendResponses(incoming: false)
    }

    private func sendResponses(incoming: Bool) {
        var success: Bool
        repeat { success = sendResponse(incoming: incoming) } while (success)
    }

    private func sendResponse(incoming: Bool) -> Bool {
        guard
            let context = contexts.last,
            let data = responses.last else {
            return false
        }

        _ = contexts.popLast()

        if contexts.isEmpty && !incoming {
            Logger.default.log("Using last extension context to request more extension contexts, sending placeholder")
            context.completeRequest(returningItems: [NSExtensionItem.placeholder])
            return true
        }

        _ = responses.popLast()
        context.completeRequest(returningItems: [NSExtensionItem(data: data)])
        return true
    }

    func beginRequest(with context: NSExtensionContext) {
        contexts.insert(context, at: 0)

        #if os(macOS)
        let browserPid = context.connection.processIdentifier
        #else
        let browserPid: Int32 = 0
        #endif

        context.inputItems
            .compactMap { $0 as? NSExtensionItem }
            .compactMap { item in
                if item.isPlacerholder {
                    Logger.default.log("Received placeholder context, ignoring message")
                    return nil
                } else {
                    return item.data
                }
            }
            .forEach { handler.send(data: $0, browserPid: browserPid) }

        sendResponses(incoming: true)
    }
}
