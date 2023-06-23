package dev.luna5ama.kmogus

class MutableArr(val base: Arr) : Arr by base {
    var offset = 0L

    val basePointer get() = base.ptr
    val baseLength get() = base.length

    override val length: Long get() = base.length - offset
    override val ptr get() = basePointer + offset

    fun offset(ptr: Ptr) {
        require(ptr.address in basePointer.address..(basePointer.address + baseLength)) { "Ptr out of bounds"}
        offset = ptr.address - basePointer.address
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

fun Arr.asMutable() = MutableArr(this)

operator fun MutableArr.plus(offset: Long) = base + (this.offset + offset)

operator fun MutableArr.minus(offset: Long) = base + (this.offset - offset)