import Foundation
import SwiftUI

struct LargeFilledButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        RoundedRectangle(cornerRadius: 15, style: .continuous)
            .fill()
            .foregroundColor(color(configuration))
            .overlay(configuration.label.foregroundColor(.white), alignment: .center)
            .overlay(overlay(configuration))
            .frame(minWidth: 330, minHeight: 48, maxHeight: 48, alignment: .center)
    }

    @ViewBuilder
    func overlay(_ configuration: Configuration) -> some View {
        if configuration.isPressed {
            RoundedRectangle(cornerRadius: 15, style: .continuous)
                .fill()
                .foregroundColor(Color(.displayP3, white: 0, opacity: 0.35))
        }
    }

    func color(_ configuration: Configuration) -> Color {
        switch configuration.role {
        case .some(.destructive):
            return .red
        case .none, .some:
            return .accentColor
        }
    }
}

extension ButtonStyle where Self == LargeFilledButtonStyle {
    static var largedFilled: LargeFilledButtonStyle {
        LargeFilledButtonStyle()
    }
}
