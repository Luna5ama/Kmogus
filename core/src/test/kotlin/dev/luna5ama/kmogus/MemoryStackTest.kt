package dev.luna5ama.kmogus

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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

            assertEquals(a.ptr.address + 8, b.ptr.address, "b.address != a.address + 4")
            assertEquals(b.ptr.address + 8, c.ptr.address, "c.address != b.address + 1")
            assertEquals(c.ptr.address + 16, d.ptr.address, "d.address != c.address + 15")

            assertEquals(4, a.len, "a.length != 4")
            assertEquals(1, b.len, "b.length != 1")
            assertEquals(15, c.len, "c.length != 15")
        }
    }

    @Test
    fun alignment() {
        MemoryStack {
            val a = malloc(1)
            val b = malloc(2)
            val c = malloc(3)
            val d = malloc(4)
            val e = malloc(5)
            val f = malloc(6)
            val g = malloc(7)
            val h = malloc(8)
            val i = malloc(9)

            assertEquals(a.ptr.address + 8, b.ptr.address, "a.padding != 8")
            assertEquals(b.ptr.address + 8, c.ptr.address, "b.padding != 8")
            assertEquals(c.ptr.address + 8, d.ptr.address, "c.padding != 8")
            assertEquals(d.ptr.address + 8, e.ptr.address, "d.padding != 8")
            assertEquals(e.ptr.address + 8, f.ptr.address, "e.padding != 8")
            assertEquals(f.ptr.address + 8, g.ptr.address, "f.padding != 8")
            assertEquals(g.ptr.address + 8, h.ptr.address, "g.padding != 8")
            assertEquals(h.ptr.address + 8, i.ptr.address, "h.padding != 8")
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
                assertEquals(0, UNSAFE.getByte(container.ptr.address + i), "Byte at index $i is not 0")
            }
        }
    }

    @Test
    fun reallocInPlaceExpandNoInit() {
        MemoryStack {
            val container = malloc(4)
            val prevAddress = container.ptr.address

            UNSAFE.putByte(container.ptr.address, 69)
            UNSAFE.putByte(container.ptr.address + 1, 42)
            UNSAFE.putByte(container.ptr.address + 2, -1)
            UNSAFE.putByte(container.ptr.address + 3, -69)

            container.realloc(8, false)
            assertEquals(prevAddress, container.ptr.address, "Expected realloc in place, but got a new address")
            assertEquals(8, container.len, "Expected new length to be 8")
            assertEquals(69, UNSAFE.getByte(container.ptr.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(container.ptr.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(container.ptr.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(container.ptr.address + 3), "Byte at index 3 is modified")
        }
    }

    @Test
    fun reallocInPlaceExpandInit() {
        MemoryStack {
            val container = malloc(4)
            val prevAddress = container.ptr.address

            UNSAFE.putByte(container.ptr.address, 69)
            UNSAFE.putByte(container.ptr.address + 1, 42)
            UNSAFE.putByte(container.ptr.address + 2, -1)
            UNSAFE.putByte(container.ptr.address + 3, -69)

            container.realloc(8, true)
            assertEquals(prevAddress, container.ptr.address, "Expected realloc in place, but got a new address")
            assertEquals(8, container.len, "Expected new length to be 8")
            assertEquals(69, UNSAFE.getByte(container.ptr.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(container.ptr.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(container.ptr.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(container.ptr.address + 3), "Byte at index 3 is modified")
            assertEquals(0, UNSAFE.getByte(container.ptr.address + 4), "Byte at index 4 is not 0")
            assertEquals(0, UNSAFE.getByte(container.ptr.address + 5), "Byte at index 5 is not 0")
            assertEquals(0, UNSAFE.getByte(container.ptr.address + 6), "Byte at index 6 is not 0")
            assertEquals(0, UNSAFE.getByte(container.ptr.address + 7), "Byte at index 7 is not 0")
        }
    }

    @Test
    fun reallocExpandNoInit() {
        MemoryStack {
            val container = malloc(4)
            val prevAddress = container.ptr.address

            UNSAFE.putByte(container.ptr.address, 69)
            UNSAFE.putByte(container.ptr.address + 1, 42)
            UNSAFE.putByte(container.ptr.address + 2, -1)
            UNSAFE.putByte(container.ptr.address + 3, -69)

            val dummy = malloc(4)

            UNSAFE.putByte(dummy.ptr.address, 11)
            UNSAFE.putByte(dummy.ptr.address + 1, 45)
            UNSAFE.putByte(dummy.ptr.address + 2, 14)
            UNSAFE.putByte(dummy.ptr.address + 3, -69)

            container.realloc(8, false)
            assertNotEquals(prevAddress, container.ptr.address)
            assertEquals(8, container.len, "Expected new length to be 8")
            assertEquals(69, UNSAFE.getByte(container.ptr.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(container.ptr.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(container.ptr.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(container.ptr.address + 3), "Byte at index 3 is modified")

            assertEquals(11, UNSAFE.getByte(dummy.ptr.address), "Byte at index 0 is modified")
            assertEquals(45, UNSAFE.getByte(dummy.ptr.address + 1), "Byte at index 1 is modified")
            assertEquals(14, UNSAFE.getByte(dummy.ptr.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(dummy.ptr.address + 3), "Byte at index 3 is modified")
        }
    }

    @Test
    fun reallocExpandInit() {
        MemoryStack {
            val container = malloc(4)
            val prevAddress = container.ptr.address

            UNSAFE.putByte(container.ptr.address, 69)
            UNSAFE.putByte(container.ptr.address + 1, 42)
            UNSAFE.putByte(container.ptr.address + 2, -1)
            UNSAFE.putByte(container.ptr.address + 3, -69)

            val dummy = malloc(4)

            UNSAFE.putByte(dummy.ptr.address, 11)
            UNSAFE.putByte(dummy.ptr.address + 1, 45)
            UNSAFE.putByte(dummy.ptr.address + 2, 14)
            UNSAFE.putByte(dummy.ptr.address + 3, -69)

            container.realloc(8, true)
            assertNotEquals(prevAddress, container.ptr.address)
            assertEquals(8, container.len, "Expected new length to be 8")
            assertEquals(69, UNSAFE.getByte(container.ptr.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(container.ptr.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(container.ptr.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(container.ptr.address + 3), "Byte at index 3 is modified")
            assertEquals(0, UNSAFE.getByte(container.ptr.address + 4), "Byte at index 4 is not 0")
            assertEquals(0, UNSAFE.getByte(container.ptr.address + 5), "Byte at index 5 is not 0")
            assertEquals(0, UNSAFE.getByte(container.ptr.address + 6), "Byte at index 6 is not 0")
            assertEquals(0, UNSAFE.getByte(container.ptr.address + 7), "Byte at index 7 is not 0")

            assertEquals(11, UNSAFE.getByte(dummy.ptr.address), "Byte at index 0 is modified")
            assertEquals(45, UNSAFE.getByte(dummy.ptr.address + 1), "Byte at index 1 is modified")
            assertEquals(14, UNSAFE.getByte(dummy.ptr.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(dummy.ptr.address + 3), "Byte at index 3 is modified")
        }
    }

    @Test
    fun reallocShrinkNoInit() {
        MemoryStack {
            val container = malloc(8)
            val prevAddress = container.ptr.address

            UNSAFE.putByte(container.ptr.address, 69)
            UNSAFE.putByte(container.ptr.address + 1, 42)
            UNSAFE.putByte(container.ptr.address + 2, -1)
            UNSAFE.putByte(container.ptr.address + 3, -69)
            UNSAFE.putByte(container.ptr.address + 4, 69)
            UNSAFE.putByte(container.ptr.address + 5, 42)
            UNSAFE.putByte(container.ptr.address + 6, -1)
            UNSAFE.putByte(container.ptr.address + 7, -69)

            container.realloc(4, false)

            assertEquals(prevAddress, container.ptr.address, "Expected realloc in place, but got a new address")
            assertEquals(4, container.len, "Expected new length to be 4")
            assertEquals(69, UNSAFE.getByte(container.ptr.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(container.ptr.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(container.ptr.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(container.ptr.address + 3), "Byte at index 3 is modified")
        }
    }

    @Test
    fun reallocShrinkInit() {
        MemoryStack {
            val container = malloc(8)
            val prevAddress = container.ptr.address

            UNSAFE.putByte(container.ptr.address, 69)
            UNSAFE.putByte(container.ptr.address + 1, 42)
            UNSAFE.putByte(container.ptr.address + 2, -1)
            UNSAFE.putByte(container.ptr.address + 3, -69)
            UNSAFE.putByte(container.ptr.address + 4, 69)
            UNSAFE.putByte(container.ptr.address + 5, 42)
            UNSAFE.putByte(container.ptr.address + 6, -1)
            UNSAFE.putByte(container.ptr.address + 7, -69)

            container.realloc(4, true)

            assertEquals(prevAddress, container.ptr.address, "Expected realloc in place, but got a new address")
            assertEquals(4, container.len, "Expected new length to be 4")
            assertEquals(69, UNSAFE.getByte(container.ptr.address), "Byte at index 0 is modified")
            assertEquals(42, UNSAFE.getByte(container.ptr.address + 1), "Byte at index 1 is modified")
            assertEquals(-1, UNSAFE.getByte(container.ptr.address + 2), "Byte at index 2 is modified")
            assertEquals(-69, UNSAFE.getByte(container.ptr.address + 3), "Byte at index 3 is modified")
        }
    }

    @Test
    fun multiRealloc() {
        MemoryStack {
            val a = malloc(61L)
            a.realloc(30L, false)
            assertEquals(30L, a.len)

            val b = malloc(59L)
            assertEquals(59L, b.len)
            checkAddressRange(a, b)

            b.realloc(1020, false)
            assertEquals(1020L, b.len)
            checkAddressRange(a, b)

            a.realloc(1032, false)
            assertEquals(1032L, a.len)
            checkAddressRange(a, b)

            val c = malloc(2049L)
            assertEquals(2049L, c.len)

            checkAddressRange(a, c)
            checkAddressRange(b, c)
        }
    }

    @Test
    fun hugeAlloc() {
        MemoryStack {
            val a = malloc(1024 * 1024 * 4)
            val b = malloc(1024 * 1024 * 4)
            val c = malloc(1024 * 1024 * 4)

            assertEquals(1024 * 1024 * 4, a.len)
            assertEquals(1024 * 1024 * 4, b.len)
            assertEquals(1024 * 1024 * 4, c.len)

            checkAddressRange(a, b)
            checkAddressRange(a, c)
            checkAddressRange(b, c)
        }
    }

    @Test
    fun hugeRealloc() {
        MemoryStack {
            val a = malloc(1024L)
            val b = malloc(1024L)
            a.realloc(1024 * 1024 * 4L, false)
            b.realloc(1024 * 1024 * 4L, false)

            assertEquals(1024 * 1024 * 4, a.len)
            assertEquals(1024 * 1024 * 4, b.len)

            checkAddressRange(a, b)
        }
    }

    private fun checkAddressRange(a: Arr, b: Arr) {
        assertTrue(b.ptr.address !in a.ptr.address until a.ptr.address + a.len, "Arr range overlaps with another arr")
        assertTrue(
            b.ptr.address + b.len !in a.ptr.address until a.ptr.address + a.len,
            "Arr range overlaps with another arr"
        )
    }
}