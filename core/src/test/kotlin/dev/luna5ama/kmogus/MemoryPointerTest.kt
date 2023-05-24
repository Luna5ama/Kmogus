package dev.luna5ama.kmogus

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertFailsWith

class MemoryPointerTest {

    @Test
    fun testProperties() {
        val pointer = MemoryPointer.malloc(0L)
        assert(pointer.address == 0L)
        assert(pointer.length == 0L)
        pointer.address = Long.MAX_VALUE
        pointer.length = Long.MAX_VALUE
        assert(pointer.address == Long.MAX_VALUE)
        assert(pointer.length == Long.MAX_VALUE)
        assertFailsWith(IllegalArgumentException::class) {
            pointer.address = -1
        }
        assertFailsWith(IllegalArgumentException::class) {
            pointer.length = -1
        }
    }

    @Test
    fun testAllocate() {
        val pointer = MemoryPointer.malloc(1)
        assert(pointer.address != 0L)
        assert(pointer.length == 1L)
        pointer.free()
    }

    @Test
    fun testAllocateZero() {
        val pointer = MemoryPointer.malloc(0L)
        assert(pointer.address == 0L)
        assert(pointer.length == 0L)
        pointer.free()
    }

    @Test
    fun testAllocateNegative() {
        assertFailsWith(IllegalArgumentException::class) {
            MemoryPointer.malloc(-1)
        }
    }

    @Test
    fun testCalloc() {
        val pointer = MemoryPointer.calloc(TestUtils.TEST_DATA_SIZE.toLong())
        assert(pointer.address != 0L)
        assert(pointer.length == TestUtils.TEST_DATA_SIZE.toLong())
        for (i in 0 until pointer.length) {
            assert(pointer.getByteUnsafe(i) == 0.toByte())
        }
        pointer.free()
    }

    @Test
    fun testRawWrapping() {
        assertFailsWith(IllegalArgumentException::class) {
            MemoryPointer.wrap(0, -1)
        }

        assertFailsWith(IllegalArgumentException::class) {
            MemoryPointer.wrap(-1, 0)
        }

        val address = UNSAFE.allocateMemory(TestUtils.TEST_DATA_SIZE.toLong())
        val pointer = MemoryPointer.wrap(address, TestUtils.TEST_DATA_SIZE.toLong())
        assert(pointer.address == address)
        assert(pointer.length == TestUtils.TEST_DATA_SIZE.toLong())

        assertFailsWith(UnsupportedOperationException::class) {
            pointer.free()
        }

        val a = TestUtils.randomBytes()
        UNSAFE.copyMemory(a, BYTE_ARRAY_OFFSET.toLong(), null, address, a.size.toLong())

        for (i in a.indices) {
            assert(pointer.getByteUnsafe(i.toLong()) == a[i])
        }

        UNSAFE.freeMemory(address)
    }

    @Test
    fun testNioBufferWrapping() {
        val buffer = TestUtils.allocDirectBuffer(TestUtils.TEST_DATA_SIZE * 4)
        val pointer = MemoryPointer.wrap(buffer)

        assertFailsWith(UnsupportedOperationException::class) {
            pointer.free()
        }

        val a = TestUtils.randomInts()
        buffer.asIntBuffer().put(a)

        for (i in a.indices) {
            assert(pointer.getIntUnsafe(i * 4L) == a[i])
            assert(pointer.getIntUnsafe(i * 4L) == buffer.getInt(i * 4))
        }

        val b = TestUtils.randomInts()
        pointer.setIntsUnsafe(b)

        for (i in b.indices) {
            assert(buffer.getInt(i * 4) == b[i])
            assert(buffer.getInt(i * 4) == pointer.getIntUnsafe(i * 4L))
        }
    }

