package dev.luna5ama.kmogus

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MutableArrTest {
    @Test
    fun asArray() {
        val pointer = Arr.malloc(4)

        val array = pointer.asMutable()
        assertEquals(0L, array.pos, "Expected initial offset to be 0")
        assertEquals(pointer.ptr, array.ptr, "Expected address to be the same")
        assertEquals(pointer.len, array.len, "Expected length to be the same")

        pointer.free()
    }

    @Test
    fun offset() {
        val pointer = Arr.malloc(420)
        val array = pointer.asMutable()

        array.pos = 69
        assertEquals(69L, array.pos, "Expected offset to be 69")

        array.pos(pointer.ptr + 420)
        assertEquals(420L, array.pos, "Expected offset to be 420")

        array.reset()
        assertEquals(0L, array.pos, "Expected offset to be 0")

        array.pos += 114
        assertEquals(114L, array.pos, "Expected offset to be 114")

        array.pos -= 14
        assertEquals(100L, array.pos, "Expected offset to be 100")

        array += 69
        assertEquals(169L, array.pos, "Expected offset to be 169")

        array -= 42
        assertEquals(127L, array.pos, "Expected offset to be 127")

        pointer.free()
    }
}