import Foundation

struct XCType: Equatable, Codable {
    enum CodingKeys: String, CodingKey {
        case name = "_name"
    }

    static let string = Self(String.self)
    static let integer = Self(Int.self)
    static let date = Self(Date.self)
    static let array = Self("Array")

    var name: String

    init(_ name: String) {
        self.name = name
    }

    init<T>(_ type: T.Type) {
        self.name = String(describing: T.self)
    }
}

enum XCValue: Codable {
    case integer(Int)
    case date(Date)
    case string(String)
    case array([Self])
    case record(type: String, properties: [String: Self])

    enum RequiredCodingKeys: String, CodingKey {
        case type = "_type"
    }

    enum ScalarCodingKeys: String, CodingKey {
        case value = "_value"
    }

    enum ArrayCodingKeys: String, CodingKey {
        case values = "_values"
    }

    struct StringCodingKey: CodingKey {
        var stringValue: String
        var intValue: Int?

        init?(stringValue: String) {
            guard !stringValue.hasPrefix("_") else { return nil }
            self.stringValue = stringValue
        }

        init?(intValue: Int) {
            nil
        }
    }

    private static let dateFormatter: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return formatter
    }()

    init(from decoder: Decoder) throws {
        let xcType = try decoder.container(keyedBy: RequiredCodingKeys.self)
            .decode(XCType.self, forKey: .type)

        switch xcType {
        case .array:
            let container = try decoder.container(keyedBy: ArrayCodingKeys.self)
            self = .array(try container.decode([Self].self, forKey: .values))
        case .string:
            let container = try decoder.container(keyedBy: ScalarCodingKeys.self)
            self = try .string(container.decode(String.self, forKey: .value))
        case .integer:
            let container = try decoder.container(keyedBy: ScalarCodingKeys.self)
            let string = try container.decode(String.self, forKey: .value)
            guard let integer = Int(string) else {
                throw DecodingError.dataCorrupted(
                    .init(
                        codingPath: container.codingPath + [ScalarCodingKeys.value],
                        debugDescription: "Unable to parse integer from \"\(string)\"",
                        underlyingError: nil
                    )
                )
            }
            self = .integer(integer)
        case .date:
            let container = try decoder.container(keyedBy: ScalarCodingKeys.self)
            let string = try container.decode(String.self, forKey: .value)
            guard let date = Self.dateFormatter.date(from: string) else {
                throw DecodingError.dataCorrupted(
                    .init(
                        codingPath: container.codingPath + [ScalarCodingKeys.value],
                        debugDescription: "Unable to parse date from \"\(string)\"",
                        underlyingError: nil
                    )
                )
            }
            self = .date(date)
        default:
            let container = try decoder.container(keyedBy: StringCodingKey.self)
            let values = try container.allKeys.map { ($0.stringValue, try container.decode(Self.self, forKey: $0)) }
            let properties = Dictionary(uniqueKeysWithValues: values)
            self = .record(type: xcType.name, properties: properties)
        }
    }

    func encode(to encoder: Encoder) throws {
        switch self {
        case let .array(values):
            var container = encoder.unkeyedContainer()
            try values.forEach { try container.encode($0) }
        case let .date(date):
            var container = encoder.singleValueContainer()
            try container.encode(date)
        case let .integer(integer):
            var container = encoder.singleValueContainer()
            try container.encode(integer)
        case let .string(string):
            var container = encoder.singleValueContainer()
            try container.encode(string)
        case let .record(type: _, properties: properties):
            var container = encoder.container(keyedBy: StringCodingKey.self)
            try properties.forEach { try container.encode($0.1, forKey: .init(stringValue: $0.0)!) }
        }
    }
}
