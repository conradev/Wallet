import SwiftUI
import WalletUICore

struct WelcomeView: View {
    @ObservedObject
    var observable: WelcomeViewModel.Observable

    #if !os(macOS)
    var action: ((WelcomeViewModel.Option) -> Void)?
    #endif

    var body: some View {
        VStack(spacing: 10) {
            Text(observable.viewModel.title)
                .font(.init(OnboardingView.titleFont))
            Text(observable.viewModel.subtitle)
                .font(.title3)
                .frame(maxWidth: 500)
            Spacer()
            ForEach(observable.viewModel.options) { option in
                Button(.init(option.title), symbolName: option.symbolName) {
                    observable.selectedOption = option
                    #if !os(macOS)
                    action?(option)
                    #endif
                }
                .buttonStyle(CellButtonStyle(selected: observable.selectedOption == option))
                .frame(maxWidth: 300)
            }
            Spacer()
        }
        .padding()
    }
}

extension WelcomeViewModel: KotlinViewModel {
    public final class Observable: KotlinObservableObject<WelcomeViewModel> {
        @Published
        var selectedOption: Option = .importPhrase
    }

    public static let bindings: [KotlinBinding] = [
        .init(\WelcomeViewModel.selectedOption, \.selectedOptionNative, \.selectedOption)
    ]
}

extension WelcomeViewModel.Option: KotlinEnum {
    var symbolName: String {
        switch self {
        case .importPhrase:
            return "square.and.arrow.down.on.square.fill"
        case .generate:
            return "plus.circle"
        default:
            kotlinCaseUnreachable(self)
        }
    }
}

extension Button where Label == AnyView {
    init(_ titleKey: LocalizedStringKey, symbolName: String, action: @escaping () -> Void) {
        self.init(action: action) {
            AnyView(
                HStack {
                    Image(systemName: symbolName)
                    #if !os(macOS)
                        .foregroundColor(.accentColor)
                    #endif
                    Text(titleKey)
                    Spacer()
                }
                    .padding(.horizontal)
            )
        }
    }
}

struct CellButtonStyle: ButtonStyle {
    var selected: Bool

    var shape: RoundedRectangle {
        RoundedRectangle(cornerRadius: 15, style: .continuous)
    }

    @ViewBuilder
    var stroke: some View {
        if selected {
            shape.stroke(Color.accentColor, lineWidth: 3)
        } else {
            shape.fill(.clear)
        }
    }

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.title2)
            .frame(maxWidth: .infinity, minHeight: 48)
        #if os(macOS)
            .background(stroke.background(shape.fill(.selection)))
        #else
            .background(shape.fill(Color(UIColor.secondarySystemBackground)))
        #endif
    }
}

#if DEBUG
struct WelcomeView_Previews: PreviewProvider {
    static var previews: some View {
        WelcomeView(observable: PreviewMocks[\.welcomeViewModel])
            .previewAsScreen()
    }
}
#endif
