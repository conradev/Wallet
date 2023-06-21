@_exported import Shared

public typealias KoinApplication = Koin_coreKoinApplication
public typealias Koin = Koin_coreKoin

extension Koin: ObservableObject { }

extension Array where Element: AnyObject {
    public init(_ kotlin: KotlinArray<Element>) {
        self = (0..<kotlin.size).map { kotlin.get(index: $0)! }
    }
}

public protocol KotlinCaseIterable: AnyObject {
    associatedtype Value: AnyObject

    static func values() -> KotlinArray<Value>
}

public protocol KotlinEnum: KotlinCaseIterable, Identifiable {
    var name: String { get }
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
        viewServiceName: Wallet.viewServiceBundleIdentifier,
        subsystem: Logger.subsystem
    )

    @discardableResult
    public static func start() -> KoinApplication {
        shared
    }
}

extension KoinApplication {
    private static let sharedKeyPaths: [PartialKeyPath<Koin>] = [
        \.mainViewModel,
        \.importViewModel,
        \.welcomeViewModel,
        \.onboardingViewModel,
        \.browserViewModel,
        \.balancesViewModel,
        \.browserMessageHost,
        \.browserPromptHost,
        \.appIndexer
    ]

    private static let previewKeyPaths: [PartialKeyPath<Koin>] = [
        \.permissionPromptViewModel,
        \.signDataPromptViewModel
    ]

#if os(macOS)
    private static let keyPaths = sharedKeyPaths + previewKeyPaths + [
        \.nativeMessageHost,
        \.viewServiceConnection,
        \.viewServiceServer
    ]
#else
    private static let keyPaths = sharedKeyPaths + previewKeyPaths
#endif

    public static func inject<T>() -> T {
        shared.inject()
    }

    public func inject<T>() -> T {
        for partialKeyPath in Self.keyPaths {
            guard let keyPath = partialKeyPath as? KeyPath<Koin, T> else { continue }
            return koin[keyPath: keyPath]
        }

        fatalError("\(T.self) is not registered with KoinApplication")
    }
}

@propertyWrapper
public struct LazyKoin<T> {
    public lazy var wrappedValue: T = { KoinApplication.shared.inject() }()

    public init() { }
}
