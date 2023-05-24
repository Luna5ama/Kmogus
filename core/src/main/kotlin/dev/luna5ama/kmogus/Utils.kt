package dev.luna5ama.kmogus

import sun.misc.Unsafe
import java.nio.*

internal val UNSAFE = run {
    val field = Unsafe::class.java.getDeclaredField("theUnsafe")
    field.isAccessible = true
    field.get(null) as Unsafe
}

internal val BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(ByteArray::class.java)
internal val SHORT_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(ShortArray::class.java)
internal val INT_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(IntArray::class.java)
internal val LONG_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(LongArray::class.java)
internal val FLOAT_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(FloatArray::class.java)
internal val DOUBLE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(DoubleArray::class.java)

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

fun memcpy(src: MemoryPointer, dst: MemoryPointer, srcOffset: Long = 0L, dstOffset: Long = 0L, length: Long) {
    UNSAFE.copyMemory(src.address + srcOffset, dst.address + dstOffset, length)
}