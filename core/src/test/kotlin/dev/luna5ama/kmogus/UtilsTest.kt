package dev.luna5ama.kmogus

import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UtilsTest {
    @Test
    fun byteBufferAddress() {
        val buffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())
        val address = buffer.address

        assertNotEquals(0L, address, "Expected non-null pointer")

        buffer.put(69)
        buffer.put(-1)
        buffer.put(-69)
        buffer.put(127)

        assertEquals(69, UNSAFE.getByte(address), "Byte at index 0 is not 69")
        assertEquals(-1, UNSAFE.getByte(address + 1), "Byte at index 1 is not -1")
        assertEquals(-69, UNSAFE.getByte(address + 2), "Byte at index 2 is not -69")
        assertEquals(127, UNSAFE.getByte(address + 3), "Byte at index 3 is not 127")
    }

    @Test
    fun shortBufferAddress() {
        val buffer = ByteBuffer.allocateDirect(4 * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
        val address = buffer.address

        assertNotEquals(0L, address, "Expected non-null pointer")

        buffer.put(420)
        buffer.put(-6969)
        buffer.put(32767)
        buffer.put(-1)

        assertEquals(420, UNSAFE.getShort(address), "Short at index 0 is not 420")
        assertEquals(-6969, UNSAFE.getShort(address + 2), "Short at index 1 is not -6969")
        assertEquals(32767, UNSAFE.getShort(address + 4), "Short at index 2 is not 32767")
        assertEquals(-1, UNSAFE.getShort(address + 6), "Short at index 3 is not -1")
    }

    @Test
    fun intBufferAddress() {
        val buffer = ByteBuffer.allocateDirect(4 * 4).order(ByteOrder.nativeOrder()).asIntBuffer()
        val address = buffer.address

        assertNotEquals(0L, address, "Expected non-null pointer")

        buffer.put(69420)
        buffer.put(-6969)
        buffer.put(114514)
        buffer.put(-1)

        assertEquals(69420, UNSAFE.getInt(address), "Int at index 0 is not 69420")
        assertEquals(-6969, UNSAFE.getInt(address + 4), "Int at index 1 is not -6969")
        assertEquals(114514, UNSAFE.getInt(address + 8), "Int at index 2 is not 114514")
        assertEquals(-1, UNSAFE.getInt(address + 12), "Int at index 3 is not -1")
    }

    @Test
    fun longBufferAddress() {
        val buffer = ByteBuffer.allocateDirect(4 * 8).order(ByteOrder.nativeOrder()).asLongBuffer()
        val address = buffer.address

        assertNotEquals(0L, address, "Expected non-null pointer")

        buffer.put(1145141919810)
        buffer.put(-6969)
        buffer.put(6969420114514)
        buffer.put(-1)

        assertEquals(1145141919810, UNSAFE.getLong(address), "Long at index 0 is not 1145141919810")
        assertEquals(-6969, UNSAFE.getLong(address + 8), "Long at index 1 is not -6969")
        assertEquals(6969420114514, UNSAFE.getLong(address + 16), "Long at index 2 is not 6969420114514")
        assertEquals(-1, UNSAFE.getLong(address + 24), "Long at index 3 is not -1")
    }

    @Test
    fun floatBufferAddress() {
        val buffer = ByteBuffer.allocateDirect(4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        val address = buffer.address

        assertNotEquals(0L, address, "Expected non-null pointer")

        buffer.put(Float.MIN_VALUE)
        buffer.put(Float.MAX_VALUE)
        buffer.put(420.69f)
        buffer.put(-1.0f)

        assertEquals(Float.MIN_VALUE, UNSAFE.getFloat(address), "Float at index 0 is not Float.MIN_VALUE")
        assertEquals(Float.MAX_VALUE, UNSAFE.getFloat(address + 4), "Float at index 1 is not Float.MAX_VALUE")
        assertEquals(420.69f, UNSAFE.getFloat(address + 8), "Float at index 2 is not 420.69f")
        assertEquals(-1.0f, UNSAFE.getFloat(address + 12), "Float at index 3 is not -1.0f")
    }

    @Test
    fun doubleBufferAddress() {
        val buffer = ByteBuffer.allocateDirect(4 * 8).order(ByteOrder.nativeOrder()).asDoubleBuffer()
        val address = buffer.address

        assertNotEquals(0L, address, "Expected non-null pointer")

        buffer.put(Double.MIN_VALUE)
        buffer.put(Double.MAX_VALUE)
        buffer.put(420.69)
        buffer.put(-1.0)

        assertEquals(Double.MIN_VALUE, UNSAFE.getDouble(address), "Double at index 0 is not Double.MIN_VALUE")
        assertEquals(Double.MAX_VALUE, UNSAFE.getDouble(address + 8), "Double at index 1 is not Double.MAX_VALUE")
        assertEquals(420.69, UNSAFE.getDouble(address + 16), "Double at index 2 is not 420.69")
        assertEquals(-1.0, UNSAFE.getDouble(address + 24), "Double at index 3 is not -1.0")
    }

    @Test
    fun bufferCapacity0() {
        val buffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())

        assertEquals(0, buffer.byteCapacity, "Expected capacity to be 0")
        assertEquals(0, buffer.asShortBuffer().byteCapacity, "Expected capacity to be 0")
        assertEquals(0, buffer.asIntBuffer().byteCapacity, "Expected capacity to be 0")
        assertEquals(0, buffer.asLongBuffer().byteCapacity, "Expected capacity to be 0")
        assertEquals(0, buffer.asFloatBuffer().byteCapacity, "Expected capacity to be 0")
        assertEquals(0, buffer.asDoubleBuffer().byteCapacity, "Expected capacity to be 0")
    }

    @Test
    fun bufferCapacityNot0() {
        val buffer = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder())

        assertEquals(16, buffer.byteCapacity, "Expected capacity to be 16")
        assertEquals(16, buffer.asShortBuffer().byteCapacity, "Expected capacity to be 16")
        assertEquals(16, buffer.asIntBuffer().byteCapacity, "Expected capacity to be 16")
        assertEquals(16, buffer.asLongBuffer().byteCapacity, "Expected capacity to be 16")
        assertEquals(16, buffer.asFloatBuffer().byteCapacity, "Expected capacity to be 16")
        assertEquals(16, buffer.asDoubleBuffer().byteCapacity, "Expected capacity to be 16")
    }

    @Test
    fun testMemcpy() {
        val a = MemoryPointer.calloc(16)
        val b = MemoryPointer.calloc(16)

        val spanA = a.asSpan()
        val spanB = b.asSpan()

        spanA[0] = 114514
        spanA[4] = 1919810
        spanA[8] = 69420
        spanA[12] = -1

        memcpy(spanA, spanB, 16)

        assertEquals(114514, spanB.getInt(0), "Int at index 0 is not 114514")
        assertEquals(1919810, spanB.getInt(4), "Int at index 4 is not 1919810")
        assertEquals(69420, spanB.getInt(8), "Int at index 8 is not 69420")
        assertEquals(-1, spanB.getInt(12), "Int at index 12 is not -1")

        memcpy(spanB[4], spanA[8], 8)

        assertEquals(1919810, spanA.getInt(8), "Int at index 8 is not 69420")
        assertEquals(69420, spanA.getInt(12), "Int at index 12 is not 69420")

        a.free()
        b.free()
    }
}