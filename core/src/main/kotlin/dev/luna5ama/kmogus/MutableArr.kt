package dev.luna5ama.kmogus

interface MutableArr : Arr {
    val basePtr: Ptr
    val baseLen: Long

    var pos: Long
    override var len: Long
    override val ptr get() = basePtr + pos
    val rem get() = len - pos

    fun pos(ptr: Ptr): MutableArr {
        require(ptr.address in basePtr.address..(basePtr.address + baseLen)) { "Ptr out of bounds" }
        pos = ptr.address - basePtr.address
        return this
    }

    fun flip(): MutableArr {
        len = pos
        pos = 0L
        return this
    }

    fun reset(): MutableArr {
        pos = 0L
        len = baseLen
        return this
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

    override fun realloc(newLength: Long, init: Boolean): Arr {
        val oldLen = baseLen
        base.realloc(newLength, init)
        if (len == oldLen) {
            len = newLength
        }
        return this
    }

    override fun free() {
        base.free()
    }
}

fun Arr.asMutable(): MutableArr = MutableArrImpl(this)

inline fun MutableArr.usePtr(block: Ptr.() -> Ptr) {
    val ptr = block(ptr)
    pos(ptr)
}