// swift-tools-version:5.5
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "XCResult",
    platforms: [.macOS(.v10_13)],
    products: [
        .executable(
            name: "xcresulttool-github",
            targets: ["XCResultGitHub"]
        )
    ],
    dependencies: [],
    targets: [
        .executableTarget(
            name: "XCResultGitHub",
            dependencies: []
        )
    ]
)
