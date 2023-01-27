#if os(macOS)

import LocalAuthenticationEmbeddedUI
import SwiftUI

struct AuthenticationView: NSViewRepresentable {
    class Coordinator {
        let context: LAContext

        init(_ context: LAContext) {
            self.context = context
        }
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

#endif
