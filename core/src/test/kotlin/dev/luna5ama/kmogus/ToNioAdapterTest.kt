package dev.luna5ama.kmogus

import org.junit.jupiter.api.Test
import java.nio.*
import kotlin.test.assertEquals

class ToNioAdapterTest {
    private fun check(buffer: Buffer, address: Long = 0L, capacity: Int = 0) {
        assertEquals(address, buffer.address)
        assertEquals(0, buffer.position())
        assertEquals(-1, buffer.mark)
        assertEquals(capacity, buffer.limit())
        assertEquals(capacity, buffer.capacity())
        when (buffer) {
            is ByteBuffer -> assertEquals(ByteOrder.nativeOrder(), buffer.order())
            is ShortBuffer -> assertEquals(ByteOrder.nativeOrder(), buffer.order())
            is CharBuffer -> assertEquals(ByteOrder.nativeOrder(), buffer.order())
            is IntBuffer -> assertEquals(ByteOrder.nativeOrder(), buffer.order())
            is LongBuffer -> assertEquals(ByteOrder.nativeOrder(), buffer.order())
            is FloatBuffer -> assertEquals(ByteOrder.nativeOrder(), buffer.order())
            is DoubleBuffer -> assertEquals(ByteOrder.nativeOrder(), buffer.order())
        }
    }

    @Test
    fun nullBuffers() {
        check(nullByteBuffer())
        check(nullShortBuffer())
        check(nullCharBuffer())
        check(nullIntBuffer())
        check(nullLongBuffer())
        check(nullFloatBuffer())
        check(nullDoubleBuffer())
    }

    @Test
    fun byte() {
        val arr = Arr.calloc(4L * 1L)
        val ptr = arr.ptr
        val buffer = ptr.asByteBuffer(4 * 1)

        check(buffer, ptr.address, 4 * 1)

        buffer.address = -69L
        buffer.position(1)
        buffer.limit(2)

        (ptr + 2).asByteBuffer(2 * 1, buffer)

        check(buffer, ptr.address + 2, 2 * 1)

        ptr.asByteBuffer(buffer)

        check(buffer, ptr.address, 2 * 1)
    }

    @Test
    fun short() {
        val arr = Arr.calloc(4L * 2L)
        val ptr = arr.ptr
        val buffer = ptr.asShortBuffer(4 * 2)

        check(buffer, ptr.address, 4 * 2)

        buffer.address = -69L
        buffer.position(1)
        buffer.limit(2)

        (ptr + 4).asShortBuffer(2 * 2, buffer)

        check(buffer, ptr.address + 4, 2 * 2)

        ptr.asShortBuffer(buffer)

        check(buffer, ptr.address, 2 * 2)
    }

    @Test
    fun char() {
        val arr = Arr.calloc(4L * 2L)
        val ptr = arr.ptr
        val buffer = ptr.asCharBuffer(4 * 2)

        check(buffer, ptr.address, 4 * 2)

        buffer.address = -69L
        buffer.position(1)
        buffer.limit(2)

        (ptr + 4).asCharBuffer(2 * 2, buffer)

        check(buffer, ptr.address + 4, 2 * 2)

        ptr.asCharBuffer(buffer)

        check(buffer, ptr.address, 2 * 2)
    }

    @Test
    fun int() {
        val arr = Arr.calloc(4L * 4L)
        val ptr = arr.ptr
        val buffer = ptr.asIntBuffer(4 * 4)

        check(buffer, ptr.address, 4 * 4)

        buffer.address = -69L
        buffer.position(1)
        buffer.limit(2)

        (ptr + 8).asIntBuffer(2 * 4, buffer)

        check(buffer, ptr.address + 8, 2 * 4)

        ptr.asIntBuffer(buffer)

        check(buffer, ptr.address, 2 * 4)
    }

    @Test
    fun long() {
        val arr = Arr.calloc(4L * 8L)
        val ptr = arr.ptr
        val buffer = ptr.asLongBuffer(4 * 8)

        check(buffer, ptr.address, 4 * 8)

        buffer.address = -69L
        buffer.position(1)
        buffer.limit(2)

        (ptr + 16).asLongBuffer(2 * 8, buffer)

        check(buffer, ptr.address + 16, 2 * 8)

        ptr.asLongBuffer(buffer)

        check(buffer, ptr.address, 2 * 8)
    }

    @Test
    fun float() {
        val arr = Arr.calloc(4L * 4L)
        val ptr = arr.ptr
        val buffer = ptr.asFloatBuffer(4 * 4)

        check(buffer, ptr.address, 4 * 4)

        buffer.address = -69L
        buffer.position(1)
        buffer.limit(2)

        (ptr + 8).asFloatBuffer(2 * 4, buffer)

        check(buffer, ptr.address + 8, 2 * 4)

        ptr.asFloatBuffer(buffer)

        check(buffer, ptr.address, 2 * 4)
    }

    @Test
    fun double() {
        val arr = Arr.calloc(4L * 8L)
        val ptr = arr.ptr
        val buffer = ptr.asDoubleBuffer(4 * 8)

        check(buffer, ptr.address, 4 * 8)

        buffer.address = -69L
        buffer.position(1)
        buffer.limit(2)

        (ptr + 16).asDoubleBuffer(2 * 8, buffer)

        check(buffer, ptr.address + 16, 2 * 8)

        ptr.asDoubleBuffer(buffer)

        check(buffer, ptr.address, 2 * 8)
    }
}