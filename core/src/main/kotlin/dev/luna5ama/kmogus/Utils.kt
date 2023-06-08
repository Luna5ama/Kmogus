package dev.luna5ama.kmogus

import sun.misc.Unsafe
import java.nio.*

internal val UNSAFE = run {
    val field = Unsafe::class.java.getDeclaredField("theUnsafe")
    field.isAccessible = true
    field.get(null) as Unsafe
}

private val ADDRESS_OFFSET = UNSAFE.objectFieldOffset(Buffer::class.java.getDeclaredField("address"))

internal val Buffer.address: Long
    get() = UNSAFE.getLong(this, ADDRESS_OFFSET)

internal val Buffer.byteCapacity: Long
    get() = when (this) {
        is ByteBuffer -> this.capacity().toLong()
        is ShortBuffer -> this.capacity().toLong() * 2L
        is IntBuffer -> this.capacity().toLong() * 4L
        is LongBuffer -> this.capacity().toLong() * 8L
        is FloatBuffer -> this.capacity().toLong() * 4L
        is DoubleBuffer -> this.capacity().toLong() * 8L
        else -> throw IllegalArgumentException("Unsupported buffer type: ${this.javaClass}")
    }

fun memcpy(src: Pointer, dst: Pointer, length: Long) {
    UNSAFE.copyMemory(src.address, dst.address, length)
}