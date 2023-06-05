package dev.luna5ama.kmogus

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MemoryTrackerTest {
    @Test
    fun test() {
        MemoryTracker.counter.set(0L)
        assertEquals(0, MemoryTracker.usedMemory)

        val manual = MemoryTracker.allocate(420)
        assertEquals(420, MemoryTracker.usedMemory)

        val pointer = MemoryPointer.malloc(69)
        assertEquals(489, MemoryTracker.usedMemory)

        MemoryTracker.free(manual, 420)
        assertEquals(69, MemoryTracker.usedMemory)

        pointer.free()
        assertEquals(0, MemoryTracker.usedMemory)
    }
}