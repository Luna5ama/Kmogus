package dev.luna5ama.kmogus

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PtrTest {
    @Test
    fun offsetOperators() {
        val container = Arr.malloc(420)
        var pointer = container.ptr

        assertEquals(container.ptr.address - 8, (pointer - 8).address, "Ptr address should be equal to ptr.address - 8")
        assertEquals(container.ptr.address + 8, (pointer + 8).address, "Ptr address should be equal to ptr.address + 8")

        pointer += 99
        assertEquals(container.ptr.address + 99, pointer.address, "Ptr address should be equal to ptr.address + 99")
        pointer -= 114
        assertEquals(container.ptr.address - 15, pointer.address, "Ptr address should be equal to ptr.address - 15")

        assertEquals(container.ptr.address - 15, pointer++.address, "Ptr address should be equal to ptr.address - 15")
        assertEquals(container.ptr.address - 14, pointer.address, "Ptr address should be equal to ptr.address - 14")
        assertEquals(container.ptr.address - 14, pointer--.address, "Ptr address should be equal to ptr.address - 14")
        assertEquals(container.ptr.address - 15, pointer.address, "Ptr address should be equal to ptr.address - 15")

        assertEquals(container.ptr.address - 10, pointer[5].address, "Ptr address should be equal to ptr.address - 10")
        assertEquals(container.ptr.address - 20, pointer[-5].address, "Ptr address should be equal to ptr.address - -20")
    }

    @Test
    fun setOperators() {
        val container = Arr.malloc(420)
        val pointer = container.ptr

        pointer[0] = 69.toByte()
        pointer[1] = 420.toShort()
        pointer[3] = 1337
        pointer[7] = 69420L
        pointer[15] = 3.14f
        pointer[19] = 420.69

        assertEquals(69.toByte(), UNSAFE.getByte(container.ptr.address), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), UNSAFE.getShort(container.ptr.address + 1), "Short at index 1 should be equal to 420")
        assertEquals(1337, UNSAFE.getInt(container.ptr.address + 3), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, UNSAFE.getLong(container.ptr.address + 7), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, UNSAFE.getFloat(container.ptr.address + 15), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, UNSAFE.getDouble(container.ptr.address + 19), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun get() {
        val container = Arr.malloc(420)

        UNSAFE.putByte(container.ptr.address, 69)
        UNSAFE.putShort(container.ptr.address + 1, 420)
        UNSAFE.putInt(container.ptr.address + 3, 1337)
        UNSAFE.putLong(container.ptr.address + 7, 69420)
        UNSAFE.putFloat(container.ptr.address + 15, 3.14f)
        UNSAFE.putDouble(container.ptr.address + 19, 420.69)

        assertEquals(69.toByte(), (container.ptr + 0).getByte(), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), (container.ptr + 1).getShort(), "Short at index 1 should be equal to 420")
        assertEquals(1337, (container.ptr + 3).getInt(), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, (container.ptr + 7).getLong(), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, (container.ptr + 15).getFloat(), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, (container.ptr + 19).getDouble(), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun getWithOffset() {
        val container = Arr.malloc(420)
        val pointer = container.ptr

        UNSAFE.putByte(container.ptr.address, 69)
        UNSAFE.putShort(container.ptr.address + 1, 420)
        UNSAFE.putInt(container.ptr.address + 3, 1337)
        UNSAFE.putLong(container.ptr.address + 7, 69420)
        UNSAFE.putFloat(container.ptr.address + 15, 3.14f)
        UNSAFE.putDouble(container.ptr.address + 19, 420.69)

        assertEquals(69.toByte(), pointer.getByte(0), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), pointer.getShort(1), "Short at index 1 should be equal to 420")
        assertEquals(1337, pointer.getInt(3), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, pointer.getLong(7), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, pointer.getFloat(15), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, pointer.getDouble(19), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun set() {
        val container = Arr.malloc(420)

        (container.ptr + 0).setByte(69)
        (container.ptr + 1).setShort(420)
        (container.ptr + 3).setInt(1337)
        (container.ptr + 7).setLong(69420)
        (container.ptr + 15).setFloat(3.14f)
        (container.ptr + 19).setDouble(420.69)

        assertEquals(69.toByte(), UNSAFE.getByte(container.ptr.address), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), UNSAFE.getShort(container.ptr.address + 1), "Short at index 1 should be equal to 420")
        assertEquals(1337, UNSAFE.getInt(container.ptr.address + 3), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, UNSAFE.getLong(container.ptr.address + 7), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, UNSAFE.getFloat(container.ptr.address + 15), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, UNSAFE.getDouble(container.ptr.address + 19), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun setWithOffset() {
        val container = Arr.malloc(420)
        val pointer = container.ptr

        pointer.setByte(0, 69)
        pointer.setShort(1, 420)
        pointer.setInt(3, 1337)
        pointer.setLong(7, 69420)
        pointer.setFloat(15, 3.14f)
        pointer.setDouble(19, 420.69)

        assertEquals(69.toByte(), UNSAFE.getByte(container.ptr.address), "Byte at index 0 should be equal to 69")
        assertEquals(420.toShort(), UNSAFE.getShort(container.ptr.address + 1), "Short at index 1 should be equal to 420")
        assertEquals(1337, UNSAFE.getInt(container.ptr.address + 3), "Int at index 3 should be equal to 1337")
        assertEquals(69420L, UNSAFE.getLong(container.ptr.address + 7), "Long at index 7 should be equal to 69420")
        assertEquals(3.14f, UNSAFE.getFloat(container.ptr.address + 15), "Float at index 11 should be equal to 3.14")
        assertEquals(420.69, UNSAFE.getDouble(container.ptr.address + 19), "Double at index 15 should be equal to 420.69")
    }

    @Test
    fun compare() {
        assert(Ptr(0) == Ptr(0))
        assert(Ptr(0) < Ptr(1))
        assert(Ptr(1) > Ptr(0))
        assert(Ptr(0) <= Ptr(0))
        assert(Ptr(0) <= Ptr(1))
        assert(Ptr(1) >= Ptr(0))
        assert(Ptr(1) >= Ptr(1))

        assert(Ptr(1145141919810) > Ptr(6942069420))
        assert(Ptr(6942069420) < Ptr(1145141919810))
    }
}