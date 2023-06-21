import SwiftUI

public struct BalancesView: View {
    @ObservedObject var observable: BalancesViewModel.Observable
    var viewModel: BalancesViewModel { observable.viewModel() }

    public var body: some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                Text(observable.accountName)
                    .font(.title.monospaced())
                    .truncationMode(.middle)
                    .lineLimit(1)
                    .frame(maxWidth: 200)
                Text(observable.totalBalance)
                    .font(.title)
                ForEach(observable.assets) { asset in
                    AssetBalanceView(asset: asset)
                }
            }
        }
    }

    public init(observable: BalancesViewModel.Observable = KoinApplication.observable(BalancesViewModel.self)) {
        self.observable = observable
    }
}

extension BalancesViewModel: KotlinViewModel {
    public final class Observable: KotlinObservableObject {
        var assets: [Asset] = []
        var accountName = ""
        var totalBalance = ""
    }

    public static let bindings: [KotlinBinding] = [
        .init(\BalancesViewModel.assets, \.assetsFlow, \.assets),
        .init(\BalancesViewModel.accountName, \.accountNameFlow, \.accountName),
        .init(\BalancesViewModel.totalBalance, \.totalBalanceFlow, \.totalBalance)
    ]
}

extension Asset: Identifiable {
    public var id: String { balance.currency.code.code }
}

#if DEBUG
struct BalancesView_Previews: PreviewProvider {
    static var previews: some View {
        BalancesView(observable: PreviewMocks[\.balancesViewModel])
            .previewAsScreen()
    }
}
#endif
