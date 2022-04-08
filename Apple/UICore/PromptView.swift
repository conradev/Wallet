import SwiftUI
import WalletCore

public struct PromptView: View {
    public var viewModel: AnyPromptViewModel

    public var body: some View {
        switch viewModel {
        case let permissionViewModel as PermissionPromptViewModel:
            PermissionPromptView(observable: permissionViewModel.observable)
        case let signDataViewModel as SignDataPromptViewModel:
            SignDataPromptView(observable: signDataViewModel.observable)
        default:
            EmptyView()
        }
    }

    public init(viewModel: AnyPromptViewModel) {
        self.viewModel = viewModel
    }
}
