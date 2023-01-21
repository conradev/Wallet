import WalletUICore
import XCTest

class AppTests: XCTestCase {
    override func setUpWithError() throws {
        try super.setUpWithError()

        continueAfterFailure = false
    }

    func testImportPhrase() throws {
        let application = XCUIApplication()
        application.launchArguments = ["--reset-accounts"]
        application.launch()

        application.buttons[.welcomeOption(.importPhrase)].tap()

        #if os(macOS)
        application.buttons[.wizardNextButton].tap()
        #endif

        application.textViews[.importPhraseField].tap()
        application.typeText("ensure maximum account glue sugar chicken radio aerobic decide fan venue own")

        #if os(macOS)
        application.buttons[.wizardNextButton].tap()
        #else
        application.buttons[.importPhraseButton].tap()
        #endif

        XCTAssertTrue(application.descendants(matching: .any)[.mainView(.balance)].exists)
    }
}

extension XCUIElementQuery {
    subscript(key: AccessbilityID) -> XCUIElement {
        self[key.description]
    }
}
