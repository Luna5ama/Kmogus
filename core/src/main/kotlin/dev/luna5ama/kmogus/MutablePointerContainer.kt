package dev.luna5ama.kmogus

class MutablePointerContainer(val base: PointerContainer) : PointerContainer by base {
    var offset = 0L

    val basePointer get() = base.pointer
    val baseLength get() = base.length

    override val length: Long get() = base.length - offset
    override val pointer get() = basePointer + offset

    fun offset(pointer: Pointer) {
        require(pointer.address in basePointer.address..(basePointer.address + baseLength)) { "Pointer out of bounds"}
        offset = pointer.address - basePointer.address
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

fun PointerContainer.asMutable() = MutablePointerContainer(this)

operator fun MutablePointerContainer.plus(offset: Long) = base + (this.offset + offset)

operator fun MutablePointerContainer.minus(offset: Long) = base + (this.offset - offset)