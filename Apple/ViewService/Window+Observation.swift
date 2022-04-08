extension Window {
    private class Observer {
        private class Context {
            private(set) weak var observer: Observer?

            init(_ observer: Observer?) {
                self.observer = observer
            }
        }

        private static let callback: SLSNotifyCallback = { notification, buffer, _, context in
            guard let context = context else { return }
            guard
                let observer = Unmanaged<Context>.fromOpaque(context).takeUnretainedValue().observer else {
                let connection = SLSMainConnectionID()
                SLSRemoveConnectionNotifyProc(connection, Observer.callback, notification, context)
                _ = Unmanaged<Context>.fromOpaque(context).takeRetainedValue()
                return
            }

            let windowId = buffer.assumingMemoryBound(to: CGWindowID.self).pointee
            observer.handle(notification: notification, windowId: windowId)
        }

        private(set) var window: Window
        private let handler: (CGSWindowEvent) -> Void

        init(window: Window, notifications: [CGSWindowEvent], handler: @escaping (CGSWindowEvent) -> Void) {
            self.window = window
            self.handler = handler

            let connection = SLSMainConnectionID()

            for notification in notifications {
                let pointer = Unmanaged.passRetained(Context(self)).toOpaque()
                SLSRegisterConnectionNotifyProc(connection, Self.callback, notification, pointer)
            }

            var windowID = window.id
            SLSRequestNotificationsForWindows(connection, &windowID, 1)
        }

        private func handle(notification: CGSWindowEvent, windowId: CGWindowID) {
            guard window.id == windowId else { return }
            handler(notification)
        }
    }

    func updates(for notifications: [CGSWindowEvent]) -> AsyncStream<CGSWindowEvent> {
        AsyncStream { continuation in
            let observer = Observer(window: self, notifications: notifications) { notification in
                continuation.yield(notification)
            }
            continuation.onTermination = { _ in _ = observer }
        }
    }
}
