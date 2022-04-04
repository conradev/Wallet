@_exported import Shared

public typealias KoinApplication = Koin_coreKoinApplication
public typealias Koin = Koin_coreKoin

extension Koin: ObservableObject { }

extension Array where Element: AnyObject {
    public init(_ kotlin: KotlinArray<Element>) {
        self = (0..<kotlin.size).map { kotlin.get(index: $0)! }
    }
}

public protocol KotlinCaseIterable: CaseIterable, AnyObject {
    associatedtype Value: AnyObject

    static func values() -> KotlinArray<Value>
}

public func kotlinCaseUnreachable<T: KotlinCaseIterable>(_ value: T) -> Never {
    fatalError("Received unexpected value \(value) when switching over \(type(of: value))")
}

extension KotlinCaseIterable {
    public static var allCases: [Value] {
        Array(values())
    }
}

extension KoinApplication {
    public static let shared = companion.start(
        applicationGroup: Wallet.appGroupIdentifier,
        subsystem: Logger.subsystem
    )

    public static func start() -> KoinApplication {
        shared
    }
}

extension KoinApplication {
    static let keyPaths: [PartialKeyPath<Koin>] = [
        \Koin.mainViewModel,
        \Koin.importViewModel,
        \Koin.welcomeViewModel,
        \Koin.onboardingViewModel
    ]

    public static func inject<T>() -> T {
        for partialKeyPath in keyPaths {
            guard let keyPath = partialKeyPath as? KeyPath<Koin, T> else { continue }
            return shared.koin[keyPath: keyPath]
        }

        fatalError("\(T.self) is not registered with KoinApplication")
    }
}
