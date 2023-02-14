import SwiftUI
import WalletUICore
import WidgetKit

public struct BalancesWidgetView: View {
    @ObservedObject
    var observable: BalancesViewModel.Observable
    var viewModel: BalancesViewModel { observable.viewModel() }

    public var body: some View {
        VStack(spacing: 10) {
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

struct BalanceWidget: WalletWidget {
    static let kind = WidgetKind.balance

    let observable: BalancesViewModel.Observable = KoinApplication.observable(BalancesViewModel.self)

    var body: some WidgetConfiguration {
        IntentConfiguration(kind: kind, intent: DisplayAssetIntent.self, provider: BalanceTimelineProvider()) { _ in
            BalancesWidgetView(observable: observable)
        }
        .configurationDisplayName("Balance")
        .description("Display your current wallet balance")
        .supportedFamilies([
            .systemLarge
        ])
    }
}

struct BalanceTimelineProvider: AsyncIntentTimelineProvider {
    typealias Intent = DisplayAssetIntent

    struct Entry: TimelineEntry {
        var date: Date = Date.now
    }

    func placeholder(in context: Context) -> Entry {
        Entry()
    }

    func snapshot(for configuration: Intent, in context: Context) async -> Entry {
        Entry()
    }

    func timeline(for configuration: Intent, in context: Context) async -> Timeline<Entry> {
        Timeline(entries: [Entry()], policy: .never)
    }
}
