import DictionaryCoding
import Foundation
import OSLog
import SafariServices
import WalletCore

struct Message: Codable { }

struct MessageResponse: Codable {
    var test123 = 123
}

class WalletNativeMessageHandler: NSObject, NSExtensionRequestHandling {
    func handle(message: Message) async throws -> MessageResponse {
        MessageResponse()
    }

    func handle(dictionary: NSDictionary) async throws -> NSDictionary {
        let message = try DictionaryDecoder().decode(Message.self, from: dictionary)
        let response = try await handle(message: message)
        return try DictionaryEncoder().encode(response)
    }

    func handle(with items: [NSExtensionItem]) async throws -> [NSExtensionItem] {
        guard
            let item = items.first,
            let message = item.userInfo?[SFExtensionMessageKey] as? NSDictionary else { return [] }

        let response = try await handle(dictionary: message)

        let outputItem = NSExtensionItem()
        outputItem.userInfo = [SFExtensionMessageKey as NSString: response]
        return [outputItem]
    }

    func beginRequest(with context: NSExtensionContext) {
        Task {
            do {
                let items = try await handle(with: context.inputItems as? [NSExtensionItem] ?? [])
                context.completeRequest(returningItems: items, completionHandler: nil)
            } catch {
                Logger.default.log("Failed with \(error.localizedDescription, privacy: .public)")
                context.cancelRequest(withError: error)
            }
        }
    }
}
