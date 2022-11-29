import Foundation

class MetadataIterator<R>: RandomAccessCollection {
    let buffer: UnsafeMutableBufferPointer<R>

    var startIndex: Int { buffer.startIndex }
    var endIndex: Int { buffer.endIndex }

    init<T>(_ receiver: T?, _ function: (T?, UnsafeMutablePointer<UInt32>?) -> UnsafeMutablePointer<R>?) {
        var count: UInt32 = 0
        let pointer = function(receiver, &count)
        buffer = UnsafeMutableBufferPointer(start: pointer, count: Int(count))
    }

    subscript(index: Int) -> R { buffer[index] }

    deinit { free(buffer.baseAddress) }
}

struct ClassMetadataIterator<R>: RandomAccessCollection {
    private var iterators: [(Range<Int>, MetadataIterator<R>)]

    var startIndex: Int { 0 }
    var endIndex: Int { iterators.last?.0.upperBound ?? 0 }

    init(
        _ receiver: AnyClass,
        _ function: (AnyClass?, UnsafeMutablePointer<UInt32>?) -> UnsafeMutablePointer<R>?
    ) {
        var types: [AnyClass] = []
        var next: AnyClass? = receiver
        while let type = next {
            types.append(type)
            next = class_getSuperclass(type)
        }

        let iterators = types.map { MetadataIterator($0, function) }

        var ranges: [Range<Int>] = []
        for iterator in iterators {
            let offset = ranges.last?.upperBound ?? 0
            ranges.append(iterator.startIndex + offset..<iterator.endIndex + offset)
        }

        self.iterators = Array(zip(ranges, iterators))
    }

    subscript(index: Int) -> R {
        let value = iterators
            .first { range, _ in range.contains(index) }
            .map { range, iterator in iterator[index - range.lowerBound] }
        guard let value = value else { fatalError("Index out of bounds") }
        return value
    }
}

extension NSObject {
    private class var instanceVariables: ClassMetadataIterator<Ivar> {
        ClassMetadataIterator(self, class_copyIvarList)
    }

    private var instanceVariables: [AnyObject] {
        Self.instanceVariables.compactMap { self[variable: $0] }
    }

    public func findInstanceVariable<T>(_ type: T.Type, depth: Int = 0) -> Set<T> where T: NSObject {
        Set(findInstanceVariable({ $0 is T }, depth: depth).compactMap { $0 as? T })
    }

    public func findInstanceVariable(_ predicate: (NSObject) -> Bool, depth: Int = 0) -> Set<NSObject> {
        let instanceVariables = instanceVariables
        let matches = Set(instanceVariables.compactMap { $0 as? NSObject }.filter { predicate($0) })
        guard depth > 0 else { return matches }

        return instanceVariables
            .compactMap { $0 as? NSObject }
            .map { matches.union($0.findInstanceVariable(predicate, depth: depth - 1)) }
            .reduce(into: Set()) { $0.formUnion($1) }
    }

    private subscript(variable variable: Ivar) -> AnyObject? {
        guard
            let typeEncoding = ivar_getTypeEncoding(variable),
            String(cString: typeEncoding).starts(with: "@") else { return nil }
        return object_getIvar(self, variable) as? AnyObject
    }
}
