package dev.luna5ama.kmogus

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MemoryArrayTest {
    @Test
    fun asArray() {
        val pointer = MemoryPointer.malloc(4)

        val array = pointer.asArray()
        assertEquals(0L, array.offset, "Expected initial offset to be 0")
        assertEquals(pointer.address, array.address, "Expected address to be the same")
        assertEquals(pointer.length, array.length, "Expected length to be the same")

        pointer.free()
    }

    @Test
    fun offset() {
        val pointer = MemoryPointer.malloc(420)
        val array = pointer.asArray()

        array.offset = 69
        assertEquals(69L, array.offset, "Expected offset to be 69")

        array.offset(Span(pointer.address + 420))
        assertEquals(420L, array.offset, "Expected offset to be 420")

        array.reset()
        assertEquals(0L, array.offset, "Expected offset to be 0")

        array.offset += 114
        assertEquals(114L, array.offset, "Expected offset to be 114")

        array.offset -= 14
        assertEquals(100L, array.offset, "Expected offset to be 100")

        array += 69
        assertEquals(169L, array.offset, "Expected offset to be 169")

        array -= 42
        assertEquals(127L, array.offset, "Expected offset to be 127")

        pointer.free()
    }
}