package dev.luna5ama.kmogus

interface MutableArr : Arr {
    val basePtr: Ptr
    val baseLen: Long

    var pos: Long
    override var len: Long
    override val ptr get() = basePtr + pos
    val rem get() = len - pos

    fun pos(ptr: Ptr) {
        require(ptr.address in basePtr.address..(basePtr.address + baseLen)) { "Ptr out of bounds" }
        pos = ptr.address - basePtr.address
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

private class MutableArrImpl(val base: Arr) : MutableArr {
    override val basePtr get() = base.ptr
    override val baseLen get() = base.len

    override var pos = 0L
    override var len = baseLen

    override fun realloc(newLength: Long, init: Boolean) {
        val oldLen = baseLen
        base.realloc(newLength, init)
        if (len == oldLen) {
            len = newLength
        }
    }

    override fun free() {
        base.free()
    }
}

fun Arr.asMutable(): MutableArr = MutableArrImpl(this)

inline fun MutableArr.usePtr(crossinline block: Ptr.() -> Ptr) {
    val ptr = block(ptr)
    pos(ptr)
}