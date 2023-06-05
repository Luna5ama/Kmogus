package dev.luna5ama.kmogus

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SpanTest {
    @Test
    fun fromPointer() {
        val pointer = MemoryPointer.malloc(420)

        assertEquals(pointer.address, Span(pointer.address).address, "Span address should be equal to pointer address")
        assertEquals(pointer.address, pointer.asSpan().address, "Span address should be equal to pointer address")
        assertEquals(pointer.address, pointer.asSpan(0).address, "Span address should be equal to pointer address")
        assertEquals(
            pointer.address - 1,
            pointer.asSpan(-1).address,
            "Span address should be equal to pointer address - 1"
        )
        assertEquals(
            pointer.address + 1,
            pointer.asSpan(1).address,
            "Span address should be equal to pointer address + 1"
        )

        pointer.free()
    }

    @Test
    fun fromArray() {
        val pointer = MemoryPointer.malloc(420)
        val array = pointer.asArray()

        assertEquals(pointer.address, array.asSpan().address, "Span address should be equal to array address")
        assertEquals(pointer.address, array.asSpan(0).address, "Span address should be equal to array address")
        assertEquals(
            pointer.address - 1,
            array.asSpan(-1).address,
            "Span address should be equal to array address - 1"
        )
        assertEquals(
            pointer.address + 1,
            array.asSpan(1).address,
            "Span address should be equal to array address + 1"
        )

        array.offset += 69

        assertEquals(
            pointer.address + 69,
            array.asSpan().address,
            "Span address should be equal to array address + 69"
        )
        assertEquals(
            pointer.address + 69,
            array.asSpan(0).address,
            "Span address should be equal to array address + 69"
        )
        assertEquals(
            pointer.address + 68,
            array.asSpan(-1).address,
            "Span address should be equal to array address + 68"
        )
        assertEquals(
            pointer.address + 70,
            array.asSpan(1).address,
            "Span address should be equal to array address + 70"
        )

        pointer.free()
    }

    @Test
    fun offsetOperators() {
        var span = Span(69)

        assertEquals(61, (span - 8).address, "Span address should be equal to 61")
        assertEquals(77, (span + 8).address, "Span address should be equal to 77")

        span += 99
        assertEquals(168, span.address, "Span address should be equal to 168")
        span -= 114
        assertEquals(54, span.address, "Span address should be equal to 54")

        assertEquals(54, span++.address, "Span address should be equal to 55")
        assertEquals(55, span.address, "Span address should be equal to 55")
        assertEquals(55, span--.address, "Span address should be equal to 54")
        assertEquals(54, span.address, "Span address should be equal to 54")

        assertEquals(59, span[5].address, "Span address should be equal to 59")
        assertEquals(49, span[-5].address, "Span address should be equal to 49")
    }

    @Test
    fun setOperators() {
        val pointer = MemoryPointer.malloc(420)
        val span = pointer.asSpan()

        span[0] = 69.toByte()
        span[1] = 420.toShort()
        span[3] = 1337
        span[7] = 69420L
        span[15] = 3.14f
        span[19] = 420.69

        assertEquals(69.toByte(), UNSAFE.getByte(pointer.address), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), UNSAFE.getShort(pointer.address + 1), "Short at index 1 should be equal to 420")
        assertEquals(1337, UNSAFE.getInt(pointer.address + 3), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, UNSAFE.getLong(pointer.address + 7), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, UNSAFE.getFloat(pointer.address + 15), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, UNSAFE.getDouble(pointer.address + 19), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun get() {
        val pointer = MemoryPointer.malloc(420)

        UNSAFE.putByte(pointer.address, 69)
        UNSAFE.putShort(pointer.address + 1, 420)
        UNSAFE.putInt(pointer.address + 3, 1337)
        UNSAFE.putLong(pointer.address + 7, 69420)
        UNSAFE.putFloat(pointer.address + 15, 3.14f)
        UNSAFE.putDouble(pointer.address + 19, 420.69)

        assertEquals(69.toByte(), pointer.asSpan(0).getByte(), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), pointer.asSpan(1).getShort(), "Short at index 1 should be equal to 420")
        assertEquals(1337, pointer.asSpan(3).getInt(), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, pointer.asSpan(7).getLong(), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, pointer.asSpan(15).getFloat(), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, pointer.asSpan(19).getDouble(), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun getWithOffset() {
        val pointer = MemoryPointer.malloc(420)
        val span = pointer.asSpan()

        UNSAFE.putByte(pointer.address, 69)
        UNSAFE.putShort(pointer.address + 1, 420)
        UNSAFE.putInt(pointer.address + 3, 1337)
        UNSAFE.putLong(pointer.address + 7, 69420)
        UNSAFE.putFloat(pointer.address + 15, 3.14f)
        UNSAFE.putDouble(pointer.address + 19, 420.69)

        assertEquals(69.toByte(), span.getByte(0), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), span.getShort(1), "Short at index 1 should be equal to 420")
        assertEquals(1337, span.getInt(3), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, span.getLong(7), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, span.getFloat(15), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, span.getDouble(19), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun set() {
        val pointer = MemoryPointer.malloc(420)

        pointer.asSpan(0).setByte(69)
        pointer.asSpan(1).setShort(420)
        pointer.asSpan(3).setInt(1337)
        pointer.asSpan(7).setLong(69420)
        pointer.asSpan(15).setFloat(3.14f)
        pointer.asSpan(19).setDouble(420.69)

        assertEquals(69.toByte(), UNSAFE.getByte(pointer.address), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), UNSAFE.getShort(pointer.address + 1), "Short at index 1 should be equal to 420")
        assertEquals(1337, UNSAFE.getInt(pointer.address + 3), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, UNSAFE.getLong(pointer.address + 7), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, UNSAFE.getFloat(pointer.address + 15), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, UNSAFE.getDouble(pointer.address + 19), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun setWithOffset() {
        val pointer = MemoryPointer.malloc(420)
        val span = pointer.asSpan()

        span.setByte(0, 69)
        span.setShort(1, 420)
        span.setInt(3, 1337)
        span.setLong(7, 69420)
        span.setFloat(15, 3.14f)
        span.setDouble(19, 420.69)

        assertEquals(69.toByte(), UNSAFE.getByte(pointer.address), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), UNSAFE.getShort(pointer.address + 1), "Short at index 1 should be equal to 420")
        assertEquals(1337, UNSAFE.getInt(pointer.address + 3), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, UNSAFE.getLong(pointer.address + 7), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, UNSAFE.getFloat(pointer.address + 15), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, UNSAFE.getDouble(pointer.address + 19), "Double at index 15 should be equal to 420.69")
    }
}