package dev.luna5ama.kmogus

class MemoryArray(val pointer: MemoryPointer): MemoryPointer by pointer {
    var offset = 0L

    fun offset(span: Span) {
        offset = span.address - pointer.address
    }

    fun reset() {
        offset = 0L
    }

    operator fun plusAssign(offset: Long) {
        this.offset += offset
    }

    operator fun minusAssign(offset: Long) {
        this.offset -= offset
    }
}

fun MemoryPointer.asArray() = MemoryArray(this)