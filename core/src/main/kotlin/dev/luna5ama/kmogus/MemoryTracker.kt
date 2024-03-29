package dev.luna5ama.kmogus

import java.util.concurrent.atomic.AtomicLong

object MemoryTracker {
    internal val counter = AtomicLong()

    val usedMemory get() = counter.get()

    internal fun allocate(size: Long): Ptr {
        require(size >= 0L) { "Invalid size" }
        if (size == 0L) return Ptr.NULL

        counter.addAndGet(size)
        return Ptr(UNSAFE.allocateMemory(size))
    }

    internal fun reallocate(ptr: Ptr, oldSize: Long, newSize: Long): Ptr {
        require(ptr.address >= 0L) { "Invalid address" }
        require(oldSize >= 0L) { "Invalid old size" }
        require(newSize >= 0L) { "Invalid new size" }

        return when {
            newSize == 0L -> {
                free(ptr, oldSize)
                Ptr.NULL
            }
            ptr.address == 0L -> {
                counter.addAndGet(newSize)
                Ptr(UNSAFE.allocateMemory(newSize))
            }
            else -> {
                require(oldSize != 0L) { "Invalid old size" }
                counter.addAndGet(newSize - oldSize)
                Ptr(UNSAFE.reallocateMemory(ptr.address, newSize))
            }
        }
    }

    internal fun free(ptr: Ptr, size: Long) {
        require(ptr.address >= 0L) { "Invalid address" }
        require(size >= 0L) { "Invalid size" }

        if (ptr.address == 0L) return
        require(size != 0L) { "Invalid size" }

        counter.addAndGet(-size)
        UNSAFE.freeMemory(ptr.address)
    }
}