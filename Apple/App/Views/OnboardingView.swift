import SwiftUI
import WalletUICore

struct OnboardingView: View {
    private class Callbacks {
        var importAction: (() -> Void)?
    }

#if os(macOS)
    static let titleFont = NSFont(
        descriptor: .preferredFontDescriptor(forTextStyle: .largeTitle).withSymbolicTraits(.bold),
        size: 0
    )!
#else
    static let titleFont = UIFont(
        descriptor: .preferredFontDescriptor(withTextStyle: .largeTitle).withSymbolicTraits(.traitBold)!,
        size: 0
    )
#endif

    @StateObject private var observable = KoinApplication.observable(OnboardingViewModel.self)
    private var viewModel: OnboardingViewModel { observable.viewModel() }
    @StateObject private var welcomeViewObservable = KoinApplication.observable(WelcomeViewModel.self)
    @StateObject private var importViewObservable = KoinApplication.observable(ImportViewModel.self)

#if os(macOS)
    @State private var callbacks = Callbacks()
    @State private var isValid = false
#endif

#if os(macOS)
    var body: some View {
        WizardView({ view(for: observable.current) }, next: next, back: back)
            .frame(minWidth: 600, minHeight: 400)
    }
#else
    var body: some View {
        NavigationStackView(OnboardingViewModel.Screen.self, stack: $observable.screens) { screen in
            view(for: screen)
        }
    }
#endif

#if os(macOS)
    private var back: (() -> Void)? {
        guard observable.screens.count > 1 else { return nil }
        return { viewModel.back() }
    }

    private var next: (() -> Void)? {
        switch observable.current {
        case .welcome:
            return welcomeAction(option: welcomeViewObservable.selectedOption)
        case .importPhrase:
            if isValid {
                return callbacks.importAction
            } else {
                return nil
            }
        default:
            return nil
        }
    }
#endif

    @ViewBuilder
    private func view(for screen: OnboardingViewModel.Screen) -> some View {
        switch screen {
        case .welcome:
            #if os(macOS)
            WelcomeView(observable: welcomeViewObservable)
            #else
            WelcomeView(observable: welcomeViewObservable) { welcomeAction(option: $0)?() }
            #endif
        case .importPhrase:
            #if os(macOS)
            ImportView(observable: importViewObservable, isValid: $isValid) { action in
                callbacks.importAction = action
            }
            #else
            ImportView(observable: importViewObservable)
            #endif
        default:
            EmptyView()
        }
    }

    private func welcomeAction(option: WelcomeViewModel.Option) -> (() -> Void)? {
        switch option {
        case .importPhrase:
            return { viewModel.import() }
        default:
            return nil
        }
    }
}

extension OnboardingViewModel: KotlinViewModel {
    public final class Observable: KotlinObservableObject {
        @Published var screens: [Screen] = [.welcome]

        var current: Screen {
            if let current = screens.last {
                return current
            }
            return .welcome
        }
    }

    public static let bindings: [KotlinBinding] = [
        .init(\OnboardingViewModel.screens, \.screensFlow, \.screens)
    ]
}

extension OnboardingViewModel.Screen: KotlinCaseIterable { }
