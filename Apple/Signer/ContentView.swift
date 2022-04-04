import SwiftUI

struct ContentView: View {
    var data: String

    var body: some View {
        Text(data)
            .padding()
            .frame(minWidth: 400, minHeight: 300)
    }
}
