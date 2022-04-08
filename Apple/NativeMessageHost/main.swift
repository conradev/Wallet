import WalletCore

let app = KoinApplication.start()
let host: NativeMessageHost = app.inject()

host.run()
