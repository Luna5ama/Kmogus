package dev.luna5ama.kmogus

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class MemoryStackTest {
    @BeforeEach
    fun setup() {
        MemoryStack.initAndGet().checkEmpty()
    }

    @Test
    fun testManualPop() {
        MemoryStack {
            val a = malloc(4)
            val b = malloc(8)
            val c = malloc(12)

            assertFailsWith(IllegalStateException::class) {
                MemoryStack.get().checkEmpty()
            }

            c.close()
            b.close()
            a.close()
        }

        MemoryStack.get().checkEmpty()
    }

    @Test
    fun testAutoPop() {
        MemoryStack {
            malloc(4)
            malloc(8)
            malloc(12)

            assertFailsWith(IllegalStateException::class) {
                MemoryStack.get().checkEmpty()
            }
        }

        MemoryStack.get().checkEmpty()

        MemoryStack {
            malloc(4).use {
                malloc(8).use {
                    malloc(12).use {
                        assertFailsWith(IllegalStateException::class) {
                            MemoryStack.get().checkEmpty()
                        }
                    }
                }
            }
        }

        MemoryStack.get().checkEmpty()
    }

    @Test
    fun testCascadeStack() {
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

        MemoryStack.get().checkEmpty()
    }

    @Test
    fun testStackLength() {
        MemoryStack {
            val a = malloc(4)
            val b = malloc(1)
            val c = malloc(15)
            val d = malloc(8)

            assert(a.length == 4L)
            assert(b.length == 1L)
            assert(c.length == 15L)
            assert(d.length == 8L)

            assertFailsWith(IllegalStateException::class) {
                MemoryStack.get().checkEmpty()
            }
        }

        MemoryStack.get().checkEmpty()
    }

    @Test
    fun testStackAddress() {
        val baseField = MemoryStack::class.java.getDeclaredField("base")
        baseField.isAccessible = true
        MemoryStack {
            val base = baseField.get(this) as MemoryPointer
            val a = malloc(4)
            val b = malloc(1)
            val c = malloc(15)
            val d = malloc(8)

            assert(a.address == base.address)
            assert(a.delegated.offset == 0L)

            assert(b.address == base.address + 4)
            assert(b.delegated.offset == 4L)

            assert(c.address == base.address + 5)
            assert(c.delegated.offset == 5L)

            assert(d.address == base.address + 20)
            assert(d.delegated.offset == 20L)
        }
    }

    @Test
    fun testCalloc() {
        MemoryStack {
            calloc(1024).use {
                for (i in 0 until 1024) {
                    assert(it.getByte(i.toLong()) == 0.toByte())
                }
            }
        }
    }
}