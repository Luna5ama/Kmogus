package dev.luna5ama.kmogus

import java.util.concurrent.atomic.AtomicLong

object MemoryTracker {
    internal val counter = AtomicLong()

    val usedMemory get() = counter.get()

    internal fun allocate(size: Long): Pointer {
        require(size >= 0L) { "Invalid size" }
        if (size == 0L) return Pointer.NULL

        counter.addAndGet(size)
        return Pointer(UNSAFE.allocateMemory(size))
    }

    internal fun reallocate(pointer: Pointer, oldSize: Long, newSize: Long): Pointer {
        require(pointer.address >= 0L) { "Invalid address" }
        require(oldSize >= 0L) { "Invalid old size" }
        require(newSize >= 0L) { "Invalid new size" }

        return when {
            newSize == 0L -> {
                free(pointer, oldSize)
                Pointer.NULL
            }
            pointer.address == 0L -> {
                Pointer(UNSAFE.allocateMemory(newSize))
            }
            else -> {
                require(oldSize != 0L) { "Invalid old size" }
                counter.addAndGet(newSize - oldSize)
                Pointer(UNSAFE.reallocateMemory(pointer.address, newSize))
            }
        }
    }

    internal fun free(pointer: Pointer, size: Long) {
        require(pointer.address >= 0L) { "Invalid address" }
        require(size >= 0L) { "Invalid size" }

        if (pointer.address == 0L) return
        require(size != 0L) { "Invalid size" }

        counter.addAndGet(-size)
        UNSAFE.freeMemory(pointer.address)
    }
}