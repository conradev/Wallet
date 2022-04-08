import Combine
import SwiftUI
import UIKit
import WalletCore

class MainSceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }

        let window = UIWindow(windowScene: windowScene)
        window.rootViewController = UIHostingController(rootView: MainView())
        window.makeKeyAndVisible()
        self.window = window

        let activationConditions = UISceneActivationConditions()
        activationConditions.canActivateForTargetContentIdentifierPredicate = NSPredicate(
            format: "NOT self BEGINSWITH \"\(PromptLocation.directURL.absoluteString)\""
        )
        activationConditions.canActivateForTargetContentIdentifierPredicate = NSPredicate(value: false)
        scene.activationConditions = activationConditions
    }
}
