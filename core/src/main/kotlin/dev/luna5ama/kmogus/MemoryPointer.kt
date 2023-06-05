package dev.luna5ama.kmogus

import java.nio.Buffer
import kotlin.math.max

interface MemoryPointer : AutoCloseable {
    val address: Long
    val length: Long

    fun reallocate(newLength: Long, init: Boolean)
    fun free()

    override fun close() {
        free()
    }

    fun ensureCapacity(capacity: Long, init: Boolean) {
        if (capacity > length) reallocate(max(capacity, length * 2), init)
    }

    companion object {
        fun wrap(buffer: Buffer, offset: Long): MemoryPointer {
            require(buffer.isDirect) { "ByteBuffer must be direct" }
            require(offset >= 0L) { "Invalid offset" }
            require(offset <= buffer.byteCapacity) { "Offset is greater than buffer capacity" }
            return wrap(buffer.address + offset, buffer.byteCapacity - offset)
        }

        fun wrap(buffer: Buffer): MemoryPointer {
            require(buffer.isDirect) { "ByteBuffer must be direct" }
            return wrap(buffer.address, buffer.byteCapacity)
        }

        fun wrap(address: Long, length: Long): MemoryPointer {
            require(address >= 0L) { "Invalid address" }
            require(length >= 0L) { "Invalid length" }
            if (length == 0L) return MemoryPointerImpl(0L, 0L)
            return MemoryPointerWrapped(address, length)
        }

        fun malloc(length: Long): MemoryPointer {
            require(length >= 0L) { "Invalid length" }
            if (length == 0L) return MemoryPointerImpl(0L, 0L)
            val pointer = MemoryPointerImpl(MemoryTracker.allocate(length), length)
            MemoryCleaner.register(pointer)
            return pointer
        }

        fun calloc(length: Long): MemoryPointer {
            val pointer = malloc(length)
            UNSAFE.setMemory(pointer.address, length, 0)
            return pointer
        }
    }
}

internal class MemoryPointerWrapped(override val address: Long, override val length: Long) : MemoryPointer {
    override fun reallocate(newLength: Long, init: Boolean) {
        throw UnsupportedOperationException("Cannot reallocate wrapped pointer")
    }

    override fun free() {
        throw UnsupportedOperationException("Cannot free wrapped pointer")
    }
}

internal class PointerContainer(@Volatile var address: Long, @Volatile var length: Long) {
    fun reallocate(newLength: Long, init: Boolean) {
        require(newLength >= 0) { "Length must be positive or zero" }

        synchronized(this) {
            if (newLength == length) return

            address = MemoryTracker.reallocate(address, length, newLength)
            if (init && newLength > length) {
                UNSAFE.setMemory(address + length, newLength - length, 0)
            }
            length = newLength
        }
    }

    fun free() {
        synchronized(this) {
            MemoryTracker.free(address, length)
            address = 0
            length = 0
        }
    }
}

internal class MemoryPointerImpl(address: Long, length: Long) : MemoryPointer {
    val container = PointerContainer(address, length)

    override val address: Long get() = container.address
    override val length: Long get() = container.length

    override fun reallocate(newLength: Long, init: Boolean) {
        container.reallocate(newLength, init)
    }

    override fun free() {
        container.free()
    }
}