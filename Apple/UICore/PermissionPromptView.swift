import SwiftUI

struct PermissionPromptView: View {
    @ObservedObject
    var observable: PermissionPromptViewModel.Observable
    var viewModel: PermissionPromptViewModel { observable.viewModel() }

    var body: some View {
        VStack {
            Image(systemName: "globe")
                .font(.system(size: 72))
            Spacer()
            Text(viewModel.title)
                .font(.headline)
            Spacer()
            VStack(alignment: .labelStart) {
                ForEach(viewModel.permissions) { permission in
                    Label {
                        Text(permission.summary)
                            .font(.title3)
                            .alignmentGuide(.labelStart) { $0[.leading] }
                    } icon: {
                        Image(systemName: permission.symbolName)
                            .font(.title2)
                            .symbolVariant(.fill)
                            .foregroundColor(.accentColor)
                            .alignmentGuide(HorizontalAlignment.center) { $0[.labelStart] + 18 }
                            .frame(alignment: .center)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.vertical)
                }
            }
            Spacer()
            #if os(macOS)
            HStack {
                Button(action: viewModel.deny) {
                    Text(viewModel.denyTitle)
                        .frame(minWidth: 84)
                }
                Button(action: viewModel.allow) {
                    Text(viewModel.allowTitle)
                        .frame(minWidth: 84)
                }
                .keyboardShortcut(.defaultAction)
            }
            .controlSize(.large)
            #else
            Button(viewModel.allowTitle, action: viewModel.allow)
                .buttonStyle(.floating)
            Button(viewModel.denyTitle, role: .cancel, action: viewModel.deny)
            .font(.headline)
            .padding(.vertical)
            #endif
            Spacer()
        }
        .padding()
        .padding(.horizontal)
    }
}

extension HorizontalAlignment {
    private struct LabelStartAlignment: AlignmentID {
        static func defaultValue(in context: ViewDimensions) -> CGFloat {
            return context[HorizontalAlignment.leading]
        }
    }

    private struct ImageCenterAlignment: AlignmentID {
        static func defaultValue(in context: ViewDimensions) -> CGFloat {
            return context[HorizontalAlignment.center]
        }
    }

    static let labelStart = HorizontalAlignment(LabelStartAlignment.self)
    static let imageCenter = HorizontalAlignment(ImageCenterAlignment.self)
}

extension PermissionPromptViewModel: KotlinViewModel {
    public final class Observable: KotlinObservableObject { }

    public static let bindings: [KotlinBinding] = []
}

extension PermissionPromptViewModel.Permission: KotlinEnum {
    var symbolName: String {
        switch self {
        case .address:
            return "person.text.rectangle"
        case .accountBalance:
            return "banknote"
        case .activity:
            return "arrow.up.arrow.down.square"
        default:
            kotlinCaseUnreachable(self)
        }
    }
}

#if DEBUG
struct PermissionPromptView_Previews: PreviewProvider {
    static var previews: some View {
        PermissionPromptView(observable: PreviewMocks[\.permissionPromptViewModel])
            .previewAsScreen()
    }
}
#endif
