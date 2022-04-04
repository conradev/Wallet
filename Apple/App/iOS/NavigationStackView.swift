import SwiftUI

struct NavigationStackView<Content, Tag>: View
    where Content: View, Tag: CaseIterable & Hashable, Tag.AllCases: RandomAccessCollection {
    @Binding
    var stack: [Tag]

    private var depth: Int = 0
    private var content: (Tag) -> Content

    private var isRoot: Bool {
        depth == 0
    }

    private var current: Tag? {
        component(at: depth).wrappedValue
    }

    private var next: Binding<Tag?> {
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

    @ViewBuilder
    private var page: some View {
        if let current = current {
            content(current)
                .overlay(links)
        } else {
            links
        }
    }

    @ViewBuilder
    private var links: some View {
        VStack {
            ForEach(Tag.allCases, id: \.self) { tag in
                // swiftlint:disable:next multiline_arguments
                NavigationLink(tag: tag, selection: next) {
                    Self(stack: $stack, depth: depth + 1, content: content)
                } label: {
                    EmptyView()
                }
            }
        }
    }

    init(stack: Binding<[Tag]>, @ViewBuilder content: @escaping (Tag) -> Content) {
        self.init(stack: stack, depth: 0, content: content)
    }

    private init(stack: Binding<[Tag]>, depth: Int, @ViewBuilder content: @escaping (Tag) -> Content) {
        self._stack = stack
        self.depth = depth
        self.content = content
    }

    private func component(at index: Int) -> Binding<Tag?> {
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
