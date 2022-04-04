import SwiftUI
import WalletCore

protocol ImportViewControllerDelegate: AnyObject {
    func action(string: String)
    func clean(string: String) -> String
    func validate(string: String) -> Bool
    func accept(character: Character) -> Bool
}

struct ImportView: NativeViewControllerRepresentable {
    class Coordinator: ImportViewControllerDelegate {
        let parent: ImportView

        var viewModel: ImportViewModel {
            parent.observable.viewModel
        }

        init(parent: ImportView) {
            self.parent = parent
        }

        func action(string: String) {
            viewModel.`import`(phrase: string)
        }

        func clean(string: String) -> String {
            viewModel.clean(phrase: string)
        }

        func validate(string: String) -> Bool {
            let value = viewModel.validate(phrase: string)
            parent.isValid?.wrappedValue = value
            return value
        }

        func accept(character: Character) -> Bool {
            if let asciiValue = character.asciiValue {
                return viewModel.accept(character: UInt16(asciiValue))
            }
            return false
        }
    }

    var observable: ImportViewModel.Observable

    var isValid: Binding<Bool>?
    var action: ((@escaping () -> Void) -> Void)?

    #if os(macOS)
    func makeNSViewController(context: Context) -> ImportViewController {
        let viewModel = context.coordinator.viewModel
        let viewController = ImportViewController(headerTitle: viewModel.title, placeholder: viewModel.placeholder)
        viewController.delegate = context.coordinator
        action?(viewController.action)
        return viewController
    }

    func updateNSViewController(_ nsViewController: ImportViewController, context: Context) { }
    #else
    func makeUIViewController(context: Context) -> ImportViewController {
        let viewModel = context.coordinator.viewModel
        let viewController = ImportViewController(
            headerTitle: viewModel.title,
            placeholder: viewModel.placeholder,
            action: viewModel.action
        )
        viewController.delegate = context.coordinator
        return viewController
    }

    func updateUIViewController(_ uiViewController: ImportViewController, context: Context) { }
    #endif

    func makeCoordinator() -> Coordinator {
        Coordinator(parent: self)
    }
}

extension ImportViewModel: KotlinViewModel {
    final class Observable: KotlinObservableObject<ImportViewModel> { }

    static let bindings: [KotlinBinding] = []
}

#if DEBUG
struct ImportView_Previews: PreviewProvider {
    static var previews: some View {
        ImportView(observable: PreviewMocks[\.importViewModel])
            .previewAsScreen()
    }
}
#endif
