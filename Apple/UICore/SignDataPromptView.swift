import LocalAuthentication
import SwiftUI

public struct SignDataPromptView: View {
    @ObservedObject
    var observable: SignDataPromptViewModel.Observable
    var viewModel: SignDataPromptViewModel { observable.viewModel }

    private var symbolName: String {
        switch viewModel.biometryType {
        case .faceprint:
            return "faceid"
        case .fingerprint:
            return "touchid"
        default:
            return "globe"
        }
    }

    private var context: LAContext {
        viewModel.context.context
    }

    #if os(macOS)
    public var body: some View {
        VStack {
            HStack {
                Text("Sign Data")
                    .font(.title2)
                    .fontWeight(.semibold)
                Spacer()
                cancelButton
            }
            Group {
                Rectangle()
                    .fill(.separator)
                    .frame(minHeight: 1, maxHeight: 1)
                Spacer()
                Text(viewModel.title)
                Spacer()
                Text(viewModel.data)
                    .font(.title3.monospaced())
                Spacer()
                Text(viewModel.warning)
            }
            Spacer()
            if AuthenticationView.functional {
                AuthenticationView(context: context)
            } else {
                signButton
                    .keyboardShortcut(.defaultAction)
            }
            Spacer()
        }
        .padding()
        .frame(minWidth: 450, minHeight: 330)
        .onAppear {
            if AuthenticationView.functional { viewModel.sign() }
        }
    }
    #else
    public var body: some View {
        VStack {
            Spacer()
            Image(systemName: symbolName)
                .font(.system(size: 72))
            Spacer()
            Text(viewModel.title)
                .font(.headline)
            Spacer()
            Text(viewModel.data)
            .lineLimit(nil)
            Spacer()
            Text(viewModel.warning)
                .lineLimit(nil)
            signButton
                .buttonStyle(.floating)
            cancelButton
            .font(.headline)
            .padding(.vertical)
        }
        .padding()
        .padding(.horizontal)
    }
    #endif

    private var signButton: some View {
        Button(viewModel.signTitle) {
            viewModel.sign()
        }
    }

    private var cancelButton: some View {
        Button(viewModel.cancelTitle, role: .cancel) {
            viewModel.cancel()
        }
    }

    public init(observable: SignDataPromptViewModel.Observable) {
        self.observable = observable
    }
}

extension SignDataPromptViewModel: KotlinViewModel {
    public final class Observable: KotlinObservableObject<SignDataPromptViewModel> { }

    public static let bindings: [KotlinBinding] = []
}

#if DEBUG
struct SignPromptView_Previews: PreviewProvider {
    static var previews: some View {
        SignDataPromptView(observable: PreviewMocks[\.signDataPromptViewModel])
            .previewAsScreen()
    }
}
#endif
