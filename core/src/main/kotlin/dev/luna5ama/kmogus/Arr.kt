package dev.luna5ama.kmogus

import java.nio.Buffer
import kotlin.math.max

interface Arr : AutoCloseable {
    val ptr: Ptr
    val len: Long

    fun realloc(newLength: Long, init: Boolean)
    fun free()

    override fun close() {
        free()
    }

    companion object {
        fun wrap(buffer: Buffer, offset: Long): Arr {
            require(buffer.isDirect) { "ByteBuffer must be direct" }
            require(offset >= 0L) { "Invalid offset" }
            require(offset <= buffer.byteCapacity) { "Offset is greater than buffer capacity" }

            return wrap(buffer.address + offset, buffer.byteCapacity - offset)
        }

        fun wrap(buffer: Buffer): Arr {
            require(buffer.isDirect) { "ByteBuffer must be direct" }

            return wrap(buffer.address, buffer.byteCapacity)
        }

        fun wrap(address: Long, length: Long): Arr {
            require(address >= 0L) { "Invalid address" }
            require(length >= 0L) { "Invalid length" }
            val ptr = Ptr(address)
            if (length == 0L) return ArrImpl(ptr, 0L)

            return ArrWrapped(ptr, length)
        }

        fun malloc(length: Long): Arr {
            require(length >= 0L) { "Invalid length" }
            if (length == 0L) return ArrImpl(Ptr.NULL, 0L)

            val pointer = ArrImpl(MemoryTracker.allocate(length), length)
            MemoryCleaner.register(pointer)

            return pointer
        }

        fun calloc(length: Long): Arr {
            val container = malloc(length)
            container.ptr.setMemory(length, 0)
            return container
        }
    }
}

internal class ArrWrapped(override val ptr: Ptr, override val len: Long) : Arr {
    override fun realloc(newLength: Long, init: Boolean) {
        throw UnsupportedOperationException("Cannot reallocate wrapped ptr")
    }

    override fun free() {
        throw UnsupportedOperationException("Cannot free wrapped ptr")
    }
}

internal class ContainerDelegated(@Volatile var ptr: Ptr, @Volatile var length: Long) {
    fun realloc(newLength: Long, init: Boolean) {
        require(newLength >= 0) { "Length must be positive or zero" }

        synchronized(this) {
            if (newLength == length) return

            ptr = MemoryTracker.reallocate(ptr, length, newLength)
            if (init && newLength > length) {
                UNSAFE.setMemory(ptr.address + length, newLength - length, 0)
            }
            length = newLength
        }
    }

    fun free() {
        synchronized(this) {
            MemoryTracker.free(ptr, length)
            ptr = Ptr.NULL
            length = 0
        }
    }
}

internal class ArrImpl(address: Ptr, length: Long) : Arr {
    val delegated = ContainerDelegated(address, length)

    override val ptr: Ptr get() = delegated.ptr
    override val len: Long get() = delegated.length

    override fun realloc(newLength: Long, init: Boolean) {
        delegated.realloc(newLength, init)
    }

    override fun free() {
        delegated.free()
    }
}


fun Arr.ensureCapacity(capacity: Long, init: Boolean) {
    if (capacity > len) realloc(max(capacity, len * 2), init)
}

operator fun Arr.plus(offset: Long) = ptr + offset

operator fun Arr.minus(offset: Long) = ptr - offset