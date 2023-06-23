package dev.luna5ama.kmogus

import sun.misc.Unsafe
import java.nio.*

internal val UNSAFE = run {
    val field = Unsafe::class.java.getDeclaredField("theUnsafe")
    field.isAccessible = true
    field.get(null) as Unsafe
}

internal val BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(ByteArray::class.java).toLong()
internal val SHORT_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(ShortArray::class.java).toLong()
internal val CHAR_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(CharArray::class.java).toLong()
internal val INT_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(IntArray::class.java).toLong()
internal val LONG_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(LongArray::class.java).toLong()
internal val FLOAT_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(FloatArray::class.java).toLong()
internal val DOUBLE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(DoubleArray::class.java).toLong()

private val ADDRESS_OFFSET = UNSAFE.objectFieldOffset(Buffer::class.java.getDeclaredField("address"))

internal val Buffer.address: Long
    get() = UNSAFE.getLong(this, ADDRESS_OFFSET)

internal val Buffer.byteCapacity: Long
    get() = when (this) {
        is ByteBuffer -> this.capacity().toLong() * Byte.SIZE_BYTES
        is ShortBuffer -> this.capacity().toLong() * Short.SIZE_BYTES
        is CharBuffer -> this.capacity().toLong() * Char.SIZE_BYTES
        is IntBuffer -> this.capacity().toLong() * Int.SIZE_BYTES
        is LongBuffer -> this.capacity().toLong() * Long.SIZE_BYTES
        is FloatBuffer -> this.capacity().toLong() * Float.SIZE_BYTES
        is DoubleBuffer -> this.capacity().toLong() * Double.SIZE_BYTES
        else -> throw IllegalArgumentException("Unsupported buffer type: ${this.javaClass}")
    }

val ByteArray.byteLength get() = this.size.toLong() * Byte.SIZE_BYTES
val ShortArray.byteLength get() = this.size.toLong() * Short.SIZE_BYTES
val CharArray.byteLength get() = this.size.toLong() * Char.SIZE_BYTES
val IntArray.byteLength get() = this.size.toLong() * Int.SIZE_BYTES
val LongArray.byteLength get() = this.size.toLong() * Long.SIZE_BYTES
val FloatArray.byteLength get() = this.size.toLong() * Float.SIZE_BYTES
val DoubleArray.byteLength get() = this.size.toLong() * Double.SIZE_BYTES