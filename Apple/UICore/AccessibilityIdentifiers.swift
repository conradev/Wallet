import SwiftUI
import WalletCore

public enum AccessbilityID: CustomStringConvertible {
    case welcomeOption(WelcomeViewModel.Option)
    case wizardNextButton
    case importPhraseField
    case importPhraseButton
    case mainView(MainViewModel.Tab)

    public var description: String {
        switch self {
        case let .welcomeOption(option):
            return "welcome-\(option.name)"
        case .wizardNextButton:
            return "wizardNext"
        case .importPhraseField:
            return "importPhraseField"
        case .importPhraseButton:
            return "importPhraseButton"
        case let .mainView(tab):
            return "mainView-\(tab.name)"
        }
    }
}

extension View {
    public func accessibilityIdentifier(
        _ id: AccessbilityID
    ) -> ModifiedContent<Self, AccessibilityAttachmentModifier> {
        accessibilityIdentifier(id.description)
    }
}

#if os(macOS)
extension NSView {
    public func setAccessibilityIdentifier(_ identifier: AccessbilityID) {
        setAccessibilityIdentifier(identifier.description)
    }
}
#else
extension UIView {
    public func setAccessibilityIdentifier(_ identifier: AccessbilityID) {
        accessibilityIdentifier = identifier.description
    }
}
#endif
