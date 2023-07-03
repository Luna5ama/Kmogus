package dev.luna5ama.kmogus

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MutablePointerContainerTest {
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

    @Test
    fun offsetPointer() {
        val container = Arr.malloc(8).asMutable()

        assertEquals(container.basePtr.address + 4, (container + 4).address, "Expected address to be address + 4")
        assertEquals(container.basePtr.address - 4, (container - 4).address, "Expected address to be address - 4")

        assertEquals(
            container.basePtr.address + 69420,
            (container + 69420).address,
            "Expected address to be address + 4"
        )
        assertEquals(
            container.basePtr.address - 69420,
            (container - 69420).address,
            "Expected address to be address - 4"
        )

        container.free()
    }

    @Test
    fun offsetPointerOnOffset() {
        val container = Arr.malloc(8).asMutable()

        container.pos += 69

        assertEquals(container.basePtr.address + 73, (container + 4).address, "Expected address to be address + 4")
        assertEquals(container.basePtr.address + 65, (container - 4).address, "Expected address to be address - 4")

        container.pos += 114514

        assertEquals(
            container.basePtr.address + 6969420,
            (container + 6854837).address,
            "Expected address to be address + 4"
        )
        assertEquals(
            container.basePtr.address - 114514,
            (container - 229097).address,
            "Expected address to be address - 4"
        )


        container.free()
    }
}