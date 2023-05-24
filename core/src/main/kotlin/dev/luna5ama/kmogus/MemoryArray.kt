package dev.luna5ama.kmogus

import java.nio.Buffer
import kotlin.math.max

interface MemoryArray : MemoryPointer {
    var pointer: Long

    fun isEmpty(): Boolean {
        return pointer == 0L
    }

    fun isNotEmpty(): Boolean {
        return pointer != 0L
    }

    // Read access
    // Single read access
    fun popByte(): Byte {
        assert(pointer >= 1L) { "Cannot pop from an empty memory list" }
        pointer--
        return getByte(pointer)
    }

    fun popShort(): Short {
        check(pointer >= 2L) { "Cannot pop from an empty memory list" }
        pointer -= 2
        return getShort(pointer)
    }

    fun popInt(): Int {
        check(pointer >= 4L) { "Cannot pop from an empty memory list" }
        pointer -= 4
        return getInt(pointer)
    }

    fun popLong(): Long {
        check(pointer >= 8L) { "Cannot pop from an empty memory list" }
        pointer -= 8
        return getLong(pointer)
    }

    fun popFloat(): Float {
        check(pointer >= 4L) { "Cannot pop from an empty memory list" }
        pointer -= 4
        return getFloat(pointer)
    }

    fun popDouble(): Double {
        check(pointer >= 8L) { "Cannot pop from an empty memory list" }
        pointer -= 8
        return getDouble(pointer)
    }

    // Write access
    // Single write access
    fun pushByte(value: Byte) {
        ensureCapacity(pointer + 1L)
        setByte(pointer, value)
        pointer++
    }

    fun pushShort(value: Short) {
        ensureCapacity(pointer + 2L)
        setShort(pointer, value)
        pointer += 2
    }

    fun pushInt(value: Int) {
        ensureCapacity(pointer + 4L)
        setInt(pointer, value)
        pointer += 4
    }

    fun pushLong(value: Long) {
        ensureCapacity(pointer + 8L)
        setLong(pointer, value)
        pointer += 8
    }

    fun pushFloat(value: Float) {
        ensureCapacity(pointer + 4L)
        setFloat(pointer, value)
        pointer += 4
    }

    fun pushDouble(value: Double) {
        ensureCapacity(pointer + 8L)
        setDouble(pointer, value)
        pointer += 8
    }

    // Bulk write access
    fun pushBytes(a: ByteArray, offset: Int = 0, length: Int = a.size - offset) {
        ensureCapacity(pointer + length)
        UNSAFE.copyMemory(a, (BYTE_ARRAY_OFFSET + offset).toLong(), null, address + pointer, length.toLong())
        pointer += length
    }

    fun pushShorts(a: ShortArray, offset: Int = 0, length: Int = a.size - offset) {
        ensureCapacity(pointer + length * 2L)
        UNSAFE.copyMemory(a, SHORT_ARRAY_OFFSET + offset * 2L, null, address + pointer, length * 2L)
        pointer += length * 2L
    }

    fun pushInts(a: IntArray, offset: Int = 0, length: Int = a.size - offset) {
        ensureCapacity(pointer + length * 4L)
        UNSAFE.copyMemory(a, INT_ARRAY_OFFSET + offset * 4L, null, address + pointer, length * 4L)
        pointer += length * 4L
    }

    fun pushLongs(a: LongArray, offset: Int = 0, length: Int = a.size - offset) {
        ensureCapacity(pointer + length * 8L)
        UNSAFE.copyMemory(a, LONG_ARRAY_OFFSET + offset * 8L, null, address + pointer, length * 8L)
        pointer += length * 8L
    }

    fun pushFloats(a: FloatArray, offset: Int = 0, length: Int = a.size - offset) {
        ensureCapacity(pointer + length * 4L)
        UNSAFE.copyMemory(a, FLOAT_ARRAY_OFFSET + offset * 4L, null, address + pointer, length * 4L)
        pointer += length * 4L
    }

    fun pushDoubles(a: DoubleArray, offset: Int = 0, length: Int = a.size - offset) {
        ensureCapacity(pointer + length * 8L)
        UNSAFE.copyMemory(a, DOUBLE_ARRAY_OFFSET + offset * 8L, null, address + pointer, length * 8L)
        pointer += length * 8L
    }

    fun trim() {
        reallocate(pointer)
    }

    override fun trim(size: Long) {
        reallocate(max(size, this.pointer))
    }

