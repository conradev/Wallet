import SwiftUI
import UIKit
import WalletUICore

class PromptSceneDelegate: UIResponder, UIWindowSceneDelegate {
    private class PromptViewController: UIHostingController<PromptView> {
        let viewModel: AnyPromptViewModel

        required init(viewModel: AnyPromptViewModel) {
            self.viewModel = viewModel
            super.init(rootView: PromptView(viewModel: viewModel))
        }

        @available(*, unavailable)
        required init?(coder aDecoder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
    }

    var window: UIWindow?

    lazy var executor: BrowserPromptHost = { KoinApplication.shared.inject() }()

    private var viewModel: AnyPromptViewModel? {
        window?
            .rootViewController
            .flatMap { $0 as? PromptViewController }?
            .viewModel
    }

    private func viewController(
        for location: PromptLocation,
        in sceneSession: UISceneSession
    ) -> PromptViewController? {
        guard let prompt = executor.prompt(id: location.id) else {
            return nil
        }

        let viewModel = executor.viewModel(koin: KoinApplication.shared.koin, prompt: prompt, host: nil)
        viewModel.dismiss = {
            Task { @MainActor in
                let options = UIWindowSceneDestructionRequestOptions()
                options.windowDismissalAnimation = .decline
                UIApplication.shared.requestSceneSessionDestruction(sceneSession, options: options)
            }
        }
        return PromptViewController(viewModel: viewModel)
    }

    // MARK: UISceneDelegate

    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        guard let location = URLContexts.compactMap(PromptLocation.init).first else { return }
        window?.rootViewController = viewController(for: location, in: scene.session)
    }

    func scene(_ scene: UIScene, continue userActivity: NSUserActivity) {
        guard let location = PromptLocation(userActivity) else { return }
        window?.rootViewController = viewController(for: location, in: scene.session)
    }

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }

        let activationConditions = UISceneActivationConditions()
        activationConditions.canActivateForTargetContentIdentifierPredicate = NSPredicate(
            format: "self BEGINSWITH \"\(PromptLocation.directURL.absoluteString)\""
        )

        let window = UIWindow(windowScene: windowScene)
        if let location = PromptLocation(connectionOptions) {
            window.rootViewController = viewController(for: location, in: session)

            activationConditions.prefersToActivateForTargetContentIdentifierPredicate = NSPredicate(
                format: "self == \"\(location.universalLink.absoluteString)\""
            )
        }
        window.makeKeyAndVisible()
        self.window = window

        scene.activationConditions = activationConditions
    }

    func sceneDidEnterBackground(_ scene: UIScene) {
        viewModel?.cancel()
    }
}
