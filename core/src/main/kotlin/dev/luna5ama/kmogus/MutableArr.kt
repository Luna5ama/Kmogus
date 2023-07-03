package dev.luna5ama.kmogus

class MutableArr(val base: Arr) : Arr by base {
    var pos = 0L

    val basePtr get() = base.ptr
    val baseLen get() = base.len

    override var len = baseLen
    override val ptr get() = basePtr + pos

    val rem get() = len - pos

    fun pos(ptr: Ptr) {
        require(ptr.address in basePtr.address..(basePtr.address + baseLen)) { "Ptr out of bounds" }
        pos = ptr.address - basePtr.address
    }

    inline fun usePtr(crossinline block: Ptr.() -> Ptr) {
        val ptr = block(ptr)
        pos(ptr)
    }

    fun flip() {
        len = pos
        pos = 0L
    }

    fun reset() {
        pos = 0L
        len = baseLen
    }

    operator fun plusAssign(offset: Long) {
        this.pos += offset
    }

    operator fun minusAssign(offset: Long) {
        this.pos -= offset
    }
}

fun Arr.asMutable() = MutableArr(this)

operator fun MutableArr.plus(offset: Long) = base + (this.pos + offset)

operator fun MutableArr.minus(offset: Long) = base + (this.pos - offset)