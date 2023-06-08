package dev.luna5ama.kmogus

import org.junit.jupiter.api.BeforeEach
import java.util.*
import kotlin.test.Test

class MemoryCleanerTest {
    @BeforeEach
    fun setup() {
        MemoryTracker.counter.set(0L)
        assert(MemoryTracker.usedMemory == 0L)
    }

    @Test
    fun testManualClean() {
        val array1 = Array(32) { PointerContainer.malloc(1024L * 1024L) }
        assert(MemoryTracker.usedMemory == 32L * 1024L * 1024L)

        val array2 = Array(16) { PointerContainer.malloc(1024L * 1024L) }
        assert(MemoryTracker.usedMemory == 48L * 1024L * 1024L)

        array1.forEach { it.free() }
        assert(MemoryTracker.usedMemory == 16L * 1024L * 1024L)

        array2.forEach { it.free() }
        assert(MemoryTracker.usedMemory == 0L)
    }

    @Test
    fun testManualCleanWithClose() {
        val array1 = Array(32) { PointerContainer.malloc(1024L * 1024L) }
        assert(MemoryTracker.usedMemory == 32L * 1024L * 1024L)

        val array2 = Array(16) { PointerContainer.malloc(1024L * 1024L) }
        assert(MemoryTracker.usedMemory == 48L * 1024L * 1024L)

        array1.forEach { it.close() }
        assert(MemoryTracker.usedMemory == 16L * 1024L * 1024L)

        array2.forEach { it.close() }
        assert(MemoryTracker.usedMemory == 0L)
    }

    @Test
    fun testAutoClean() {
        val array1 = Array(32) { PointerContainer.malloc(1024L * 1024L) }
        assert(MemoryTracker.usedMemory == 32 * 1024L * 1024L)
        val array2 = Array(16) { PointerContainer.malloc(1024L * 1024L) }
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