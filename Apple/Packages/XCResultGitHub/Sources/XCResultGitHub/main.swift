import Foundation

func usage() -> Never {
    print("xcresulttool-github <.xcresult>")
    exit(1)
}

guard CommandLine.arguments.count > 1 else { usage() }

let data: Data
do {
    let pipe = Pipe()
    let process = Process()
    process.standardOutput = pipe
    process.executableURL = URL(fileURLWithPath: "/usr/bin/xcrun")
    process.arguments = ["xcresulttool", "get", "--path", CommandLine.arguments[1], "--format", "json"]
    try process.run()
    process.waitUntilExit()
    data = pipe.fileHandleForReading.readDataToEndOfFile()
} catch {
    print("error: could not load file")
    exit(1)
}

let value = try JSONDecoder().decode(XCValue.self, from: data)
let record = try JSONDecoder().decode(ActionsInvocationRecord.self, from: try JSONEncoder().encode(value))

var messages: [Message] = []

if let testFailureSummaries = record.issues?.testFailureSummaries {
    messages += testFailureSummaries.compactMap { testFailure in
        guard
            let fileURL = testFailure.documentLocationInCreatingWorkspace?.url,
            let location = ParsedDocumentLocation(url: fileURL) else { return nil }

        return Message(
            type: .error,
            file: location.fileURL,
            title: "\(testFailure.testCaseName) failed",
            column: location.column,
            endColumn: location.endColumn,
            line: location.line,
            endLine: location.endLine,
            message: testFailure.message
        )
    }
}

if let warningSummaries = record.issues?.warningSummaries {
    messages += warningSummaries.compactMap { warning in
        guard
            let fileURL = warning.documentLocationInCreatingWorkspace?.url,
            let location = ParsedDocumentLocation(url: fileURL) else { return nil }

        let messageType: MessageType
        switch warning.issueType {
        case .swiftCompilerWarning, .warning:
            messageType = .warning
        default:
            messageType = .notice
        }

        return Message(
            type: messageType,
            file: location.fileURL,
            title: nil,
            column: location.column,
            endColumn: location.endColumn,
            line: location.line,
            endLine: location.endLine,
            message: warning.message
        )
    }
}

messages.forEach { print($0) }
