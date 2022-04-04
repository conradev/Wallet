import Foundation

enum MessageType: String, Codable {
    case debug
    case notice
    case warning
    case error
}

struct Message: CustomStringConvertible {
    var type: MessageType

    var file: URL
    var title: String?
    var column: Int?
    var endColumn: Int?
    var line: Int?
    var endLine: Int?

    var message: String

    var description: String {
        var entry = [("file", file.path)]
        if let title = title {
            entry.append(("title", title))
        }
        if let line = line {
            entry.append(("line", "\(line)"))
        }
        if let endLine = endLine {
            entry.append(("endLine", "\(endLine)"))
        }
        if let column = column {
            entry.append(("col", "\(column)"))
        }
        if let endColumn = endColumn {
            entry.append(("endColumn", "\(endColumn)"))
        }

        return "::\(type.rawValue) \(entry.map { "\($0.0)=\($0.1)" }.joined(separator: ","))::\(message)"
    }
}
