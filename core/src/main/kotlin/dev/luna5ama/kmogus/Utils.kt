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
private val POSITION_OFFSET = UNSAFE.objectFieldOffset(Buffer::class.java.getDeclaredField("position"))
private val MARK_OFFSET = UNSAFE.objectFieldOffset(Buffer::class.java.getDeclaredField("mark"))
private val LIMIT_OFFSET = UNSAFE.objectFieldOffset(Buffer::class.java.getDeclaredField("limit"))
private val CAPACITY_OFFSET = UNSAFE.objectFieldOffset(Buffer::class.java.getDeclaredField("capacity"))

internal var Buffer.address
    get() = UNSAFE.getLong(this, ADDRESS_OFFSET)
    set(value) {
        UNSAFE.putLong(this, ADDRESS_OFFSET, value)
    }

internal var Buffer.position
    get() = position()
    set(value) {
        UNSAFE.putInt(this, POSITION_OFFSET, value)
    }

internal var Buffer.mark
    get() = UNSAFE.getInt(this, MARK_OFFSET)
    set(value) {
        UNSAFE.putInt(this, MARK_OFFSET, value)
    }

internal var Buffer.limit
    get() = limit()
    set(value) {
        UNSAFE.putInt(this, LIMIT_OFFSET, value)
    }

internal var Buffer.capacity
    get() = capacity()
    set(value) {
        UNSAFE.putInt(this, CAPACITY_OFFSET, value)
    }

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

private val DIRECT_BYTE_BUFFER_CLASS = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder()).javaClass

fun nullByteBuffer(): ByteBuffer {
    val buffer = (UNSAFE.allocateInstance(DIRECT_BYTE_BUFFER_CLASS) as ByteBuffer).order(ByteOrder.nativeOrder())
    buffer.address = 0
    buffer.mark = -1
    buffer.limit = 0
    buffer.capacity = 0

    return buffer
}

fun nullShortBuffer(): ShortBuffer {
    return nullByteBuffer().asShortBuffer()
}

fun nullCharBuffer(): CharBuffer {
    return nullByteBuffer().asCharBuffer()
}

fun nullIntBuffer(): IntBuffer {
    return nullByteBuffer().asIntBuffer()
}

fun nullLongBuffer(): LongBuffer {
    return nullByteBuffer().asLongBuffer()
}

fun nullFloatBuffer(): FloatBuffer {
    return nullByteBuffer().asFloatBuffer()
}

fun nullDoubleBuffer(): DoubleBuffer {
    return nullByteBuffer().asDoubleBuffer()
}

fun Ptr.asByteBuffer(size: Int, oldBuffer: ByteBuffer? = null): ByteBuffer {
    val buffer = oldBuffer ?: nullByteBuffer()
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1
    buffer.limit = size
    buffer.capacity = size

    return buffer
}

fun Ptr.asByteBuffer(buffer: ByteBuffer): ByteBuffer {
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1

    return buffer
}

fun Ptr.asShortBuffer(size: Int, oldBuffer: ShortBuffer? = null): ShortBuffer {
    val buffer = oldBuffer ?: nullShortBuffer()
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1
    buffer.limit = size
    buffer.capacity = size

    return buffer
}

fun Ptr.asShortBuffer(buffer: ShortBuffer): ShortBuffer {
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1

    return buffer
}

fun Ptr.asCharBuffer(size: Int, oldBuffer: CharBuffer? = null): CharBuffer {
    val buffer = oldBuffer ?: nullCharBuffer()
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1
    buffer.limit = size
    buffer.capacity = size

    return buffer
}

fun Ptr.asCharBuffer(buffer: CharBuffer): CharBuffer {
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1

    return buffer
}

fun Ptr.asIntBuffer(size: Int, oldBuffer: IntBuffer? = null): IntBuffer {
    val buffer = oldBuffer ?: nullIntBuffer()
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1
    buffer.limit = size
    buffer.capacity = size

    return buffer
}

fun Ptr.asIntBuffer(buffer: IntBuffer): IntBuffer {
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1

    return buffer
}

fun Ptr.asLongBuffer(size: Int, oldBuffer: LongBuffer? = null): LongBuffer {
    val buffer = oldBuffer ?: nullLongBuffer()
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1
    buffer.limit = size
    buffer.capacity = size

    return buffer
}

fun Ptr.asLongBuffer(buffer: LongBuffer): LongBuffer {
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1

    return buffer
}

fun Ptr.asFloatBuffer(size: Int, oldBuffer: FloatBuffer? = null): FloatBuffer {
    val buffer = oldBuffer ?: nullFloatBuffer()
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1
    buffer.limit = size
    buffer.capacity = size

    return buffer
}

fun Ptr.asFloatBuffer(buffer: FloatBuffer): FloatBuffer {
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1

    return buffer
}

fun Ptr.asDoubleBuffer(size: Int, oldBuffer: DoubleBuffer? = null): DoubleBuffer {
    val buffer = oldBuffer ?: nullDoubleBuffer()
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1
    buffer.limit = size
    buffer.capacity = size

    return buffer
}

fun Ptr.asDoubleBuffer(buffer: DoubleBuffer): DoubleBuffer {
    buffer.address = this.address
    buffer.position = 0
    buffer.mark = -1

    return buffer
}