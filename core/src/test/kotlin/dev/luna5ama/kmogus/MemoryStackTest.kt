package dev.luna5ama.kmogus

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

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

            assertEquals(a.pointer.address + 4, b.pointer.address, "b.address != a.address + 4")
            assertEquals(b.pointer.address + 1, c.pointer.address, "c.address != b.address + 1")
            assertEquals(c.pointer.address + 15, d.pointer.address, "d.address != c.address + 15")

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
            val container = calloc(1024)
            for (i in 0 until 1024) {
                assertEquals(0, UNSAFE.getByte(container.pointer.address + i), "Byte at index $i is not 0")
            }
        }
    }

    @Test
    fun reallocInPlaceExpandNoInit() {
        MemoryStack {
            val container = malloc(4)
            val prevAddress = container.pointer.address

            UNSAFE.putByte(container.pointer.address, 69)
            UNSAFE.putByte(container.pointer.address + 1, 42)
            UNSAFE.putByte(container.pointer.address + 2, -1)
            UNSAFE.putByte(container.pointer.address + 3, -69)

            container.reallocate(8, false)
            assertEquals(prevAddress, container.pointer.address, "Expected realloc in place, but got a new address")
            assertEquals(8, container.length, "Expected new length to be 8")
            assertEquals(69, UNSAFE.getByte(container.pointer.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(container.pointer.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(container.pointer.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(container.pointer.address + 3), "Byte at index 3 is modified")
        }
    }

    @Test
    fun reallocInPlaceExpandInit() {
        MemoryStack {
            val container = malloc(4)
            val prevAddress = container.pointer.address

            UNSAFE.putByte(container.pointer.address, 69)
            UNSAFE.putByte(container.pointer.address + 1, 42)
            UNSAFE.putByte(container.pointer.address + 2, -1)
            UNSAFE.putByte(container.pointer.address + 3, -69)

            container.reallocate(8, true)
            assertEquals(prevAddress, container.pointer.address, "Expected realloc in place, but got a new address")
            assertEquals(8, container.length, "Expected new length to be 8")
            assertEquals(69, UNSAFE.getByte(container.pointer.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(container.pointer.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(container.pointer.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(container.pointer.address + 3), "Byte at index 3 is modified")
            assertEquals(0, UNSAFE.getByte(container.pointer.address + 4), "Byte at index 4 is not 0")
            assertEquals(0, UNSAFE.getByte(container.pointer.address + 5), "Byte at index 5 is not 0")
            assertEquals(0, UNSAFE.getByte(container.pointer.address + 6), "Byte at index 6 is not 0")
            assertEquals(0, UNSAFE.getByte(container.pointer.address + 7), "Byte at index 7 is not 0")
        }
    }

    @Test
    fun reallocExpandNoInit() {
        MemoryStack {
            val container = malloc(4)
            val prevAddress = container.pointer.address

            UNSAFE.putByte(container.pointer.address, 69)
            UNSAFE.putByte(container.pointer.address + 1, 42)
            UNSAFE.putByte(container.pointer.address + 2, -1)
            UNSAFE.putByte(container.pointer.address + 3, -69)

            val dummy = malloc(4)

            UNSAFE.putByte(dummy.pointer.address, 11)
            UNSAFE.putByte(dummy.pointer.address + 1, 45)
            UNSAFE.putByte(dummy.pointer.address + 2, 14)
            UNSAFE.putByte(dummy.pointer.address + 3, -69)

            container.reallocate(8, false)
            assertNotEquals(prevAddress, container.pointer.address)
            assertEquals(8, container.length, "Expected new length to be 8")
            assertEquals(69, UNSAFE.getByte(container.pointer.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(container.pointer.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(container.pointer.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(container.pointer.address + 3), "Byte at index 3 is modified")

            assertEquals(11, UNSAFE.getByte(dummy.pointer.address), "Byte at index 0 is modified")
            assertEquals(45, UNSAFE.getByte(dummy.pointer.address + 1), "Byte at index 1 is modified")
            assertEquals(14, UNSAFE.getByte(dummy.pointer.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(dummy.pointer.address + 3), "Byte at index 3 is modified")
        }
    }

    @Test
    fun reallocExpandInit() {
        MemoryStack {
            val container = malloc(4)
            val prevAddress = container.pointer.address

            UNSAFE.putByte(container.pointer.address, 69)
            UNSAFE.putByte(container.pointer.address + 1, 42)
            UNSAFE.putByte(container.pointer.address + 2, -1)
            UNSAFE.putByte(container.pointer.address + 3, -69)

            val dummy = malloc(4)

            UNSAFE.putByte(dummy.pointer.address, 11)
            UNSAFE.putByte(dummy.pointer.address + 1, 45)
            UNSAFE.putByte(dummy.pointer.address + 2, 14)
            UNSAFE.putByte(dummy.pointer.address + 3, -69)

            container.reallocate(8, true)
            assertNotEquals(prevAddress, container.pointer.address)
            assertEquals(8, container.length, "Expected new length to be 8")
            assertEquals(69, UNSAFE.getByte(container.pointer.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(container.pointer.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(container.pointer.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(container.pointer.address + 3), "Byte at index 3 is modified")
            assertEquals(0, UNSAFE.getByte(container.pointer.address + 4), "Byte at index 4 is not 0")
            assertEquals(0, UNSAFE.getByte(container.pointer.address + 5), "Byte at index 5 is not 0")
            assertEquals(0, UNSAFE.getByte(container.pointer.address + 6), "Byte at index 6 is not 0")
            assertEquals(0, UNSAFE.getByte(container.pointer.address + 7), "Byte at index 7 is not 0")

            assertEquals(11, UNSAFE.getByte(dummy.pointer.address), "Byte at index 0 is modified")
            assertEquals(45, UNSAFE.getByte(dummy.pointer.address + 1), "Byte at index 1 is modified")
            assertEquals(14, UNSAFE.getByte(dummy.pointer.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(dummy.pointer.address + 3), "Byte at index 3 is modified")
        }
    }

    @Test
    fun reallocShrinkNoInit() {
        MemoryStack {
            val container = malloc(8)
            val prevAddress = container.pointer.address

            UNSAFE.putByte(container.pointer.address, 69)
            UNSAFE.putByte(container.pointer.address + 1, 42)
            UNSAFE.putByte(container.pointer.address + 2, -1)
            UNSAFE.putByte(container.pointer.address + 3, -69)
            UNSAFE.putByte(container.pointer.address + 4, 69)
            UNSAFE.putByte(container.pointer.address + 5, 42)
            UNSAFE.putByte(container.pointer.address + 6, -1)
            UNSAFE.putByte(container.pointer.address + 7, -69)

            container.reallocate(4, false)

            assertEquals(prevAddress, container.pointer.address, "Expected realloc in place, but got a new address")
            assertEquals(4, container.length, "Expected new length to be 4")
            assertEquals(69, UNSAFE.getByte(container.pointer.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(container.pointer.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(container.pointer.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(container.pointer.address + 3), "Byte at index 3 is modified")
        }
    }

    @Test
    fun reallocShrinkInit() {
        MemoryStack {
            val container = malloc(8)
            val prevAddress = container.pointer.address

            UNSAFE.putByte(container.pointer.address, 69)
            UNSAFE.putByte(container.pointer.address + 1, 42)
            UNSAFE.putByte(container.pointer.address + 2, -1)
            UNSAFE.putByte(container.pointer.address + 3, -69)
            UNSAFE.putByte(container.pointer.address + 4, 69)
            UNSAFE.putByte(container.pointer.address + 5, 42)
            UNSAFE.putByte(container.pointer.address + 6, -1)
            UNSAFE.putByte(container.pointer.address + 7, -69)

            container.reallocate(4, true)

            assertEquals(prevAddress, container.pointer.address, "Expected realloc in place, but got a new address")
            assertEquals(4, container.length, "Expected new length to be 4")
            assertEquals(69, UNSAFE.getByte(container.pointer.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(container.pointer.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(container.pointer.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(container.pointer.address + 3), "Byte at index 3 is modified")
        }
    }
}