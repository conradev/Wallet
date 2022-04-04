# Architecture

The app is being built from the ground up with multi-platform and multi-chain in mind. Doing both at once would be difficult, so it is starting off as multi-platform (macOS, iOS, Android) on a single chain (Ethereum), and will add support more chains in the future.

In order to share as much code as possible between the platforms, the app is built using [Kotlin Multiplatform](https://kotlinlang.org/lp/mobile/). Kotlin Multiplatform allows for easy integration between Kotlin and native code, which is important because the UI on each platform is implemented in a platform-native framework and language. The app implements its UI using SwiftUI on macOS and iOS, and Jetpack Compose on Android. These UI frameworks give the app a more native platform feel, and support features like accessibility, dynamic text size and dark mode out of the box.

The app uses the [model-view-viewmodel (MVVM)](https://en.wikipedia.org/wiki/Model–view–viewmodel) architecture, and everything up to and including the view models is shared between platforms. With this architecture, the only platform-specific code is the UI and navigation code. This also hopefully means that adding new platforms and UI frameworks in the future (GTK on Linux, WinUI on Windows) will be less of a burden.

The app uses [Koin](https://insert-koin.io) for dependency injection. When possible, the code tries to avoid Koin leaking into the components themselves, instead opting for Koin to create a separate graph of interrelated components (a module). See `Koin.kt` for the module definitions in each package. 

The view models use [Flows](https://kotlinlang.org/docs/flow.html) in order to keep their state updated. Jetpack Compose and SwiftUI each have the ability to update their views in response to changes from flows.

Data is persisted using [SQLDelight](https://cashapp.github.io/sqldelight/).
