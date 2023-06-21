import SwiftUI
import WalletUICore

struct BrowserView: View {
    @ObservedObject
    var observable: BrowserViewModel.Observable = KoinApplication.observable(BrowserViewModel.self)
    var viewModel: BrowserViewModel { observable.viewModel() }

    @State
    var visiblePrompt: Prompt?

    var body: some View {
        List(observable.prompts) { prompt in
            Button(prompt.id) {
                show(prompt: prompt)
            }
        }
        .sheet(item: $visiblePrompt) { prompt in
            PromptView(viewModel: promptViewModel(prompt))
        }
    }

    private func show(prompt: Prompt) {
        PromptLocation(prompt).open()
    }

    private func promptViewModel(_ prompt: Prompt) -> AnyPromptViewModel {
        let viewModel = viewModel.viewModel(prompt: prompt, host: nil)
        viewModel.dismiss = {
            Task { @MainActor in self.visiblePrompt = nil }
        }
        return viewModel
    }
}

extension Prompt: Identifiable { }

extension BrowserViewModel: KotlinViewModel {
    public final class Observable: KotlinObservableObject {
        @Published
        var prompts: [Prompt] = []
    }

    public static let bindings: [KotlinBinding] = [
        .init(\BrowserViewModel.prompts, \.promptsFlow, \.prompts)
    ]
}