    fun clear() {
        pointer = 0
    }

    companion object {
        @JvmStatic
        fun wrap(buffer: Buffer): MemoryArray {
            require(buffer.isDirect) { "Buffer must be direct" }
            return wrap(buffer.address, buffer.byteCapacity)
        }

        @JvmStatic
        fun wrap(address: Long, length: Long): MemoryArray {
            return wrap(MemoryPointer.wrap(address, length))
        }

        @JvmStatic
        fun wrap(pointer: MemoryPointer, offset: Long = 0L, length: Long = pointer.length): MemoryArray {
            require(offset >= 0) { "Offset must be positive or zero" }
            require(length >= 0) { "Length must be positive or zero" }
            return WrappedMemoryArray(pointer, offset, length)
        }

        @JvmStatic
        fun malloc(length: Long): MemoryArray {
            require(length >= 0) { "Length must be positive or zero" }
            val memoryArray = MemoryArrayImpl(MemoryTracker.allocate(length), length)
            MemoryCleaner.register(memoryArray)
            return memoryArray
        }

        @JvmStatic
        fun calloc(length: Long): MemoryArray {
            val memoryList = malloc(length)
            UNSAFE.setMemory(memoryList.address, length, 0)
            return memoryList
        }
    }
}

internal class WrappedMemoryArray(var base: MemoryPointer, var offset: Long, override var length: Long) : MemoryArray {
    constructor(pointer: MemoryPointer) : this(pointer, 0L, pointer.length)

    override var address: Long
        get() = base.address + offset
        set(_) {
            throw UnsupportedOperationException()
        }

    override var pointer = 0L

    override fun reallocate(newLength: Long) {
        throw UnsupportedOperationException("Cannot reallocate a wrapped memory list")
    }

    override fun free() {
        throw UnsupportedOperationException("Cannot free a wrapped memory list")
    }

    override fun trim() {
        throw UnsupportedOperationException("Cannot trim a wrapped memory list")
    }

    override fun trim(size: Long) {
        throw UnsupportedOperationException("Cannot trim a wrapped memory list")
    }
}

internal class MemoryArrayImpl(address: Long, length: Long) : AddressHolder(address, length), MemoryArray {
    override var pointer = 0L
}

inline fun MemoryArray.forEachByteUnsafe(
    byteOffset: Long = 0L,
    length: Int = this.pointer.toInt(),
    block: (Byte) -> Unit
) {
    for (i in 0 until length) {
        block(getByte(byteOffset + i))
    }
}

inline fun MemoryArray.forEachByteIndexedUnsafe(
    byteOffset: Long = 0L,
    length: Int = this.pointer.toInt(),
    block: (Int, Byte) -> Unit
) {
    for (i in 0 until length) {
        block(i, getByte(byteOffset + i))
    }
}

inline fun MemoryArray.forEachShortUnsafe(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 2L).toInt(),
    block: (Short) -> Unit
) {
    for (i in 0 until length) {
        block(getShort(byteOffset + i * 2L))
    }
}

inline fun MemoryArray.forEachShortIndexedUnsafe(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 2L).toInt(),
    block: (Int, Short) -> Unit
) {
    for (i in 0 until length) {
        block(i, getShort(byteOffset + i * 2L))
    }
}

inline fun MemoryArray.forEachIntUnsafe(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 4L).toInt(),
    block: (Int) -> Unit
) {
    for (i in 0 until length) {
        block(getInt(byteOffset + i * 4L))
    }
}

inline fun MemoryArray.forEachIntIndexedUnsafe(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 4L).toInt(),
    block: (Int, Int) -> Unit
) {
    for (i in 0 until length) {
        block(i, getInt(byteOffset + i * 4L))
    }
}

inline fun MemoryArray.forEachLongUnsafe(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 8L).toInt(),
    block: (Long) -> Unit
) {
    for (i in 0 until length) {
        block(getLong(byteOffset + i * 8L))
    }
}

inline fun MemoryArray.forEachLongIndexedUnsafe(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 8L).toInt(),
    block: (Int, Long) -> Unit
) {
    for (i in 0 until length) {
        block(i, getLong(byteOffset + i * 8L))
    }
}

inline fun MemoryArray.forEachFloatUnsafe(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 4L).toInt(),
    block: (Float) -> Unit
) {
    for (i in 0 until length) {
        block(getFloat(byteOffset + i * 4L))
    }
}

