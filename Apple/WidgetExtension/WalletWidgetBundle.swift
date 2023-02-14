import WalletCore
import WidgetKit
import SwiftUI

@main
struct WalletWidgetBundle: WidgetBundle {
    let app = KoinApplication.start()

    var body: some Widget {
        BalanceWidget()
    }
}

enum WidgetKind: String {
    case balance = "balance"
}

protocol WalletWidget: Widget {
    static var kind: WidgetKind { get }
}

extension WalletWidget {
    var kind: String { Self.kind.rawValue }
}
