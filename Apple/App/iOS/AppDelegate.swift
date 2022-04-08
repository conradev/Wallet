import UIKit
import WalletCore

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    let app = KoinApplication.start()

    let connection = AppConnection()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        return true
    }

    func application(_ application: UIApplication, handleEventsForBackgroundURLSession identifier: String) async {
        do {
            let message = try await connection.handleEventFor(identifier: identifier)
            switch message {
            case .prompt(let id):
                PromptLocation(id: id).open()
            }
        } catch AppConnection.ConnectionError.requestTimedOut {
            Logger.default.log("App connection message \(identifier, privacy: .public) received after time out")
        } catch {
            Logger.default.log(
                "App connection message \(identifier, privacy: .public) failed with \(error.localizedDescription)"
            )
        }
    }

    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        return Scene(options: options).configuration(for: connectingSceneSession.role)
    }
}

enum Scene: String {
    case main
    case prompt

    init(options: UIScene.ConnectionOptions) {
        if PromptLocation(options) != nil {
            self = .prompt
            return
        }

        self = .main
    }

    func configuration(for role: UISceneSession.Role) -> UISceneConfiguration {
        UISceneConfiguration(name: rawValue, sessionRole: role)
    }
}
