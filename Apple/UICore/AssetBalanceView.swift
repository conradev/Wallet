import Shared
import SwiftUI

public struct AssetBalanceView: View {
    public var asset: Shared.Asset

    public init(asset: Shared.Asset) {
        self.asset = asset
    }

    public var body: some View {
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
