import SwiftUI
import WalletUICore

struct BalancesView: View {
    @ObservedObject
    var observable: BalancesViewModel.Observable = KoinApplication.observable()

    var body: some View {
        LazyVStack(spacing: 10) {
            Text(observable.viewModel.accountName)
                .font(.title.monospaced())
                .truncationMode(.middle)
                .lineLimit(1)
                .frame(maxWidth: 200)
            Text(observable.totalBalance)
                .font(.title)
            ForEach(observable.balances, id: \.self) { balance in
                BalanceView(balance: balance)
            }
        }
    }
}

struct BalanceView: View {
    var balance: BalanceViewModel

    var body: some View {
        HStack {
            Text(balance.currencyName)
                .font(.headline)
            Spacer()
            VStack(alignment: .trailing) {
                Text(balance.formattedConvertedBalance)
                    .font(.body.monospaced())
                Text(balance.formattedBalance)
                    .font(.body.monospaced())
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 10)
                .fill(.background)
        )
        .padding(.horizontal)
    }
}

extension BalancesViewModel: KotlinViewModel {
    public final class Observable: KotlinObservableObject<BalancesViewModel> {
        var totalBalance: String = ""
        var balances: [BalanceViewModel] = []
    }

    public static let bindings: [KotlinBinding] = [
        .init(\BalancesViewModel.balances, \.balancesNative, \.balances),
        .init(\BalancesViewModel.totalBalance, \.totalBalanceNative, \.totalBalance)
    ]
}

#if DEBUG
struct BalancesView_Previews: PreviewProvider {
    static var previews: some View {
        BalancesView(observable: PreviewMocks[\.balancesViewModel])
            .previewAsScreen()
    }
}
#endif
