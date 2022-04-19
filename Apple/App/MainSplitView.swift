import SwiftUI
import WalletCore

protocol MainViewProvider {
    associatedtype Content: View

    func view(for tab: MainViewModel.Tab) -> Content
}

extension MainViewProvider {
    @ViewBuilder
    func view(for tab: MainViewModel.Tab) -> some View {
        Text(tab.title)
    }
}

struct MainSplitView: View, MainViewProvider {
    @ObservedObject
    var observable: MainViewModel.Observable

    var body: some View {
        NavigationView {
            List {
                ForEach(MainViewModel.Tab.allCases, id: \.self) { tab in
                    // swiftlint:disable:next multiline_arguments
                    NavigationLink(isActive: isActive(tab: tab)) {
                        view(for: tab)
                    } label: {
                        label(for: tab)
                    }
                }
            }
            .listStyle(.sidebar)
            #if os(macOS)
            .frame(minWidth: 150)
            #else
            .navigationTitle(Wallet.localizedAppName)
            #endif
        }
        #if os(macOS)
        .frame(minWidth: 600, minHeight: 400)
        #endif
    }

    @ViewBuilder
    func label(for tab: MainViewModel.Tab) -> some View {
        Label(tab.title, systemImage: tab.systemImage)
            .font(.title3)
            #if os(macOS)
            .padding(.vertical, 5)
            #endif
    }

    private func isActive(tab: MainViewModel.Tab) -> Binding<Bool> {
        .init {
            observable.selectedTab == tab
        } set: { newValue in
            guard newValue else { return }
            observable.selectedTab = tab
        }
    }
}

extension MainViewModel: KotlinViewModel {
    final class Observable: KotlinObservableObject<MainViewModel> {
        @Published
        var showOnboarding = false

        @Published
        var selectedTab: Tab = .balance
    }

    static let bindings: [KotlinBinding] = [
        .init(\MainViewModel.showOnboarding, \.showOnboardingFlowNative, \.showOnboarding) { $0.boolValue },
        .init(\MainViewModel.selectedTab, \.selectedTabNative, \.selectedTab)
    ]
}

extension MainViewModel.Tab: KotlinCaseIterable {
    var systemImage: String {
        switch self {
        case .balance:
            return "dollarsign.circle"
        case .collectibles:
            return "square.stack"
        case .transfer:
            return "arrow.up.arrow.down"
        case .utility:
            return "globe"
        case .transactions:
            return "person.crop.circle"
        default:
            kotlinCaseUnreachable(self)
        }
    }
}
