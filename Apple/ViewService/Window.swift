import AppKit
import DictionaryCoding

struct Window: Identifiable {
    private struct Query: Sequence {
        struct Iterator: IteratorProtocol {
            let iterator: AnyObject

            func next() -> Window? {
                guard SLSWindowIteratorAdvance(iterator) else { return nil }
                return Window(from: iterator)
            }
        }

        var windows: [Window.ID]?

        func makeIterator() -> Iterator {
            let connection = SLSMainConnectionID()
            let query = SLSWindowQueryWindows(connection, windows.map { $0 as CFArray }, 0)
            return Iterator(iterator: SLSWindowQueryResultCopyWindows(query))
        }
    }

    static var onScreen: [Window] {
        Array(Query())
    }

    var id: CGWindowID
    var processIdentifier: pid_t
    var bounds: CGRect
    var isVisible: Bool

    private init(from iterator: AnyObject) {
        id = SLSWindowIteratorGetWindowID(iterator)
        processIdentifier = SLSWindowIteratorGetPID(iterator)
        bounds = SLSWindowIteratorGetBounds(iterator)

        var isVisible: ObjCBool = false
        SLSWindowIsVisible(SLSMainConnectionID(), id, &isVisible)
        self.isVisible = isVisible.boolValue
    }

    init?(id: Window.ID) {
        guard let window = Array(Query(windows: [id])).first else { return nil }
        self = window
    }

    func frame(in screen: NSScreen) -> NSRect {
        var frame = bounds
        frame.origin.y = screen.frame.size.height - frame.origin.y - frame.size.height
        return frame
    }
}
