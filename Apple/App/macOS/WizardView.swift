import SwiftUI

struct WizardView<Content: View>: View {
    var content: () -> Content
    var next: (() -> Void)?
    var back: (() -> Void)?

    var body: some View {
        VStack(spacing: 0) {
            content()
            navigationBar
        }
    }

    @ViewBuilder private var navigationBar: some View {
        Rectangle()
            .fill(.separator)
            .frame(minHeight: 1, maxHeight: 1)
        HStack(alignment: .center, spacing: 10) {
            Spacer()
            if let back = back {
                Button(action: back) {
                    Text("Back")
                        .frame(minWidth: 84)
                }
            }
            Button {
                next?()
            } label: {
                Text("Continue")
                    .frame(minWidth: 84)
            }
            .disabled(next == nil)
            .controlSize(.large)
            .keyboardShortcut(.defaultAction)
            .accessibilityIdentifier(.wizardNextButton)
        }
        .controlSize(.large)
        .padding(.horizontal)
        .frame(minHeight: 64)
    }

    init(@ViewBuilder _ content: @escaping () -> Content, next: (() -> Void)? = nil, back: (() -> Void)? = nil) {
        self.content = content
        self.next = next
        self.back = back
    }
}
