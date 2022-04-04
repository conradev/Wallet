import Foundation

struct ActionsInvocationRecord: Codable {
    var issues: ResultIssueSummaries?
    var metrics: ResultMetrics?
}

struct ResultIssueSummaries: Codable {
    var testFailureSummaries: [TestFailureIssueSummary]?
    var warningSummaries: [IssueSummary]?
}

struct ResultMetrics: Codable {
    var testsCount: Int?
    var testsFailedCount: Int?
    var warningCount: Int?
}

struct IssueType: ExpressibleByStringLiteral, Equatable, Codable {
    static let swiftCompilerWarning: Self = "Swift Compiler Warning"
    static let warning: Self = "Warning"
    static let uncategorized: Self = "Uncategorized"

    var rawValue: String

    init(stringLiteral value: String) {
        self.rawValue = value
    }

    init(from decoder: Decoder) throws {
        rawValue = try decoder.singleValueContainer().decode(String.self)
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        try container.encode(rawValue)
    }
}

struct IssueSummary: Codable {
    var issueType: IssueType
    var message: String
    var documentLocationInCreatingWorkspace: DocumentLocation?
}

struct TestFailureIssueSummary: Codable {
    var message: String
    var testCaseName: String
    var producingTarget: String
    var documentLocationInCreatingWorkspace: DocumentLocation?
    var issueType: IssueType
}

struct DocumentLocation: Codable {
    var concreteTypeName: String
    var url: URL
}

struct ParsedDocumentLocation {
    var fileURL: URL
    var line: Int?
    var endLine: Int?
    var column: Int?
    var endColumn: Int?

    init?(url: URL) {
        guard
            var components = URLComponents(url: url, resolvingAgainstBaseURL: false),
            let fragment = components.fragment else { return nil }

        components.fragment = nil
        guard let strippedURL = components.url else { return nil }
        self.fileURL = strippedURL

        let pairs: [(String, String)] = fragment.split(separator: "&")
            .compactMap { pair in
                let components = pair.split(separator: "=")
                guard components.count == 2 else { return nil }
                return (String(components[0]), String(components[1]))
            }
        let values = Dictionary(uniqueKeysWithValues: pairs)

        if let column = values["StartingColumnNumber"].map({ Int($0) }) {
            self.column = column
        }
        if let endColumn = values["EndingColumnNumber"].map({ Int($0) }) {
            self.endColumn = endColumn
        }
        if let line = values["StartingLineNumber"].map({ Int($0) }) {
            self.line = line
        }
        if let endLine = values["EndingLineNumber"].map({ Int($0) }) {
            self.endLine = endLine
        }
    }
}
