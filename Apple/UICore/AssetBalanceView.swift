import Shared
import SwiftUI

struct AssetBalanceView: View {
    var asset: Asset

    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(asset.balance.currency.name)
                    .font(.title2)
                Text(asset.balanceString)
                    .font(.body)
                    .foregroundColor(.gray)
            }
            Spacer()
            Text(asset.fiatBalanceString)
                .font(.body)
        }
        .padding()
        .frame(maxHeight: 64)
    }
}