inline fun MemoryArray.forEachFloatIndexedUnsafe(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 4L).toInt(),
    block: (Int, Float) -> Unit
) {
    for (i in 0 until length) {
        block(i, getFloat(byteOffset + i * 4L))
    }
}

inline fun MemoryArray.forEachDoubleUnsafe(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 8L).toInt(),
    block: (Double) -> Unit
) {
    for (i in 0 until length) {
        block(getDouble(byteOffset + i * 8L))
    }
}

inline fun MemoryArray.forEachDoubleIndexedUnsafe(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 8L).toInt(),
    block: (Int, Double) -> Unit
) {
    for (i in 0 until length) {
        block(i, getDouble(byteOffset + i * 8L))
    }
}


inline fun MemoryArray.forEachByte(
    byteOffset: Long = 0L,
    length: Int = this.pointer.toInt(),
    block: (Byte) -> Unit
) {
    checkForeachIndexRange(byteOffset, length, 1)
    forEachByteUnsafe(byteOffset, length, block)
}

inline fun MemoryArray.forEachByteIndexed(
    byteOffset: Long = 0L,
    length: Int = this.pointer.toInt(),
    block: (Int, Byte) -> Unit
) {
    checkForeachIndexRange(byteOffset, length, 1)
    forEachByteIndexedUnsafe(byteOffset, length, block)
}

inline fun MemoryArray.forEachShort(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 2L).toInt(),
    block: (Short) -> Unit
) {
    checkForeachIndexRange(byteOffset, length, 2)
    forEachShortUnsafe(byteOffset, length, block)
}

inline fun MemoryArray.forEachShortIndexed(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 2L).toInt(),
    block: (Int, Short) -> Unit
) {
    checkForeachIndexRange(byteOffset, length, 2)
    forEachShortIndexedUnsafe(byteOffset, length, block)
}

inline fun MemoryArray.forEachInt(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 4L).toInt(),
    block: (Int) -> Unit
) {
    checkForeachIndexRange(byteOffset, length, 4)
    forEachIntUnsafe(byteOffset, length, block)
}

inline fun MemoryArray.forEachIntIndexed(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 4L).toInt(),
    block: (Int, Int) -> Unit
) {
    checkForeachIndexRange(byteOffset, length, 4)
    forEachIntIndexedUnsafe(byteOffset, length, block)
}

inline fun MemoryArray.forEachLong(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 8L).toInt(),
    block: (Long) -> Unit
) {
    checkForeachIndexRange(byteOffset, length, 8)
    forEachLongUnsafe(byteOffset, length, block)
}

inline fun MemoryArray.forEachLongIndexed(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 8L).toInt(),
    block: (Int, Long) -> Unit
) {
    checkForeachIndexRange(byteOffset, length, 8)
    forEachLongIndexedUnsafe(byteOffset, length, block)
}

inline fun MemoryArray.forEachFloat(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 4L).toInt(),
    block: (Float) -> Unit
) {
    checkForeachIndexRange(byteOffset, length, 4)
    forEachFloatUnsafe(byteOffset, length, block)
}

inline fun MemoryArray.forEachFloatIndexed(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 4L).toInt(),
    block: (Int, Float) -> Unit
) {
    checkForeachIndexRange(byteOffset, length, 4)
    forEachFloatIndexedUnsafe(byteOffset, length, block)
}

inline fun MemoryArray.forEachDouble(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 8L).toInt(),
    block: (Double) -> Unit
) {
    checkForeachIndexRange(byteOffset, length, 8)
    forEachDoubleUnsafe(byteOffset, length, block)
}

inline fun MemoryArray.forEachDoubleIndexed(
    byteOffset: Long = 0L,
    length: Int = (this.pointer / 8L).toInt(),
    block: (Int, Double) -> Unit
) {
    checkForeachIndexRange(byteOffset, length, 8)
    forEachDoubleIndexedUnsafe(byteOffset, length, block)
}

fun MemoryArray.checkForeachIndexRange(byteOffset: Long, length: Int, unitSize: Int) {
    if (byteOffset < 0L || byteOffset > this.pointer) {
        throw IndexOutOfBoundsException("offset $byteOffset is out of bounds for length ${this.pointer}")
    }
    if (length < 0 || byteOffset + length * unitSize > this.pointer) {
        throw IndexOutOfBoundsException("length $length is out of bounds for length ${this.pointer}")
    }
}