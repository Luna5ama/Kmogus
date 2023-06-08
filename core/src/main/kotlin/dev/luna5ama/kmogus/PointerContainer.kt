package dev.luna5ama.kmogus

import java.nio.Buffer
import kotlin.math.max

interface PointerContainer : AutoCloseable {
    val pointer: Pointer
    val length: Long

    fun reallocate(newLength: Long, init: Boolean)
    fun free()

    override fun close() {
        free()
    }

    companion object {
        fun wrap(buffer: Buffer, offset: Long): PointerContainer {
            require(buffer.isDirect) { "ByteBuffer must be direct" }
            require(offset >= 0L) { "Invalid offset" }
            require(offset <= buffer.byteCapacity) { "Offset is greater than buffer capacity" }

            return wrap(buffer.address + offset, buffer.byteCapacity - offset)
        }

        fun wrap(buffer: Buffer): PointerContainer {
            require(buffer.isDirect) { "ByteBuffer must be direct" }

            return wrap(buffer.address, buffer.byteCapacity)
        }

        fun wrap(address: Long, length: Long): PointerContainer {
            require(address >= 0L) { "Invalid address" }
            require(length >= 0L) { "Invalid length" }
            val pointer = Pointer(address)
            if (length == 0L) return PointerContainerImpl(pointer, 0L)

            return PointerContainerWrapped(pointer, length)
        }

        fun malloc(length: Long): PointerContainer {
            require(length >= 0L) { "Invalid length" }
            if (length == 0L) return PointerContainerImpl(Pointer.NULL, 0L)

            val pointer = PointerContainerImpl(MemoryTracker.allocate(length), length)
            MemoryCleaner.register(pointer)

            return pointer
        }

        fun calloc(length: Long): PointerContainer {
            val container = malloc(length)
            container.pointer.setMemory(length, 0)
            return container
        }
    }
}

internal class PointerContainerWrapped(override val pointer: Pointer, override val length: Long) : PointerContainer {
    override fun reallocate(newLength: Long, init: Boolean) {
        throw UnsupportedOperationException("Cannot reallocate wrapped pointer")
    }

    override fun free() {
        throw UnsupportedOperationException("Cannot free wrapped pointer")
    }
}

internal class ContainerDelegated(@Volatile var pointer: Pointer, @Volatile var length: Long) {
    fun reallocate(newLength: Long, init: Boolean) {
        require(newLength >= 0) { "Length must be positive or zero" }

        synchronized(this) {
            if (newLength == length) return

            pointer = MemoryTracker.reallocate(pointer, length, newLength)
            if (init && newLength > length) {
                UNSAFE.setMemory(pointer.address + length, newLength - length, 0)
            }
            length = newLength
        }
    }

    fun free() {
        synchronized(this) {
            MemoryTracker.free(pointer, length)
            pointer = Pointer.NULL
            length = 0
        }
    }
}

internal class PointerContainerImpl(address: Pointer, length: Long) : PointerContainer {
    val delegated = ContainerDelegated(address, length)

    override val pointer: Pointer get() = delegated.pointer
    override val length: Long get() = delegated.length

    override fun reallocate(newLength: Long, init: Boolean) {
        delegated.reallocate(newLength, init)
    }

    override fun free() {
        delegated.free()
    }
}


fun PointerContainer.ensureCapacity(capacity: Long, init: Boolean) {
    if (capacity > length) reallocate(max(capacity, length * 2), init)
}

operator fun PointerContainer.plus(offset: Long) = pointer + offset

operator fun PointerContainer.minus(offset: Long) = pointer - offset