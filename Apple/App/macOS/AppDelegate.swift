import Cocoa
import Combine
import SwiftUI
import WalletCore

@main
class AppDelegate: NSObject, NSApplicationDelegate, OnboardingWindowControllerDelegate, MainWindowControllerDelegate {
    let app = KoinApplication.start()

    let observable: MainViewModel.Observable = KoinApplication.observable()

    private lazy var onboardingWindowController: OnboardingWindowController = {
        let controller = OnboardingWindowController()
        controller.delegate = self
        return controller
    }()

    private lazy var mainWindowController: MainWindowController = {
        let controller = MainWindowController(observable: observable)
        controller.delegate = self
        return controller
    }()

    private var observers: [AnyCancellable] = []

    private lazy var observer: AnyCancellable = {
        let observable = self.observable
        return observable
            .objectWillChange
            .delay(for: 0, scheduler: RunLoop.main)
            .sink { [weak self] _ in
                guard !observable.showOnboarding else { return }
                self?.finishOnboarding()
            }
    }()

    func applicationDidFinishLaunching(_ notification: Notification) {
        LoginItem.viewService.start()

        do {
            try Chrome.write()
            try Firefox.write()
        } catch {
            print("Could not write browser manifests: \(error)")
        }

        startObservingOnboarding()

        if observable.showOnboarding {
            onboardingWindowController.showWindow(self)
        } else {
            mainWindowController.showWindow(self)
        }
    }

    private func startObservingOnboarding() {
        observers.append(
            observable
                .objectWillChange
                .delay(for: 0, scheduler: RunLoop.main)
                .sink { [weak self] in
                    guard let self = self else { return }
                    guard !self.observable.showOnboarding else { return }
                    self.finishOnboarding()
                }
        )
    }

    private func finishOnboarding() {
        guard let isVisible = onboardingWindowController.window?.isVisible, isVisible else { return }
        onboardingWindowController.close()
        mainWindowController.showWindow(self)
    }

    // MARK: OnboardingWindowControllerDelegate

    func onboardingWindowDidClose(_ sender: Any) {
        if let delegate = sender as? Self, delegate == self { return }
        NSApp.terminate(sender)
    }

    // MARK: MainWindowControllerDelegate

    func mainWindowDidClose(_ sender: Any) {
        NSApp.terminate(sender)
    }
}
