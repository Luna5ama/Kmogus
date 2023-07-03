package dev.luna5ama.kmogus

class MutableArr(val base: Arr) : Arr by base {
    var offset = 0L

    val basePtr get() = base.ptr
    val baseLen get() = base.len

    override val len: Long get() = base.len - offset
    override val ptr get() = basePtr + offset
    val remaining get() = len

    fun offset(ptr: Ptr) {
        require(ptr.address in basePtr.address..(basePtr.address + baseLen)) { "Ptr out of bounds" }
        offset = ptr.address - basePtr.address
    }

    inline fun usePtr(crossinline block: Ptr.() -> Ptr) {
        val ptr = block(ptr)
        offset(ptr)
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