package dev.luna5ama.kmogus

import java.util.concurrent.atomic.AtomicLong

object MemoryTracker {
    internal val counter = AtomicLong()

    val usedMemory get() = counter.get()

    internal fun allocate(size: Long): Long {
        require(size >= 0L) { "Invalid size" }
        if (size == 0L) return 0L

        counter.addAndGet(size)
        return UNSAFE.allocateMemory(size)
    }

    internal fun reallocate(address: Long, oldSize: Long, newSize: Long): Long {
        require(address >= 0L) { "Invalid address" }
        require(oldSize >= 0L) { "Invalid old size" }
        require(newSize >= 0L) { "Invalid new size" }

        return when {
            newSize == 0L -> {
                free(address, oldSize)
                0L
            }
            address == 0L -> {
                UNSAFE.allocateMemory(newSize)
            }
            else -> {
                require(oldSize != 0L) { "Invalid old size" }
                counter.addAndGet(newSize - oldSize)
                UNSAFE.reallocateMemory(address, newSize)
            }
        }
    }

    internal fun free(address: Long, size: Long) {
        require(address >= 0L) { "Invalid address" }
        require(size >= 0L) { "Invalid size" }

        if (address == 0L) return
        require(size != 0L) { "Invalid size" }

        counter.addAndGet(-size)
        UNSAFE.freeMemory(address)
    }
}