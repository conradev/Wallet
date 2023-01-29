import SwiftUI
import WalletUICore

protocol MainViewProvider {
    associatedtype Content: View

    func view(for tab: MainViewModel.Tab) -> Content
}

extension MainViewProvider {
    func view(for tab: MainViewModel.Tab) -> some View {
        contentView(for: tab)
            .accessibilityIdentifier(.mainView(tab))
    }

    @ViewBuilder
    func contentView(for tab: MainViewModel.Tab) -> some View {
        switch tab {
        case .utility:
            BrowserView()
        default:
            Text(tab.title)
        }
    }
}

struct MainSplitView: View, MainViewProvider {
    @ObservedObject
    var observable: MainViewModel.Observable

    var body: some View {
        NavigationView {
            List {
                ForEach(MainViewModel.Tab.allCases) { tab in
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
    public final class Observable: KotlinObservableObject {
        @Published
        var showOnboarding = false

        @Published
        var selectedTab: Tab = .balance
    }

    public static let bindings: [KotlinBinding] = [
        .init(\MainViewModel.showOnboarding, \.showOnboardingNative, \.showOnboarding) { $0.boolValue },
        .init(\MainViewModel.selectedTab, \.selectedTabNative, \.selectedTab)
    ]
}

extension MainViewModel.Tab: KotlinEnum {
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
