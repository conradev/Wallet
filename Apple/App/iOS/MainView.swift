import SwiftUI
import WalletCore

struct MainView: View {
    @Environment(\.horizontalSizeClass)
    var horizontalSizeClass

    @StateObject
    var observable: MainViewModel.Observable = KoinApplication.observable()

    var body: some View {
        content
            .fullScreenCover(isPresented: $observable.showOnboarding) {
                OnboardingView()
            }
    }

    @ViewBuilder
    var content: some View {
        if horizontalSizeClass == .regular {
            MainSplitView(observable: observable)
        } else {
            MainTabView(observable: observable)
        }
    }
}

struct MainTabView: View, MainViewProvider {
    @ObservedObject
    var observable: MainViewModel.Observable

    var body: some View {
        TabView(selection: $observable.selectedTab) {
            ForEach(MainViewModel.Tab.allCases, id: \.self) { tab in
                view(for: tab)
                    .tabItem { label(for: tab) }
                    .tag(tab)
            }
        }
    }

    @ViewBuilder
    func label(for tab: MainViewModel.Tab) -> some View {
        Label(tab.title, systemImage: tab.systemImage)
    }
}
