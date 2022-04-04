import AnyCodable
import DictionaryCoding
import Foundation
import OSLog
import WalletCore

struct Message: Codable { }

struct MessageResponse: Codable {
    var test123 = 123
}

func handle(message: Message) throws -> MessageResponse {
    MessageResponse()
}

let input = FileHandle.standardInput
let output = FileHandle.standardOutput
while true {
    let length = Int(input.readData(ofLength: 4).withUnsafeBytes { $0.load(as: UInt32.self) })
    let data = input.readData(ofLength: length)
    let message = try JSONDecoder().decode(Message.self, from: data)
    let response = try handle(message: message)

    let responseData = try JSONEncoder().encode(response)
    var responseLength = Data(count: 4)
    responseLength.withUnsafeMutableBytes { $0.storeBytes(of: UInt32(responseData.count), as: UInt32.self) }
    output.write(responseLength + responseData)
}
