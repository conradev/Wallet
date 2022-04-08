#if !os(macOS)

import Foundation
import notify
import OSLog

public final class AppConnection: NSObject, URLSessionTaskDelegate {
    public enum Message: Codable {
        case prompt(String)
    }

    public enum ConnectionError: Error {
        case requestTimedOut
        case invalidIdentifier
        case unknown
    }

    private static let timeout: DispatchTimeInterval = .seconds(2)
    private static let queue = DispatchQueue(label: "\(Wallet.appBundleIdentifier).\(AppConnection.self)")
    private static let cancelledMessagesKey = "\(AppConnection.self)CancelledMessages"

    private let appGroupIdentifier: String
    private let defaults: UserDefaults

    private var sessions: [String: URLSession] = [:]
    private var results: [String: UnsafeContinuation<Message, Error>] = [:]

    private var cancelled: Set<String> {
        get { Set(defaults.stringArray(forKey: Self.cancelledMessagesKey) ?? []) }
        set {
            if newValue.isEmpty {
                defaults.removeObject(forKey: Self.cancelledMessagesKey)
            } else {
                defaults.set(Array(newValue), forKey: Self.cancelledMessagesKey)
            }
        }
    }

    override public convenience init() {
        self.init(appGroupIdentifier: Wallet.appGroupIdentifier)
    }

    required init(appGroupIdentifier: String) {
        guard let defaults = UserDefaults(suiteName: appGroupIdentifier) else {
            fatalError("Unable to find user defaults for group \(appGroupIdentifier)")
        }

        self.appGroupIdentifier = appGroupIdentifier
        self.defaults = defaults
        super.init()
    }

    private static func notification(for identifier: String) -> String { "\(AppConnection.self)-\(identifier)" }

    private func configuration(for identifier: String) -> URLSessionConfiguration {
        let configuration = URLSessionConfiguration.background(withIdentifier: identifier)
        configuration.sharedContainerIdentifier = appGroupIdentifier
        return configuration
    }

    public func handleEventFor(identifier: String) async throws -> Message {
        if cancelled.contains(identifier) {
            cancelled.remove(identifier)
            throw ConnectionError.requestTimedOut
        }

        notify_post(Self.notification(for: identifier))

        Logger.default.info("Receiving message via app connection \(identifier, privacy: .public)")
        guard sessions[identifier] == nil else { throw ConnectionError.invalidIdentifier }
        return try await withUnsafeThrowingContinuation { continuation in
            let session = URLSession(
                configuration: configuration(for: identifier),
                delegate: self,
                delegateQueue: OperationQueue.main
            )
            session.finishTasksAndInvalidate()
            self.sessions[identifier] = session
            self.results[identifier] = continuation
        }
    }

    public func send(message: Message) async throws {
        Logger.default.info("Sending message via app connection: \(String(describing: message), privacy: .public)")

        let identifier = UUID().uuidString
        let session = URLSession(configuration: configuration(for: identifier))

        var request = URLRequest(url: URL(string: "http://127.0.0.2")!)
        request.httpBody = try JSONEncoder().encode(message)
        let task = session.downloadTask(with: request)
        task.resume()

        session
            .findInstanceVariable { $0.responds(to: #selector(NSUserActivity.invalidate)) }
            .forEach { $0.perform(#selector(NSUserActivity.invalidate)) }

        return try await withUnsafeThrowingContinuation { continuation in
            var tokenOut: Int32 = 0
            notify_register_dispatch(Self.notification(for: identifier), &tokenOut, Self.queue) { token in
                notify_cancel(token)
                continuation.resume(returning: ())
            }

            let token = tokenOut
            Self.queue.asyncAfter(deadline: .now() + Self.timeout) {
                notify_cancel(token)
                self.cancelled.insert(identifier)
                continuation.resume(throwing: ConnectionError.requestTimedOut)
            }
        }
    }

    // MARK: URLSessionDelegate

    public func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        guard let identifier = session.configuration.identifier, let result = results[identifier] else { return }
        do {
            guard let body = task.originalRequest?.httpBody else { throw ConnectionError.unknown }
            let message = try JSONDecoder().decode(Message.self, from: body)
            Logger.default.info("Received message via app connection \(String(describing: message), privacy: .public)")
            result.resume(returning: message)
        } catch {
            result.resume(throwing: error)
        }
    }

    public func urlSession(_ session: URLSession, didBecomeInvalidWithError error: Error?) {
        guard let identifier = session.configuration.identifier else { return }
        sessions[identifier] = nil
    }
}

#endif
