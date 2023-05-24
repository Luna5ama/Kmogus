package dev.luna5ama.kmogus

import java.util.*
import kotlin.test.Test

class MemoryCleanerTest {
    @Test
    fun testManualClean() {
        MemoryTracker.counter.set(0L)
        assert(MemoryTracker.usedMemory == 0L)

        val array1 = Array(32) { MemoryPointer.malloc(1024L * 1024L) }
        assert(MemoryTracker.usedMemory == 32L * 1024L * 1024L)

        val array2 = Array(16) { MemoryArray.malloc(1024L * 1024L) }
        assert(MemoryTracker.usedMemory == 48L * 1024L * 1024L)

        array1.forEach { it.free() }
        assert(MemoryTracker.usedMemory == 16L * 1024L * 1024L)

        array2.forEach { it.free() }
        assert(MemoryTracker.usedMemory == 0L)
    }

    @Test
    fun testAutoClean() {
        MemoryTracker.counter.set(0L)
        assert(MemoryTracker.usedMemory == 0L)

        val array1 = Array(32) { MemoryPointer.malloc(1024L * 1024L) }
        assert(MemoryTracker.usedMemory == 32 * 1024L * 1024L)
        val array2 = Array(16) { MemoryArray.malloc(1024L * 1024L) }
        assert(MemoryTracker.usedMemory == 48 * 1024L * 1024L)

        Arrays.fill(array1, null)
        repeat(20) {
            System.gc()
            Thread.sleep(50L)
        }
        assert(MemoryTracker.usedMemory == 16 * 1024L * 1024L)

        Arrays.fill(array2, null)
        repeat(20) {
            System.gc()
            Thread.sleep(50L)
        }
        assert(MemoryTracker.usedMemory == 0L)
    }
}