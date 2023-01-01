#if DEBUG
import SwiftUI

extension ColorScheme {
    var previewName: String {
        String(describing: self).capitalized
    }
}

private struct DeviceNamesKey: EnvironmentKey {
#if os(macOS)
    static let defaultValue = ["Mac"]
#else
    static let defaultValue = [
        "iPhone SE (3rd generation)",
        "iPhone 14 Pro Max",
        "iPad (10th generation)"
    ]
#endif
}

extension EnvironmentValues {
    var deviceNames: [String] {
        get { self[DeviceNamesKey.self] }
        set { self[DeviceNamesKey.self] = newValue }
    }
}

struct ScreenPreview<Screen: View>: View {
    @Environment(\.deviceNames)
    var deviceNames: [String]

    var screen: Screen

    var body: some View {
        ForEach(deviceNames, id: \.self) { device in
            #if os(macOS)
            screen
                .previewDevice(PreviewDevice(rawValue: device))
                .previewDisplayName(device)
            #else
            NavigationView {
                screen
                    .navigationBarTitle("")
                    .navigationBarHidden(true)
            }
            .navigationViewStyle(StackNavigationViewStyle())
            .previewDevice(PreviewDevice(rawValue: device))
            .previewDisplayName(device)
            #endif
        }
    }
}

extension View {
    public func previewAsScreen() -> some View {
        ScreenPreview(screen: self)
    }
}
#endif
