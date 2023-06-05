package dev.luna5ama.kmogus

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MemoryStackTest {
    @BeforeEach
    fun setup() {
        assertDoesNotThrow("MemoryStack is not empty at the start") {
            MemoryStack.get().checkEmpty()
        }
    }

    @AfterEach
    fun teardown() {
        assertDoesNotThrow("MemoryStack is not empty at the end") {
            MemoryStack.get().checkEmpty()
        }
    }

    @Test
    fun address() {
        MemoryStack {
            val a = malloc(4)
            val b = malloc(1)
            val c = malloc(15)
            val d = malloc(8)

            assertEquals(a.address + 4, b.address, "b.address != a.address + 4")
            assertEquals(b.address + 1, c.address, "c.address != b.address + 1")
            assertEquals(c.address + 15, d.address, "d.address != c.address + 15")

            assertEquals(4, a.length, "a.length != 4")
            assertEquals(1, b.length, "b.length != 1")
            assertEquals(15, c.length, "c.length != 15")
        }
    }

    @Test
    fun illegalFree() {
        MemoryStack {
            val ptr = malloc(4)

            assertFailsWith(UnsupportedOperationException::class) {
                ptr.free()
            }
        }
    }

    @Test
    fun pop() {
        MemoryStack {
            malloc(4)
            malloc(8)
            malloc(12)
        }
    }

    @Test
    fun cascadeStack() {
        MemoryStack {
            malloc(4)
            malloc(8)
            malloc(12)

            assertFailsWith(IllegalStateException::class) {
                MemoryStack.get().checkEmpty()
            }

            MemoryStack {
                malloc(8)
                malloc(16)
                malloc(32)

                assertFailsWith(IllegalStateException::class) {
                    MemoryStack.get().checkEmpty()
                }

                MemoryStack {
                    malloc(16)
                    malloc(32)
                    malloc(64)

                    assertFailsWith(IllegalStateException::class) {
                        MemoryStack.get().checkEmpty()
                    }
                }
            }
        }
    }

    @Test
    fun calloc() {
        MemoryStack {
            val pointer = calloc(1024)
            for (i in 0 until 1024) {
                assertEquals(0, UNSAFE.getByte(pointer.address + i), "Byte at index $i is not 0")
            }
        }
    }

    @Test
    fun reallocExpandNoInit() {
        MemoryStack {
            val pointer = malloc(4)
            UNSAFE.putByte(pointer.address, 69)
            UNSAFE.putByte(pointer.address + 1, 42)
            UNSAFE.putByte(pointer.address + 2, -1)
            UNSAFE.putByte(pointer.address + 3, -69)

            pointer.reallocate(8, false)
            assertEquals(8, pointer.length, "Expected new length to be 8")
            assertEquals(69, UNSAFE.getByte(pointer.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(pointer.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(pointer.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(pointer.address + 3), "Byte at index 3 is modified")
        }
    }

    @Test
    fun reallocExpandInit() {
        MemoryStack {
            val pointer = malloc(4)
            UNSAFE.putByte(pointer.address, 69)
            UNSAFE.putByte(pointer.address + 1, 42)
            UNSAFE.putByte(pointer.address + 2, -1)
            UNSAFE.putByte(pointer.address + 3, -69)

            pointer.reallocate(8, true)
            assertEquals(8, pointer.length, "Expected new length to be 8")
            assertEquals(69, UNSAFE.getByte(pointer.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(pointer.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(pointer.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(pointer.address + 3), "Byte at index 3 is modified")
            assertEquals(0, UNSAFE.getByte(pointer.address + 4), "Byte at index 4 is not 0")
            assertEquals(0, UNSAFE.getByte(pointer.address + 5), "Byte at index 5 is not 0")
            assertEquals(0, UNSAFE.getByte(pointer.address + 6), "Byte at index 6 is not 0")
            assertEquals(0, UNSAFE.getByte(pointer.address + 7), "Byte at index 7 is not 0")
        }
    }

    @Test
    fun reallocShrinkNoInit() {
        MemoryStack {
            val pointer = malloc(8)
            val prevAddress = pointer.address

            UNSAFE.putByte(pointer.address, 69)
            UNSAFE.putByte(pointer.address + 1, 42)
            UNSAFE.putByte(pointer.address + 2, -1)
            UNSAFE.putByte(pointer.address + 3, -69)
            UNSAFE.putByte(pointer.address + 4, 69)
            UNSAFE.putByte(pointer.address + 5, 42)
            UNSAFE.putByte(pointer.address + 6, -1)
            UNSAFE.putByte(pointer.address + 7, -69)

            pointer.reallocate(4, false)

            assertEquals(prevAddress, pointer.address, "Expected realloc in place, but got a new address")
            assertEquals(4, pointer.length, "Expected new length to be 4")
            assertEquals(69, UNSAFE.getByte(pointer.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(pointer.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(pointer.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(pointer.address + 3), "Byte at index 3 is modified")
        }
    }

    @Test
    fun reallocShrinkInit() {
        MemoryStack {
            val pointer = malloc(8)
            val prevAddress = pointer.address

            UNSAFE.putByte(pointer.address, 69)
            UNSAFE.putByte(pointer.address + 1, 42)
            UNSAFE.putByte(pointer.address + 2, -1)
            UNSAFE.putByte(pointer.address + 3, -69)
            UNSAFE.putByte(pointer.address + 4, 69)
            UNSAFE.putByte(pointer.address + 5, 42)
            UNSAFE.putByte(pointer.address + 6, -1)
            UNSAFE.putByte(pointer.address + 7, -69)

            pointer.reallocate(4, true)

            assertEquals(prevAddress, pointer.address, "Expected realloc in place, but got a new address")
            assertEquals(4, pointer.length, "Expected new length to be 4")
            assertEquals(69, UNSAFE.getByte(pointer.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(pointer.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(pointer.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(pointer.address + 3), "Byte at index 3 is modified")
        }
    }
}