    @Test
    fun testCheckOffset() {
        val pointer = MemoryPointer.malloc(8L)

        val checkOffsetMethod = MemoryPointer::class.java.getDeclaredMethod(
            "checkOffset",
            Long::class.javaPrimitiveType,
            Long::class.javaPrimitiveType
        )
        checkOffsetMethod.isAccessible = true

        val checkOffset: MemoryPointer.(Long, Long) -> Unit = { offset, size ->
            runCatching {
                checkOffsetMethod.invoke(this, offset, size)
            }.onFailure {
                throw it.cause!!
            }
        }

        assertDoesNotThrow {
            pointer.checkOffset(0, 1L)
            pointer.checkOffset(0, 4L)
            pointer.checkOffset(7, 1L)
            pointer.checkOffset(4, 4L)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            checkOffset(pointer, -1, 1L)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            checkOffset(pointer, -1, 4L)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            checkOffset(pointer, 8, 1L)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            checkOffset(pointer, 5, 4L)
        }

        pointer.free()
    }

    @Test
    fun checkIndexRange() {
        val pointer = MemoryPointer.malloc(8L)
        testCheckIndexRange(getCheckIndexRange("checkSrcIndexRange"), pointer)
        testCheckIndexRange(getCheckByteIndexRange("checkSrcByteIndexRange"), pointer)
        testCheckIndexRange(getCheckIndexRange("checkDstIndexRange"), pointer)
        testCheckIndexRange(getCheckByteIndexRange("checkDstByteIndexRange"), pointer)
        pointer.free()
    }

    private fun getCheckByteIndexRange(name: String): MemoryPointer.(Int, Int) -> Unit {
        val checkSrcByteIndexRangeMethod = MemoryPointer::class.java.getDeclaredMethod(
            name,
            Long::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        )
        checkSrcByteIndexRangeMethod.isAccessible = true

        val checkSrcByteIndexRange: MemoryPointer.(Int, Int) -> Unit = { srcIndex, length ->
            runCatching {
                checkSrcByteIndexRangeMethod.invoke(this, srcIndex, length)
            }.onFailure {
                throw it.cause!!
            }
        }
        return checkSrcByteIndexRange
    }

    private fun getCheckIndexRange(name: String): MemoryPointer.(Int, Int) -> Unit {
        val checkSrcIndexRangeMethod = MemoryPointer::class.java.getDeclaredMethod(
            name,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        )
        checkSrcIndexRangeMethod.isAccessible = true

        val checkSrcIndexRange: MemoryPointer.(Int, Int) -> Unit = { srcIndex, length ->
            runCatching {
                checkSrcIndexRangeMethod.invoke(this, srcIndex, length, 8)
            }.onFailure {
                throw it.cause!!
            }
        }
        return checkSrcIndexRange
    }

    private fun testCheckIndexRange(
        checkIndexRange: MemoryPointer.(Int, Int) -> Unit,
        pointer: MemoryPointer
    ) {
        assertDoesNotThrow {
            checkIndexRange(pointer, 0, 0)
            checkIndexRange(pointer, 0, 1)
            checkIndexRange(pointer, 0, 8)
            checkIndexRange(pointer, 7, 1)
            checkIndexRange(pointer, 3, 4)
            checkIndexRange(pointer, 8, 0)
        }

        assertFailsWith(IndexOutOfBoundsException::class) {
            checkIndexRange(pointer, -1, 1)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            checkIndexRange(pointer, 9, 1)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            checkIndexRange(pointer, 0, -1)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            checkIndexRange(pointer, 0, 9)
        }
    }

    @Test
    fun testByteSingle() {
        val array = TestUtils.randomBytes()

        val pointer = MemoryPointer.malloc(array.size.toLong())

        for (i in array.indices) {
            pointer.setByteUnsafe(i.toLong(), array[i])
        }

        for (i in array.indices) {
            assert(array[i] == pointer.getByteUnsafe(i.toLong()))
        }

        pointer.free()
    }

    @Test
    fun testShortSingle() {
        val array = TestUtils.randomShorts()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 2)

        for (i in array.indices) {
            pointer.setShortUnsafe(i * 2L, array[i])
        }

        for (i in array.indices) {
            assert(array[i] == pointer.getShortUnsafe(i * 2L))
        }

