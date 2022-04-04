import Combine
import KMPNativeCoroutinesCombine
import KMPNativeCoroutinesCore
import Shared
import SwiftUI
import WalletCore

struct KotlinBinding {
    var bind: (AnyObject, AnyObject) -> [AnyCancellable]

    init<ViewModel: KotlinViewModel, T, U>(
        _ viewModelKeyPath: KeyPath<ViewModel, U>,
        _ viewModelNativeKeyPath: KeyPath<ViewModel, NativeFlow<T, Error, KotlinUnit>>,
        _ valueKeyPath: ReferenceWritableKeyPath<ViewModel.Observable, U>,
        _ content: @escaping (T) -> U
    ) {
        bind = { foo, bar in
            guard
                let viewModel = foo as? ViewModel,
                let observable = bar as? ViewModel.Observable else { fatalError("Failed to bind \(foo) and \(bar)") }
            observable[keyPath: valueKeyPath] = viewModel[keyPath: viewModelKeyPath]
            let read = createPublisher(for: viewModel[keyPath: viewModelNativeKeyPath])
                .map(content)
                .replaceError(with: observable[keyPath: valueKeyPath])
                .receive(on: DispatchQueue.main)
                .assign(to: valueKeyPath, on: observable)
            return [read]
        }
    }

    init<ViewModel: KotlinViewModel, T>(
        _ viewModelKeyPath: KeyPath<ViewModel, T>,
        _ viewModelNativeKeyPath: KeyPath<ViewModel, NativeFlow<T, Error, KotlinUnit>>,
        _ valueKeyPath: ReferenceWritableKeyPath<ViewModel.Observable, T>
    ) {
        self.init(viewModelKeyPath, viewModelNativeKeyPath, valueKeyPath) { $0 }
    }

    init<ViewModel: KotlinViewModel, T, U: Equatable>(
        _ viewModelKeyPath: KeyPath<ViewModel, Kotlinx_coroutines_coreMutableStateFlow>,
        _ viewModelNativeKeyPath: KeyPath<ViewModel, NativeFlow<T, Error, KotlinUnit>>,
        _ valueKeyPath: ReferenceWritableKeyPath<ViewModel.Observable, U>,
        _ content: @escaping (T) -> U
    ) {
        bind = { foo, bar in
            guard
                let viewModel = foo as? ViewModel,
                let observable = bar as? ViewModel.Observable else { fatalError("Failed to bind \(foo) and \(bar)") }
            if let currentValue = viewModel[keyPath: viewModelKeyPath].value as? T {
                observable[keyPath: valueKeyPath] = content(currentValue)
            }
            let read = createPublisher(for: viewModel[keyPath: viewModelNativeKeyPath])
                .map(content)
                .replaceError(with: observable[keyPath: valueKeyPath])
                .receive(on: DispatchQueue.main)
                .assign(to: valueKeyPath, on: observable)
            let write = observable
                .objectWillChange
                .delay(for: 0, scheduler: RunLoop.main)
                .map { observable[keyPath: valueKeyPath] }
                .sink { viewModel[keyPath: viewModelKeyPath].setValue($0) }
            return [read, write]
        }
    }

    init<ViewModel: KotlinViewModel, T: Equatable>(
        _ viewModelKeyPath: KeyPath<ViewModel, Kotlinx_coroutines_coreMutableStateFlow>,
        _ viewModelNativeKeyPath: KeyPath<ViewModel, NativeFlow<T, Error, KotlinUnit>>,
        _ valueKeyPath: ReferenceWritableKeyPath<ViewModel.Observable, T>
    ) {
        self.init(viewModelKeyPath, viewModelNativeKeyPath, valueKeyPath) { $0 }
    }
}

class KotlinObservableObject<ViewModel>: ObservableObject {
    var viewModel: ViewModel! // swiftlint:disable:this implicitly_unwrapped_optional
    var bindings: [AnyCancellable] = []

    required init() {
    }
}

protocol KotlinViewModel: ObservableObject {
    associatedtype Observable: KotlinObservableObject<Self>

    static var bindings: [KotlinBinding] { get }
}

extension KotlinViewModel {
    var observable: Observable {
        let observable = Observable()
        observable.viewModel = self
        bind(to: observable)
        return observable
    }

    func bind(to observable: Observable) {
        observable.bindings += Self.bindings.flatMap { $0.bind(self, observable) }
    }
}

extension KoinApplication {
    static func observable<T, ViewModel: KotlinViewModel>() -> T where T: KotlinObservableObject<ViewModel> {
        let viewModel: ViewModel = inject()
        if let observable = viewModel.observable as? T {
            return observable
        } else {
            fatalError("Unable to generate observable of type \(T.self)")
        }
    }
}

extension PreviewMocks {
    static subscript<T>(_ keyPath: KeyPath<Koin, T>) -> T.Observable where T: KotlinViewModel {
        PreviewMocks.Companion.shared.koin[keyPath: keyPath].observable
    }
}
