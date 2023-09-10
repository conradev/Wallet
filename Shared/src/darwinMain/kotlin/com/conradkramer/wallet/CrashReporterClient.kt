package com.conradkramer.wallet

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.NativePtr
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKStringFromUtf8
import platform.darwin.LC_SEGMENT_64
import platform.darwin.load_command
import platform.darwin.mach_header_64
import platform.darwin.section_64
import platform.darwin.segment_command_64
import platform.posix.Dl_info
import platform.posix.RTLD_DEFAULT
import platform.posix.dladdr
import platform.posix.dlsym

@kotlin.experimental.ExperimentalNativeApi
class CrashReporterClient {
    companion object {
        fun installHook() {
            var previous: ReportUnhandledExceptionHook? = null
            previous = setUnhandledExceptionHook { exception ->
                Image.objc.write(exception)
                previous?.invoke(exception)
            }
        }
    }
}

private data class Image(val base: NativePtr) {
    private data class Header(
        val offset: NativePtr,
        val numberOfCommands: UInt,
        val size: Long = sizeOf<mach_header_64>(),
    )

    private open class LoadCommand(
        val size: UInt,
    )

    private class SegmentLoadCommand(
        val name: String,
        val offset: NativePtr,
        size: UInt,
        val sections: List<Section>,
    ) : LoadCommand(size) {
        data class Section(
            val name: String,
            val offset: NativePtr,
        )

        private data class SectionIterator(
            var offset: NativePtr,
            val count: UInt,
        ) : Iterator<Section> {
            private var index: UInt = 0u

            override fun hasNext(): Boolean {
                return index < count
            }

            override fun next(): Section {
                val header = interpretCPointer<section_64>(offset)!!.pointed
                val name = header.sectname.toKStringFromUtf8()
                val sectionOffset = NativePtr.NULL + header.addr.toLong()
                offset += sizeOf<section_64>()
                index++
                return Section(name, sectionOffset)
            }
        }

        companion object {
            fun fromAddress(offset: NativePtr): SegmentLoadCommand {
                val header = interpretCPointer<segment_command_64>(offset)!!.pointed
                val name = header.segname.toKStringFromUtf8()
                val segmentOffset = NativePtr.NULL + header.vmaddr.toLong()
                val sections = SectionIterator(
                    offset + sizeOf<segment_command_64>(),
                    header.nsects,
                ).asSequence().toList()
                return SegmentLoadCommand(name, segmentOffset, header.cmdsize, sections)
            }
        }
    }

    private data class LoadCommandIterator(val image: Image) : Iterator<LoadCommand> {
        private var index: UInt = 0u
        private var offset = image.base + image.header.size
        private val count = image.header.numberOfCommands

        override fun hasNext(): Boolean {
            return index < count
        }

        override fun next(): LoadCommand {
            val header = interpretCPointer<load_command>(offset)!!.pointed
            val size = header.cmdsize
            val command = when (header.cmd) {
                LC_SEGMENT_64.toUInt() -> SegmentLoadCommand.fromAddress(offset)
                else -> LoadCommand(size)
            }
            offset += header.cmdsize.toLong()
            index++
            return command
        }
    }

    private val header: Header
        get() {
            val pointer = interpretCPointer<mach_header_64>(base)!!
            return Header(pointer.rawValue, pointer.pointed.ncmds)
        }

    private val first: NativePtr
        get() = LoadCommandIterator(this)
            .asSequence()
            .mapNotNull { it as? Image.SegmentLoadCommand }
            .first { it.name == "__TEXT" }
            .offset

    private val crashInfo: NativePtr
        get() = LoadCommandIterator(this)
            .asSequence()
            .mapNotNull { it as? SegmentLoadCommand }
            .filter { it.name == "__DATA" }
            .flatMap { it.sections }
            .first { it.name == "__crash_info" }
            .offset

    /**
     * struct crashreporter_annotations_t {
     *     uint64_t version;
     *     uint64_t message;
     *     uint64_t signature_string;
     *     uint64_t backtrace;
     *     uint64_t message2;
     *     uint64_t thread;
     *     uint64_t dialog_mode;
     *     uint64_t abort_cause;
     * };
     */
    fun write(throwable: Throwable) {
        val offset = NativePtr.NULL + crashInfo.toLong() + (-first.toLong()) + header.offset.toLong()
        val struct = interpretCPointer<ULongVar>(offset)!!
        if (struct[0].toInt() != 5) {
            return
        }

        /**
         * Write to the "signature_string" field because the message field gets
         * ignored because libsystem_c writes to it from abort() and it takes precedence
         */
        val message = throwable.toString().getPointer().rawValue.toLong().toULong()
        struct[1] = message
        struct[2] = message

        struct[3] = throwable
            .getStackTrace()
            .joinToString("\n")
            .getPointer()
            .rawValue
            .toLong()
            .toULong()
    }

    companion object {
        private fun fromAddress(pointer: COpaquePointer): Image {
            val base = memScoped {
                val info: Dl_info = alloc()
                dladdr(pointer, info.ptr)
                info.dli_fbase?.rawValue
                    ?: throw Exception("Could not resolve image from pointer $pointer")
            }
            return Image(base)
        }

        private fun fromSymbol(symbol: String): Image {
            val pointer = dlsym(RTLD_DEFAULT, symbol)
                ?: throw Exception("Could not resolve image from symbol $symbol")
            return fromAddress(pointer)
        }

        val objc: Image
            get() = fromSymbol("objc_getClass")
    }
}

private fun String.getPointer(): CPointer<ByteVar> {
    return cstr.place(interpretCPointer(nativeHeap.alloc(cstr.size, cstr.align).rawPtr)!!)
}