        pointer.free()
    }

    @Test
    fun testIntSingle() {
        val array = TestUtils.randomInts()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 4)

        for (i in array.indices) {
            pointer.setIntUnsafe(i * 4L, array[i])
        }

        for (i in array.indices) {
            assert(array[i] == pointer.getIntUnsafe(i * 4L))
        }

        pointer.free()
    }

    @Test
    fun testLongSingle() {
        val array = TestUtils.randomLongs()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 8)

        for (i in array.indices) {
            pointer.setLongUnsafe(i * 8L, array[i])
        }

        for (i in array.indices) {
            assert(array[i] == pointer.getLongUnsafe(i * 8L))
        }

        pointer.free()
    }

    @Test
    fun testFloatSingle() {
        val array = TestUtils.randomFloats()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 4)

        for (i in array.indices) {
            pointer.setFloatUnsafe(i * 4L, array[i])
        }

        for (i in array.indices) {
            assert(array[i] == pointer.getFloatUnsafe(i * 4L))
        }

        pointer.free()
    }

    @Test
    fun testDoubleSingle() {
        val array = TestUtils.randomDoubles()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 8)

        for (i in array.indices) {
            pointer.setDoubleUnsafe(i * 8L, array[i])
        }

        for (i in array.indices) {
            assert(array[i] == pointer.getDoubleUnsafe(i * 8L))
        }

        pointer.free()
    }

    @Test
    fun testByteBulk() {
        val array = TestUtils.randomBytes()

        val pointer = MemoryPointer.malloc(array.size.toLong())

        pointer.setBytesUnsafe(array)

        for (i in array.indices) {
            assert(array[i] == pointer.getByteUnsafe(i.toLong()))
        }

        assert(array.contentEquals(pointer.getBytesUnsafe()))
        assert(array.contentEquals(pointer.getBytesUnsafe(ByteArray(array.size))))

        pointer.free()
    }


    @Test
    fun testShortBulk() {
        val array = TestUtils.randomShorts()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 2)

        pointer.setShortsUnsafe(array)

        for (i in array.indices) {
            assert(array[i] == pointer.getShortUnsafe(i * 2L))
        }

        assert(array.contentEquals(pointer.getShortsUnsafe()))
        assert(array.contentEquals(pointer.getShortsUnsafe(ShortArray(array.size))))

        pointer.free()
    }

    @Test
    fun testIntBulk() {
        val array = TestUtils.randomInts()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 4)

        pointer.setIntsUnsafe(array)

        for (i in array.indices) {
            assert(array[i] == pointer.getIntUnsafe(i * 4L))
        }

        assert(array.contentEquals(pointer.getIntsUnsafe()))
        assert(array.contentEquals(pointer.getIntsUnsafe(IntArray(array.size))))

        pointer.free()
    }

    @Test
    fun testLongBulk() {
        val array = TestUtils.randomLongs()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 8)

        pointer.setLongsUnsafe(array)

        for (i in array.indices) {
            assert(array[i] == pointer.getLongUnsafe(i * 8L))
        }

        assert(array.contentEquals(pointer.getLongsUnsafe()))
        assert(array.contentEquals(pointer.getLongsUnsafe(LongArray(array.size))))

        pointer.free()
    }

    @Test
    fun testFloatBulk() {
        val array = TestUtils.randomFloats()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 4)

        pointer.setFloatsUnsafe(array)

        for (i in array.indices) {
            assert(array[i] == pointer.getFloatUnsafe(i * 4L))
        }

        assert(array.contentEquals(pointer.getFloatsUnsafe()))
        assert(array.contentEquals(pointer.getFloatsUnsafe(FloatArray(array.size))))

        pointer.free()
    }

    @Test
    fun testDoubleBulk() {
        val array = TestUtils.randomDoubles()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 8)

        pointer.setDoublesUnsafe(array)

        for (i in array.indices) {
            assert(array[i] == pointer.getDoubleUnsafe(i * 8L))
        }

        assert(array.contentEquals(pointer.getDoublesUnsafe()))
        assert(array.contentEquals(pointer.getDoublesUnsafe(DoubleArray(array.size))))

        pointer.free()
    }

    @Test
    fun testCheckForEachIndexRange() {
        val pointer = MemoryPointer.malloc(8L)

        assertDoesNotThrow {
            pointer.checkForeachIndexRange(0, 8, 1)
            pointer.checkForeachIndexRange(0, 2, 4)
            pointer.checkForeachIndexRange(0, 1, 1)
            pointer.checkForeachIndexRange(0, 1, 4)
            pointer.checkForeachIndexRange(8, 0, 1)
            pointer.checkForeachIndexRange(8, 0, 4)
            pointer.checkForeachIndexRange(7, 1, 1)
            pointer.checkForeachIndexRange(3, 1, 4)
            pointer.checkForeachIndexRange(3, 1, 4)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            pointer.checkForeachIndexRange(-1, 1, 1)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            pointer.checkForeachIndexRange(9, 1, 1)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            pointer.checkForeachIndexRange(0, -1, 1)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            pointer.checkForeachIndexRange(0, 9, 1)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            pointer.checkForeachIndexRange(0, 1, 9)
        }

        pointer.free()
    }

    @Test
    fun testIterationByte() {
        val array = TestUtils.randomBytes()

        val pointer = MemoryPointer.malloc(array.size.toLong())

        pointer.setBytesUnsafe(array)

        var index = 0
        pointer.forEachByteUnsafe { v ->
            assert(v == array[index++])
        }

        pointer.forEachByteIndexedUnsafe(TestUtils.TEST_DATA_SIZE / 4L, TestUtils.TEST_DATA_SIZE / 2) { i, v ->
            assert(v == array[i + TestUtils.TEST_DATA_SIZE / 4])
        }

        pointer.free()
    }

    @Test
    fun testIterationShort() {
        val array = TestUtils.randomShorts()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 2)

        pointer.setShortsUnsafe(array)

        var index = 0
        pointer.forEachShortUnsafe { v ->
            assert(v == array[index++])
        }

        pointer.forEachShortIndexedUnsafe(TestUtils.TEST_DATA_SIZE / 4L * 2L, TestUtils.TEST_DATA_SIZE / 2) { i, v ->
            assert(v == array[i + TestUtils.TEST_DATA_SIZE / 4])
        }

        pointer.free()
    }

    @Test
    fun testIterationInt() {
        val array = TestUtils.randomInts()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 4)

        pointer.setIntsUnsafe(array)

        var index = 0
        pointer.forEachIntUnsafe { v ->
            assert(v == array[index++])
        }

        pointer.forEachIntIndexedUnsafe(TestUtils.TEST_DATA_SIZE / 4L * 4L, TestUtils.TEST_DATA_SIZE / 2) { i, v ->
            assert(v == array[i + TestUtils.TEST_DATA_SIZE / 4])
        }

        pointer.free()
    }

    @Test
    fun testIterationLong() {
        val array = TestUtils.randomLongs()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 8)

        pointer.setLongsUnsafe(array)

        var index = 0
        pointer.forEachLongUnsafe { v ->
            assert(v == array[index++])
        }

        pointer.forEachLongIndexedUnsafe(TestUtils.TEST_DATA_SIZE / 4L * 8L, TestUtils.TEST_DATA_SIZE / 2) { i, v ->
            assert(v == array[i + TestUtils.TEST_DATA_SIZE / 4])
        }

        pointer.free()
    }

    @Test
    fun testIterationFloat() {
        val array = TestUtils.randomFloats()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 4)

        pointer.setFloatsUnsafe(array)

        var index = 0
        pointer.forEachFloatUnsafe { v ->
            assert(v == array[index++])
        }

        pointer.forEachFloatIndexedUnsafe(TestUtils.TEST_DATA_SIZE / 4L * 4L, TestUtils.TEST_DATA_SIZE / 2) { i, v ->
            assert(v == array[i + TestUtils.TEST_DATA_SIZE / 4])
        }

        pointer.free()
    }

    @Test
    fun testIterationDouble() {
        val array = TestUtils.randomDoubles()

        val pointer = MemoryPointer.malloc(array.size.toLong() * 8)

        pointer.setDoublesUnsafe(array)

        var index = 0
        pointer.forEachDouble { v ->
            assert(v == array[index++])
        }

        pointer.forEachDoubleIndexed(TestUtils.TEST_DATA_SIZE / 4L * 8L, TestUtils.TEST_DATA_SIZE / 2) { i, v ->
            assert(v == array[i + TestUtils.TEST_DATA_SIZE / 4])
        }

        pointer.free()
    }
}