import SwiftUI
import WalletCore

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
