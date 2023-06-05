package dev.luna5ama.kmogus

import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class MemoryPointerTest {
    @Test
    fun mallocEmpty() {
        val pointer = MemoryPointer.malloc(0)
        assertEquals(0L, pointer.address, "Empty pointer should have address 0")
        assertEquals(0L, pointer.length, "Empty pointer should have length 0")

        pointer.free()
    }

    @Test
    fun mallocNonEmpty() {
        val pointer = MemoryPointer.malloc(69)
        assertNotEquals(0L, pointer.address, "Non-empty pointer should have non-zero address")
        assertEquals(69L, pointer.length, "Expected length 69")

        pointer.free()
    }

    @Test
    fun mallocIllegal() {
        assertFailsWith(IllegalArgumentException::class) {
            MemoryPointer.malloc(-1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            MemoryPointer.malloc(-999)
        }
        assertFailsWith(IllegalArgumentException::class) {
            MemoryPointer.malloc(Int.MIN_VALUE.toLong())
        }
        assertFailsWith(IllegalArgumentException::class) {
            MemoryPointer.malloc(Long.MIN_VALUE)
        }
    }

    @Test
    fun callocEmpty() {
        val pointer = MemoryPointer.calloc(0)
        assertEquals(0L, pointer.address, "Empty pointer should have address 0")
        assertEquals(0L, pointer.length, "Empty pointer should have length 0")

        pointer.free()
    }

    @Test
    fun callocNonEmpty() {
        val pointer = MemoryPointer.calloc(69)
        assertNotEquals(0L, pointer.address, "Non-empty pointer should have non-zero address")
        assertEquals(69L, pointer.length, "Expected length 69")

        for (i in 0 until 69) {
            assertEquals(0, UNSAFE.getByte(pointer.address + i), "Byte at index $i is not 0")
        }

        pointer.free()
    }

    @Test
    fun callocIllegal() {
        assertFailsWith(IllegalArgumentException::class) {
            MemoryPointer.calloc(-1)
        }
        assertFailsWith(IllegalArgumentException::class) {
            MemoryPointer.calloc(-999)
        }
        assertFailsWith(IllegalArgumentException::class) {
            MemoryPointer.calloc(Int.MIN_VALUE.toLong())
        }
        assertFailsWith(IllegalArgumentException::class) {
            MemoryPointer.calloc(Long.MIN_VALUE)
        }
    }

    @Test
    fun reallocWith0NoInit() {
        val pointer = MemoryPointer.malloc(0)
        pointer.reallocate(8, false)
        assertNotEquals(0L, pointer.address, "Non-empty pointer should have non-zero address")
        assertEquals(8L, pointer.length, "Expected length 8")
        pointer.free()
    }

    @Test
    fun reallocWith0Init() {
        val pointer = MemoryPointer.malloc(0)
        pointer.reallocate(8, true)
        assertNotEquals(0L, pointer.address, "Non-empty pointer should have non-zero address")
        assertEquals(8L, pointer.length, "Expected length 8")

        for (i in 0 until 8) {
            assertEquals(0, UNSAFE.getByte(pointer.address + i), "Byte at index $i is not 0")
        }

        pointer.free()
    }

    @Test
    fun reallocExpandNoInit() {
        val pointer = MemoryPointer.malloc(4)
        UNSAFE.putByte(pointer.address, 69)
        UNSAFE.putByte(pointer.address + 1, 42)
        UNSAFE.putByte(pointer.address + 2, -1)
        UNSAFE.putByte(pointer.address + 3, 5)

        pointer.reallocate(8, false)
        assertEquals(8L, pointer.length, "Expected length 8")
        assertEquals(69, UNSAFE.getByte(pointer.address), "Byte at index 0 is not 69")
        assertEquals(42, UNSAFE.getByte(pointer.address + 1), "Byte at index 1 is not 42")
        assertEquals(-1, UNSAFE.getByte(pointer.address + 2), "Byte at index 2 is not -1")
        assertEquals(5, UNSAFE.getByte(pointer.address + 3), "Byte at index 3 is not 5")

        pointer.free()
    }

    @Test
    fun reallocExpandInit() {
        val pointer = MemoryPointer.malloc(4)
        UNSAFE.putByte(pointer.address, 69)
        UNSAFE.putByte(pointer.address + 1, 42)
        UNSAFE.putByte(pointer.address + 2, -1)
        UNSAFE.putByte(pointer.address + 3, 5)

        pointer.reallocate(8, true)
        assertEquals(8L, pointer.length, "Expected length 8")
        assertEquals(69, UNSAFE.getByte(pointer.address), "Byte at index 0 is not 69")
        assertEquals(42, UNSAFE.getByte(pointer.address + 1), "Byte at index 1 is not 42")
        assertEquals(-1, UNSAFE.getByte(pointer.address + 2), "Byte at index 2 is not -1")
        assertEquals(5, UNSAFE.getByte(pointer.address + 3), "Byte at index 3 is not 5")

        assertEquals(0, UNSAFE.getByte(pointer.address + 4), "Byte at index 4 is not 0")
        assertEquals(0, UNSAFE.getByte(pointer.address + 5), "Byte at index 5 is not 0")
        assertEquals(0, UNSAFE.getByte(pointer.address + 6), "Byte at index 6 is not 0")
        assertEquals(0, UNSAFE.getByte(pointer.address + 7), "Byte at index 7 is not 0")

        pointer.free()
    }

    @Test
    fun reallocShrinkNoInit() {
        val pointer = MemoryPointer.malloc(4)
        UNSAFE.putByte(pointer.address, 69)
        UNSAFE.putByte(pointer.address + 1, 42)
        UNSAFE.putByte(pointer.address + 2, -1)
        UNSAFE.putByte(pointer.address + 3, 5)

        pointer.reallocate(2, false)
        assertEquals(2L, pointer.length, "Expected length 8")
        assertEquals(69, UNSAFE.getByte(pointer.address), "Byte at index 0 is not 69")
        assertEquals(42, UNSAFE.getByte(pointer.address + 1), "Byte at index 1 is not 42")

        pointer.free()
    }

    @Test
    fun reallocShrinkInit() {
        val pointer = MemoryPointer.malloc(4)
        UNSAFE.putByte(pointer.address, 69)
        UNSAFE.putByte(pointer.address + 1, 42)
        UNSAFE.putByte(pointer.address + 2, -1)
        UNSAFE.putByte(pointer.address + 3, 5)

        pointer.reallocate(2, true)
        assertEquals(2L, pointer.length, "Expected length 8")
        assertEquals(69, UNSAFE.getByte(pointer.address), "Byte at index 0 is not 69")
        assertEquals(42, UNSAFE.getByte(pointer.address + 1), "Byte at index 1 is not 42")

        pointer.free()
    }

    @Test
    fun reallocTo0NoInit() {
        val pointer = MemoryPointer.malloc(4)
        UNSAFE.putByte(pointer.address, 69)
        UNSAFE.putByte(pointer.address + 1, 42)
        UNSAFE.putByte(pointer.address + 2, -1)
        UNSAFE.putByte(pointer.address + 3, 5)

        pointer.reallocate(0, true)
        assertEquals(0L, pointer.address, "Expected address 0")
        assertEquals(0L, pointer.length, "Expected length 0")

        pointer.free()
    }

    @Test
    fun reallocTo0Init() {
        val pointer = MemoryPointer.malloc(4)
        UNSAFE.putByte(pointer.address, 69)
        UNSAFE.putByte(pointer.address + 1, 42)
        UNSAFE.putByte(pointer.address + 2, -1)
        UNSAFE.putByte(pointer.address + 3, 5)

        pointer.reallocate(0, true)
        assertEquals(0L, pointer.address, "Expected address 0")
        assertEquals(0L, pointer.length, "Expected length 0")

        pointer.free()
    }

    @Test
    fun reallocIllegal() {
        val pointer = MemoryPointer.malloc(4)

        assertFailsWith(IllegalArgumentException::class) {
            pointer.reallocate(-1, true)
        }

        assertFailsWith(IllegalArgumentException::class) {
            pointer.reallocate(-1, false)
        }

        pointer.free()
    }

    @Test
    fun wrappedPointer() {
        val pointer = UNSAFE.allocateMemory(4)

        val wrapped = MemoryPointer.wrap(pointer, 4)
        assertEquals(pointer, wrapped.address, "Expected address: 0x%016X, actual: 0x%016X".format(pointer, wrapped.address))
        assertEquals(4L, wrapped.length, "Expected length 4")

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.free()
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(8, true)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(8, false)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(1, true)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(1, false)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(0, true)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(0, false)
        }

        UNSAFE.freeMemory(pointer)
    }

    @Test
    fun wrappedBuffer() {
        val buffer = ByteBuffer.allocateDirect(8)
        buffer.put(4, 1)
        buffer.put(5, 2)
        buffer.put(6, 3)
        buffer.put(7, 4)

        val wrapped = MemoryPointer.wrap(buffer)
        assertEquals(buffer.address, wrapped.address, "Expected address: 0x%016X, actual: 0x%016X".format(buffer.address, wrapped.address))
        assertEquals(8L, wrapped.length, "Expected length 8")

        UNSAFE.putByte(wrapped.address, 69)
        UNSAFE.putByte(wrapped.address + 1, 42)
        UNSAFE.putByte(wrapped.address + 2, -1)
        UNSAFE.putByte(wrapped.address + 3, 5)

        assertEquals(69, buffer.get(0), "Byte at index 0 is not 69")
        assertEquals(42, buffer.get(1), "Byte at index 1 is not 42")
        assertEquals(-1, buffer.get(2), "Byte at index 2 is not -1")
        assertEquals(5, buffer.get(3), "Byte at index 3 is not 5")

        assertEquals(1, buffer.get(4), "Byte at index 4 was modified")
        assertEquals(2, buffer.get(5), "Byte at index 5 was modified")
        assertEquals(3, buffer.get(6), "Byte at index 6 was modified")
        assertEquals(4, buffer.get(7), "Byte at index 7 was modified")

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.free()
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(16, true)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(16, false)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(8, true)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(8, false)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(1, true)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(1, false)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(0, true)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(0, false)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(-1, true)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(-1, false)
        }
    }

    @Test
    fun wrappedBufferOffset() {
        val buffer = ByteBuffer.allocateDirect(8)
        buffer.put(0, 1)
        buffer.put(1, 2)
        buffer.put(2, 3)
        buffer.put(3, 4)

        val wrapped = MemoryPointer.wrap(buffer, 4L)
        assertEquals(buffer.address + 4L, wrapped.address, "Expected address: 0x%016X, actual: 0x%016X".format(buffer.address + 4L, wrapped.address))
        assertEquals(4L, wrapped.length, "Expected length 4")

        UNSAFE.putByte(wrapped.address, 69)
        UNSAFE.putByte(wrapped.address + 1, 42)
        UNSAFE.putByte(wrapped.address + 2, -1)
        UNSAFE.putByte(wrapped.address + 3, 5)

        assertEquals(69, buffer.get(4), "Byte at index 4 is not 69")
        assertEquals(42, buffer.get(5), "Byte at index 5 is not 42")
        assertEquals(-1, buffer.get(6), "Byte at index 6 is not -1")
        assertEquals(5, buffer.get(7), "Byte at index 7 is not 5")

        assertEquals(1, buffer.get(0), "Byte at index 0 was modified")
        assertEquals(2, buffer.get(1), "Byte at index 1 was modified")
        assertEquals(3, buffer.get(2), "Byte at index 2 was modified")
        assertEquals(4, buffer.get(3), "Byte at index 3 was modified")

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.free()
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(16, true)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(16, false)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(8, true)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(8, false)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(1, true)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(1, false)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(0, true)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(0, false)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(-1, true)
        }

        assertFailsWith(UnsupportedOperationException::class) {
            wrapped.reallocate(-1, false)
        }
    }
}