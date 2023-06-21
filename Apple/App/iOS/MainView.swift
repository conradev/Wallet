import SwiftUI
import WalletCore

struct MainView: View {
    @Environment(\.horizontalSizeClass)
    var horizontalSizeClass

    @StateObject var observable: MainViewModel.Observable = KoinApplication.observable(MainViewModel.self)

    var body: some View {
        content
            .fullScreenCover(isPresented: $observable.showOnboarding) {
                OnboardingView()
            }
    }

    @ViewBuilder var content: some View {
        if horizontalSizeClass == .regular {
            MainSplitView(observable: observable)
        } else {
            MainTabView(observable: observable)
        }
    }
}
