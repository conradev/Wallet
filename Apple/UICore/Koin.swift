import Combine
import KMPNativeCoroutinesCombine
import KMPNativeCoroutinesCore
import Shared
import SwiftUI
@_exported import WalletCore

public struct KotlinBinding {
    var bind: (AnyObject, AnyObject) -> [AnyCancellable]

    public init<ViewModel: KotlinViewModel, T, U>(
        _ viewModelKeyPath: KeyPath<ViewModel, U>,
        _ viewModelNativeKeyPath: KeyPath<ViewModel, NativeFlow<T, Error, KotlinUnit>>,
        _ valueKeyPath: ReferenceWritableKeyPath<ViewModel.Observable, U>,
        _ get: @escaping (T) -> U
    ) {
        bind = { foo, bar in
            guard
                let viewModel = foo as? ViewModel,
                let observable = bar as? ViewModel.Observable else { fatalError("Failed to bind \(foo) and \(bar)") }
            observable[keyPath: valueKeyPath] = viewModel[keyPath: viewModelKeyPath]
            let read = createPublisher(for: viewModel[keyPath: viewModelNativeKeyPath])
                .map(get)
                .replaceError(with: observable[keyPath: valueKeyPath])
                .receive(on: DispatchQueue.main)
                .assign(to: valueKeyPath, on: observable)
            return [read]
        }
    }

    public init<ViewModel: KotlinViewModel, T: Equatable>(
        _ viewModelKeyPath: KeyPath<ViewModel, T>,
        _ viewModelNativeKeyPath: KeyPath<ViewModel, NativeFlow<T, Error, KotlinUnit>>,
        _ valueKeyPath: ReferenceWritableKeyPath<ViewModel.Observable, T>
    ) {
        self.init(viewModelKeyPath, viewModelNativeKeyPath, valueKeyPath) { $0 }
    }

    public init<ViewModel: KotlinViewModel, T, U: Equatable>(
        _ viewModelKeyPath: ReferenceWritableKeyPath<ViewModel, T>,
        _ viewModelNativeKeyPath: KeyPath<ViewModel, NativeFlow<T, Error, KotlinUnit>>,
        _ valueKeyPath: ReferenceWritableKeyPath<ViewModel.Observable, U>,
        _ set: @escaping (T) -> U,
        _ get: @escaping (U) -> T
    ) {
        bind = { foo, bar in
            guard
                let viewModel = foo as? ViewModel,
                let observable = bar as? ViewModel.Observable else { fatalError("Failed to bind \(foo) and \(bar)") }
            observable[keyPath: valueKeyPath] = set(viewModel[keyPath: viewModelKeyPath])
            let read = createPublisher(for: viewModel[keyPath: viewModelNativeKeyPath])
                .map(set)
                .replaceError(with: observable[keyPath: valueKeyPath])
                .receive(on: DispatchQueue.main)
                .assign(to: valueKeyPath, on: observable)
            let write = observable
                .objectWillChange
                .delay(for: 0, scheduler: RunLoop.main)
                .map { observable[keyPath: valueKeyPath] }
                .sink { viewModel[keyPath: viewModelKeyPath] = get($0) }
            return [read, write]
        }
    }

    public init<ViewModel: KotlinViewModel, T: Equatable>(
        _ viewModelKeyPath: ReferenceWritableKeyPath<ViewModel, T>,
        _ viewModelNativeKeyPath: KeyPath<ViewModel, NativeFlow<T, Error, KotlinUnit>>,
        _ valueKeyPath: ReferenceWritableKeyPath<ViewModel.Observable, T>
    ) {
        self.init(viewModelKeyPath, viewModelNativeKeyPath, valueKeyPath, { $0 }, { $0 })
    }
}

open class KotlinObservableObject: ObservableObject {
    public var viewModelStorage: Any?
    public var bindings: [AnyCancellable] = []

    public required init() {
    }

    public func viewModel<T>() -> T {
        guard let viewModel = viewModelStorage as? T else {
            fatalError("Failed to cast viewModel to correct type")
        }
        return viewModel
    }
}

public protocol KotlinViewModel: ObservableObject {
    associatedtype Observable: KotlinObservableObject

    static var bindings: [KotlinBinding] { get }
}

extension KotlinViewModel {
    public var observable: Observable {
        let observable = Observable()
        observable.viewModelStorage = self
        bind(to: observable)
        return observable
    }

    func bind(to observable: Observable) {
        observable.bindings += Self.bindings.flatMap { $0.bind(self, observable) }
    }
}

extension KoinApplication {
    public static func observable<ViewModel: KotlinViewModel>(
        _: ViewModel.Type
    ) -> ViewModel.Observable {
        let viewModel: ViewModel = inject()
        return viewModel.observable
    }
}

extension PreviewMocks {
    public static subscript<T>(_ keyPath: KeyPath<Koin, T>) -> T.Observable where T: KotlinViewModel {
        PreviewMocks.Companion.shared.koin[keyPath: keyPath].observable
    }
}
