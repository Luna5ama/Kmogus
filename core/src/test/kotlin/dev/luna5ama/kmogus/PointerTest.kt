package dev.luna5ama.kmogus

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PointerTest {
    @Test
    fun offsetOperators() {
        val container = PointerContainer.malloc(420)
        var pointer = container.pointer

        assertEquals(container.pointer.address - 8, (pointer - 8).address, "Pointer address should be equal to pointer.address - 8")
        assertEquals(container.pointer.address + 8, (pointer + 8).address, "Pointer address should be equal to pointer.address + 8")

        pointer += 99
        assertEquals(container.pointer.address + 99, pointer.address, "Pointer address should be equal to pointer.address + 99")
        pointer -= 114
        assertEquals(container.pointer.address - 15, pointer.address, "Pointer address should be equal to pointer.address - 15")

        assertEquals(container.pointer.address - 15, pointer++.address, "Pointer address should be equal to pointer.address - 15")
        assertEquals(container.pointer.address - 14, pointer.address, "Pointer address should be equal to pointer.address - 14")
        assertEquals(container.pointer.address - 14, pointer--.address, "Pointer address should be equal to pointer.address - 14")
        assertEquals(container.pointer.address - 15, pointer.address, "Pointer address should be equal to pointer.address - 15")

        assertEquals(container.pointer.address - 10, pointer[5].address, "Pointer address should be equal to pointer.address - 10")
        assertEquals(container.pointer.address - 20, pointer[-5].address, "Pointer address should be equal to pointer.address - -20")
    }

    @Test
    fun setOperators() {
        val container = PointerContainer.malloc(420)
        val pointer = container.pointer

        pointer[0] = 69.toByte()
        pointer[1] = 420.toShort()
        pointer[3] = 1337
        pointer[7] = 69420L
        pointer[15] = 3.14f
        pointer[19] = 420.69

        assertEquals(69.toByte(), UNSAFE.getByte(container.pointer.address), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), UNSAFE.getShort(container.pointer.address + 1), "Short at index 1 should be equal to 420")
        assertEquals(1337, UNSAFE.getInt(container.pointer.address + 3), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, UNSAFE.getLong(container.pointer.address + 7), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, UNSAFE.getFloat(container.pointer.address + 15), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, UNSAFE.getDouble(container.pointer.address + 19), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun get() {
        val container = PointerContainer.malloc(420)

        UNSAFE.putByte(container.pointer.address, 69)
        UNSAFE.putShort(container.pointer.address + 1, 420)
        UNSAFE.putInt(container.pointer.address + 3, 1337)
        UNSAFE.putLong(container.pointer.address + 7, 69420)
        UNSAFE.putFloat(container.pointer.address + 15, 3.14f)
        UNSAFE.putDouble(container.pointer.address + 19, 420.69)

        assertEquals(69.toByte(), (container.pointer + 0).getByte(), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), (container.pointer + 1).getShort(), "Short at index 1 should be equal to 420")
        assertEquals(1337, (container.pointer + 3).getInt(), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, (container.pointer + 7).getLong(), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, (container.pointer + 15).getFloat(), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, (container.pointer + 19).getDouble(), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun getWithOffset() {
        val container = PointerContainer.malloc(420)
        val pointer = container.pointer

        UNSAFE.putByte(container.pointer.address, 69)
        UNSAFE.putShort(container.pointer.address + 1, 420)
        UNSAFE.putInt(container.pointer.address + 3, 1337)
        UNSAFE.putLong(container.pointer.address + 7, 69420)
        UNSAFE.putFloat(container.pointer.address + 15, 3.14f)
        UNSAFE.putDouble(container.pointer.address + 19, 420.69)

        assertEquals(69.toByte(), pointer.getByte(0), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), pointer.getShort(1), "Short at index 1 should be equal to 420")
        assertEquals(1337, pointer.getInt(3), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, pointer.getLong(7), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, pointer.getFloat(15), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, pointer.getDouble(19), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun set() {
        val container = PointerContainer.malloc(420)

        (container.pointer + 0).setByte(69)
        (container.pointer + 1).setShort(420)
        (container.pointer + 3).setInt(1337)
        (container.pointer + 7).setLong(69420)
        (container.pointer + 15).setFloat(3.14f)
        (container.pointer + 19).setDouble(420.69)

        assertEquals(69.toByte(), UNSAFE.getByte(container.pointer.address), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), UNSAFE.getShort(container.pointer.address + 1), "Short at index 1 should be equal to 420")
        assertEquals(1337, UNSAFE.getInt(container.pointer.address + 3), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, UNSAFE.getLong(container.pointer.address + 7), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, UNSAFE.getFloat(container.pointer.address + 15), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, UNSAFE.getDouble(container.pointer.address + 19), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun setWithOffset() {
        val container = PointerContainer.malloc(420)
        val pointer = container.pointer

        pointer.setByte(0, 69)
        pointer.setShort(1, 420)
        pointer.setInt(3, 1337)
        pointer.setLong(7, 69420)
        pointer.setFloat(15, 3.14f)
        pointer.setDouble(19, 420.69)

        assertEquals(69.toByte(), UNSAFE.getByte(container.pointer.address), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), UNSAFE.getShort(container.pointer.address + 1), "Short at index 1 should be equal to 420")
        assertEquals(1337, UNSAFE.getInt(container.pointer.address + 3), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, UNSAFE.getLong(container.pointer.address + 7), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, UNSAFE.getFloat(container.pointer.address + 15), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, UNSAFE.getDouble(container.pointer.address + 19), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun compare() {
        assert(Pointer(0) == Pointer(0))
        assert(Pointer(0) < Pointer(1))
        assert(Pointer(1) > Pointer(0))
        assert(Pointer(0) <= Pointer(0))
        assert(Pointer(0) <= Pointer(1))
        assert(Pointer(1) >= Pointer(0))
        assert(Pointer(1) >= Pointer(1))

        assert(Pointer(1145141919810) > Pointer(6942069420))
        assert(Pointer(6942069420) < Pointer(1145141919810))
    }

    @Test
    fun copyTo() {
        val a = PointerContainer.malloc(4)
        val b = PointerContainer.malloc(8)

        a.pointer.setByte(0, 11)
        a.pointer.setByte(1, 127)
        a.pointer.setByte(2, -1)
        a.pointer.setByte(3, -69)

        b.pointer.setByte(4, -99)
        b.pointer.setByte(5, 15)
        b.pointer.setByte(6, 64)
        b.pointer.setByte(7, 0)

        a.pointer.copyTo(b.pointer, 4)

        assertEquals(11, b.pointer.getByte(0))
        assertEquals(127, b.pointer.getByte(1))
        assertEquals(-1, b.pointer.getByte(2))
        assertEquals(-69, b.pointer.getByte(3))

        b.pointer.setByte(0, -55)
        b.pointer.setByte(1, 66)

        b.pointer.copyTo(a.pointer, 2)

        assertEquals(-55, a.pointer.getByte(0))
        assertEquals(66, a.pointer.getByte(1))
        assertEquals(-1, a.pointer.getByte(2))
        assertEquals(-69, a.pointer.getByte(3))
    }
}