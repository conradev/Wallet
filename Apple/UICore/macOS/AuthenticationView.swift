import LocalAuthenticationEmbeddedUI
import SwiftUI

struct AuthenticationView: NSViewRepresentable {
    class Coordinator {
        let context: LAContext

        init(_ context: LAContext) {
            self.context = context
        }
    }

    static var functional: Bool {
        // TODO: Pending resolution of FB10013283
        ProcessInfo.processInfo.isOperatingSystemAtLeast(.init(majorVersion: 16, minorVersion: 0, patchVersion: 0))
    }

    var context: LAContext

    func makeNSView(context: Context) -> LAAuthenticationView {
        LAAuthenticationView(context: context.coordinator.context)
    }

    func updateNSView(_ nsView: LAAuthenticationView, context: Context) { }

    func makeCoordinator() -> Coordinator {
        Coordinator(context)
    }
}
