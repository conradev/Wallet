import SwiftUI
import WalletCore

struct NavigationStackView<Content, Tag>: View where Content: View, Tag: KotlinCaseIterable, Tag.Value: Hashable {
    @Binding var stack: [Tag.Value]

    private var depth: Int = 0
    private var content: (Tag.Value) -> Content

    private var isRoot: Bool {
        depth == 0
    }

    private var current: Tag.Value? {
        component(at: depth).wrappedValue
    }

    private var next: Binding<Tag.Value?> {
        component(at: depth + 1)
    }

    var body: some View {
        if isRoot {
            NavigationView {
                page
            }
            .navigationViewStyle(.stack)
        } else {
            page
        }
    }

    @ViewBuilder private var page: some View {
        if let current = current {
            content(current)
                .overlay(links)
        } else {
            links
        }
    }

    @ViewBuilder private var links: some View {
        VStack {
            ForEach(Tag.allCases, id: \.self) { tag in
                NavigationLink(tag: tag, selection: next) {
                    Self(stack: $stack, depth: depth + 1, content: content)
                } label: {
                    EmptyView()
                }
            }
        }
    }

    init(_: Tag.Type, stack: Binding<[Tag.Value]>, @ViewBuilder content: @escaping (Tag.Value) -> Content) {
        self.init(stack: stack, depth: 0, content: content)
    }

    private init(stack: Binding<[Tag.Value]>, depth: Int, @ViewBuilder content: @escaping (Tag.Value) -> Content) {
        self._stack = stack
        self.depth = depth
        self.content = content
    }

    private func component(at index: Int) -> Binding<Tag.Value?> {
        .init {
            if index < stack.count {
                return stack[index]
            } else {
                return nil
            }
        } set: { newValue in
            if let newValue = newValue {
                if index < stack.count {
                    stack[index] = newValue
                } else {
                    stack.append(newValue)
                }
            } else {
                if index < stack.count {
                    stack.removeSubrange(index..<stack.endIndex)
                }
            }
        }
    }
}
