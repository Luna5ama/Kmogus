package dev.luna5ama.kmogus

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertFailsWith

class MemoryArrayTest {
    @Test
    fun testEmpty() {
        val memArray = MemoryArray.malloc(0)
        assert(memArray.pointer == 0L)
        assert(memArray.isEmpty())
        memArray.pointer = 1L
        assert(memArray.pointer == 1L)
        assert(memArray.isNotEmpty())
        memArray.free()
    }

    @Test
    fun testRawWrapping() {
        assertFailsWith(IllegalArgumentException::class) {
            MemoryArray.wrap(0, -1)
        }

        assertFailsWith(IllegalArgumentException::class) {
            MemoryArray.wrap(-1, 0)
        }

        val address = UNSAFE.allocateMemory(TestUtils.TEST_DATA_SIZE * 4L)
        val memArray = MemoryArray.wrap(address, TestUtils.TEST_DATA_SIZE * 4L)
        assert(memArray.address == address)
        assert(memArray.length == TestUtils.TEST_DATA_SIZE * 4L)

        assertFailsWith(UnsupportedOperationException::class) {
            memArray.free()
        }

        val a = TestUtils.randomInts()
        memArray.pushInts(a)

        for (i in a.indices) {
            assert(memArray.getIntUnsafe(i * 4L) == a[i])
        }

        UNSAFE.freeMemory(address)
    }

    @Test
    fun testNioBufferWrapping() {
        val buffer = TestUtils.allocDirectBuffer(TestUtils.TEST_DATA_SIZE * 4)
        val memArray = MemoryArray.wrap(buffer)

        val a = TestUtils.randomInts()
        buffer.asIntBuffer().put(a)
        memArray.pointer += a.size * 4L

        for (i in a.indices.reversed()) {
            val v = memArray.popInt()
            assert(v == a[i])
            assert(v == buffer.getInt(i * 4))
        }

        assert(memArray.pointer == 0L)

        val b = TestUtils.randomInts()
        memArray.pushInts(b)

        for (i in b.indices.reversed()) {
            assert(buffer.getInt(i * 4) == b[i])
            assert(buffer.getInt(i * 4) == memArray.popInt())
        }
    }

    @Test
    fun testPushByteSingle() {
        val array = TestUtils.randomBytes()
        val memArray = MemoryArray.malloc(array.size.toLong())

        for (i in array.indices) {
            memArray.pushByte(array[i])
        }

        assert(memArray.pointer == memArray.length)

        for (i in array.indices) {
            assert(memArray.getByte(i.toLong()) == array[i])
        }

        memArray.free()
    }

    @Test
    fun testPushShortSingle() {
        val array = TestUtils.randomShorts()
        val memArray = MemoryArray.malloc(array.size.toLong() * 2)

        for (i in array.indices) {
            memArray.pushShort(array[i])
        }

        assert(memArray.pointer == memArray.length)

        for (i in array.indices) {
            assert(memArray.getShort(i.toLong() * 2) == array[i])
        }

        memArray.free()
    }

    @Test
    fun testPushIntSingle() {
        val array = TestUtils.randomInts()
        val memArray = MemoryArray.malloc(array.size.toLong() * 4)

        for (i in array.indices) {
            memArray.pushInt(array[i])
        }

        assert(memArray.pointer == memArray.length)

        for (i in array.indices) {
            assert(memArray.getInt(i.toLong() * 4) == array[i])
        }

        memArray.free()
    }

    @Test
    fun testPushLongSingle() {
        val array = TestUtils.randomLongs()
        val memArray = MemoryArray.malloc(array.size.toLong() * 8)

        for (i in array.indices) {
            memArray.pushLong(array[i])
        }

        assert(memArray.pointer == memArray.length)

        for (i in array.indices) {
            assert(memArray.getLong(i.toLong() * 8) == array[i])
        }

        memArray.free()
    }

    @Test
    fun testPushFloatSingle() {
        val array = TestUtils.randomFloats()
        val memArray = MemoryArray.malloc(array.size.toLong() * 4)

        for (i in array.indices) {
            memArray.pushFloat(array[i])
        }

        assert(memArray.pointer == memArray.length)

        for (i in array.indices) {
            assert(memArray.getFloat(i.toLong() * 4) == array[i])
        }

        memArray.free()
    }

    @Test
    fun testPushDoubleSingle() {
        val array = TestUtils.randomDoubles()
        val memArray = MemoryArray.malloc(array.size.toLong() * 8)

        for (i in array.indices) {
            memArray.pushDouble(array[i])
        }

        assert(memArray.pointer == memArray.length)

        for (i in array.indices) {
            assert(memArray.getDouble(i.toLong() * 8) == array[i])
        }

        memArray.free()
    }

    @Test
    fun testPushMixedSingle() {
        val bytes = TestUtils.randomBytes()
        val shorts = TestUtils.randomShorts()
        val ints = TestUtils.randomInts()
        val longs = TestUtils.randomLongs()
        val floats = TestUtils.randomFloats()
        val doubles = TestUtils.randomDoubles()
        val memArray = MemoryArray.malloc(
            bytes.size.toLong() +
                shorts.size.toLong() * 2 +
                ints.size.toLong() * 4 +
                longs.size.toLong() * 8 +
                floats.size.toLong() * 4 +
                doubles.size.toLong() * 8
        )

        for (i in bytes.indices) {
            memArray.pushByte(bytes[i])
            memArray.pushShort(shorts[i])
            memArray.pushInt(ints[i])
            memArray.pushLong(longs[i])
            memArray.pushFloat(floats[i])
            memArray.pushDouble(doubles[i])
        }

        assert(memArray.pointer == memArray.length)

        var index = 0
        for (i in bytes.indices) {
            assert(memArray.getByte(index.toLong()) == bytes[i])
            index++
            assert(memArray.getShort(index.toLong()) == shorts[i])
            index += 2
            assert(memArray.getInt(index.toLong()) == ints[i])
            index += 4
            assert(memArray.getLong(index.toLong()) == longs[i])
            index += 8
            assert(memArray.getFloat(index.toLong()) == floats[i])
            index += 4
            assert(memArray.getDouble(index.toLong()) == doubles[i])
            index += 8
        }

        memArray.free()
    }

    @Test
    fun testPushByteBulk() {
        val array1 = TestUtils.randomBytes()
        val array2 = TestUtils.randomBytes()
        val memArray = MemoryArray.malloc(array1.size.toLong() + array2.size.toLong())

        memArray.pushBytes(array1)
        memArray.pushBytes(array2)

        assert(memArray.pointer == memArray.length)

        for (i in array1.indices) {
            assert(memArray.getByte(i.toLong()) == array1[i])
        }
        for (i in array2.indices) {
            assert(memArray.getByte((i + array1.size).toLong()) == array2[i])
        }

        memArray.free()
    }

    @Test
    fun testPushShortBulk() {
        val array1 = TestUtils.randomShorts()
        val array2 = TestUtils.randomShorts()
        val memArray = MemoryArray.malloc((array1.size + array2.size).toLong() * 2)

        memArray.pushShorts(array1)
        memArray.pushShorts(array2)

        assert(memArray.pointer == memArray.length)

        for (i in array1.indices) {
            assert(memArray.getShort(i.toLong() * 2) == array1[i])
        }
        for (i in array2.indices) {
            assert(memArray.getShort((i + array1.size).toLong() * 2) == array2[i])
        }

        memArray.free()
    }

    @Test
    fun testPushIntBulk() {
        val array1 = TestUtils.randomInts()
        val array2 = TestUtils.randomInts()
        val memArray = MemoryArray.malloc((array1.size + array2.size).toLong() * 4)

        memArray.pushInts(array1)
        memArray.pushInts(array2)

        assert(memArray.pointer == memArray.length)

        for (i in array1.indices) {
            assert(memArray.getInt(i.toLong() * 4) == array1[i])
        }
        for (i in array2.indices) {
            assert(memArray.getInt((i + array1.size).toLong() * 4) == array2[i])
        }

        memArray.free()
    }

    @Test
    fun testPushLongBulk() {
        val array1 = TestUtils.randomLongs()
        val array2 = TestUtils.randomLongs()
        val memArray = MemoryArray.malloc((array1.size + array2.size).toLong() * 8)

        memArray.pushLongs(array1)
        memArray.pushLongs(array2)

        assert(memArray.pointer == memArray.length)

        for (i in array1.indices) {
            assert(memArray.getLong(i.toLong() * 8) == array1[i])
        }
        for (i in array2.indices) {
            assert(memArray.getLong((i + array1.size).toLong() * 8) == array2[i])
        }

        memArray.free()
    }

    @Test
    fun testPushFloatBulk() {
        val array1 = TestUtils.randomFloats()
        val array2 = TestUtils.randomFloats()
        val memArray = MemoryArray.malloc((array1.size + array2.size).toLong() * 4)

        memArray.pushFloats(array1)
        memArray.pushFloats(array2)

        assert(memArray.pointer == memArray.length)

        for (i in array1.indices) {
            assert(memArray.getFloat(i.toLong() * 4) == array1[i])
        }
        for (i in array2.indices) {
            assert(memArray.getFloat((i + array1.size).toLong() * 4) == array2[i])
        }

        memArray.free()
    }

    @Test
    fun testPushDoubleBulk() {
        val array1 = TestUtils.randomDoubles()
        val array2 = TestUtils.randomDoubles()
        val memArray = MemoryArray.malloc((array1.size + array2.size).toLong() * 8)

        memArray.pushDoubles(array1)
        memArray.pushDoubles(array2)

        assert(memArray.pointer == memArray.length)

        for (i in array1.indices) {
            assert(memArray.getDouble(i.toLong() * 8) == array1[i])
        }
        for (i in array2.indices) {
            assert(memArray.getDouble((i + array1.size).toLong() * 8) == array2[i])
        }

        memArray.free()
    }

    @Test
    fun testPopByte() {
        val array = TestUtils.randomBytes()
        val memArray = MemoryArray.malloc(array.size.toLong())

        for (i in array.indices) {
            memArray.pushByte(array[i])
        }

        assert(memArray.pointer == memArray.length)

        for (i in array.indices) {
            assert(memArray.popByte() == array[array.size - i - 1])
        }

        assert(memArray.pointer == 0L)

        memArray.free()
    }

    @Test
    fun testPopShort() {
        val array = TestUtils.randomShorts()
        val memArray = MemoryArray.malloc(array.size.toLong() * 2)

        for (i in array.indices) {
            memArray.pushShort(array[i])
        }

        assert(memArray.pointer == memArray.length)

        for (i in array.indices) {
            assert(memArray.popShort() == array[array.size - i - 1])
        }

        assert(memArray.pointer == 0L)

        memArray.free()
    }

    @Test
    fun testPopInt() {
        val array = TestUtils.randomInts()
        val memArray = MemoryArray.malloc(array.size.toLong() * 4)

        for (i in array.indices) {
            memArray.pushInt(array[i])
        }

        assert(memArray.pointer == memArray.length)

        for (i in array.indices) {
            assert(memArray.popInt() == array[array.size - i - 1])
        }

        assert(memArray.pointer == 0L)

        memArray.free()
    }

    @Test
    fun testPopLong() {
        val array = TestUtils.randomLongs()
        val memArray = MemoryArray.malloc(array.size.toLong() * 8)

        for (i in array.indices) {
            memArray.pushLong(array[i])
        }

        assert(memArray.pointer == memArray.length)

        for (i in array.indices) {
            assert(memArray.popLong() == array[array.size - i - 1])
        }

        assert(memArray.pointer == 0L)

        memArray.free()
    }

    @Test
    fun testPopFloat() {
        val array = TestUtils.randomFloats()
        val memArray = MemoryArray.malloc(array.size.toLong() * 4)

        for (i in array.indices) {
            memArray.pushFloat(array[i])
        }

        assert(memArray.pointer == memArray.length)

        for (i in array.indices) {
            assert(memArray.popFloat() == array[array.size - i - 1])
        }

        assert(memArray.pointer == 0L)

        memArray.free()
    }

    @Test
    fun testPopDouble() {
        val array = TestUtils.randomDoubles()
        val memArray = MemoryArray.malloc(array.size.toLong() * 8)

        for (i in array.indices) {
            memArray.pushDouble(array[i])
        }

        assert(memArray.pointer == memArray.length)

        for (i in array.indices) {
            assert(memArray.popDouble() == array[array.size - i - 1])
        }

        assert(memArray.pointer == 0L)

        memArray.free()
    }

    @Test
    fun testPopMixed() {
        val bytes = TestUtils.randomBytes()
        val shorts = TestUtils.randomShorts()
        val ints = TestUtils.randomInts()
        val longs = TestUtils.randomLongs()
        val floats = TestUtils.randomFloats()
        val doubles = TestUtils.randomDoubles()
        val memArray = MemoryArray.malloc(
            bytes.size.toLong() +
                shorts.size.toLong() * 2 +
                ints.size.toLong() * 4 +
                longs.size.toLong() * 8 +
                floats.size.toLong() * 4 +
                doubles.size.toLong() * 8
        )

        for (i in bytes.indices) {
            memArray.pushByte(bytes[i])
            memArray.pushShort(shorts[i])
            memArray.pushInt(ints[i])
            memArray.pushLong(longs[i])
            memArray.pushFloat(floats[i])
            memArray.pushDouble(doubles[i])
        }

        assert(memArray.pointer == memArray.length)

        for (i in bytes.indices) {
            assert(memArray.popDouble() == doubles[bytes.size - i - 1])
            assert(memArray.popFloat() == floats[bytes.size - i - 1])
            assert(memArray.popLong() == longs[bytes.size - i - 1])
            assert(memArray.popInt() == ints[bytes.size - i - 1])
            assert(memArray.popShort() == shorts[bytes.size - i - 1])
            assert(memArray.popByte() == bytes[bytes.size - i - 1])
        }

        assert(memArray.pointer == 0L)

        memArray.free()
    }

    @Test
    fun testCheckForEachIndexRange() {
        val memArray = MemoryArray.malloc(8L)
        memArray.pointer = 8L

        assertDoesNotThrow {
            memArray.checkForeachIndexRange(0, 8, 1)
            memArray.checkForeachIndexRange(0, 2, 4)
            memArray.checkForeachIndexRange(0, 1, 1)
            memArray.checkForeachIndexRange(0, 1, 4)
            memArray.checkForeachIndexRange(8, 0, 1)
            memArray.checkForeachIndexRange(8, 0, 4)
            memArray.checkForeachIndexRange(7, 1, 1)
            memArray.checkForeachIndexRange(3, 1, 4)
            memArray.checkForeachIndexRange(3, 1, 4)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            memArray.checkForeachIndexRange(-1, 1, 1)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            memArray.checkForeachIndexRange(9, 1, 1)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            memArray.checkForeachIndexRange(0, -1, 1)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            memArray.checkForeachIndexRange(0, 9, 1)
        }
        assertFailsWith(IndexOutOfBoundsException::class) {
            memArray.checkForeachIndexRange(0, 1, 9)
        }

        memArray.free()
    }

    @Test
    fun testIterationByte() {
        val array = TestUtils.randomBytes()

        val memArray = MemoryArray.malloc(array.size.toLong())

        memArray.pushBytes(array)

        var index = 0
        memArray.forEachByteUnsafe { v ->
            assert(v == array[index++])
        }

        memArray.forEachByteIndexedUnsafe(TestUtils.TEST_DATA_SIZE / 4L, TestUtils.TEST_DATA_SIZE / 2) { i, v ->
            assert(v == array[i + TestUtils.TEST_DATA_SIZE / 4])
        }

        memArray.free()
    }

    @Test
    fun testIterationShort() {
        val array = TestUtils.randomShorts()

        val memArray = MemoryArray.malloc(array.size.toLong() * 2)

        memArray.pushShorts(array)

        var index = 0
        memArray.forEachShortUnsafe { v ->
            assert(v == array[index++])
        }

        memArray.forEachShortIndexedUnsafe(TestUtils.TEST_DATA_SIZE / 4L * 2L, TestUtils.TEST_DATA_SIZE / 2) { i, v ->
            assert(v == array[i + TestUtils.TEST_DATA_SIZE / 4])
        }

        memArray.free()
    }

    @Test
    fun testIterationInt() {
        val array = TestUtils.randomInts()

        val memArray = MemoryArray.malloc(array.size.toLong() * 4)

        memArray.pushInts(array)

        var index = 0
        memArray.forEachIntUnsafe { v ->
            assert(v == array[index++])
        }

        memArray.forEachIntIndexedUnsafe(TestUtils.TEST_DATA_SIZE / 4L * 4L, TestUtils.TEST_DATA_SIZE / 2) { i, v ->
            assert(v == array[i + TestUtils.TEST_DATA_SIZE / 4])
        }

        memArray.free()
    }

    @Test
    fun testIterationLong() {
        val array = TestUtils.randomLongs()

        val memArray = MemoryArray.malloc(array.size.toLong() * 8)

        memArray.pushLongs(array)

        var index = 0
        memArray.forEachLongUnsafe { v ->
            assert(v == array[index++])
        }

        memArray.forEachLongIndexedUnsafe(TestUtils.TEST_DATA_SIZE / 4L * 8L, TestUtils.TEST_DATA_SIZE / 2) { i, v ->
            assert(v == array[i + TestUtils.TEST_DATA_SIZE / 4])
        }

        memArray.free()
    }

    @Test
    fun testIterationFloat() {
        val array = TestUtils.randomFloats()

        val memArray = MemoryArray.malloc(array.size.toLong() * 4)

        memArray.pushFloats(array)

        var index = 0
        memArray.forEachFloatUnsafe { v ->
            assert(v == array[index++])
        }

        memArray.forEachFloatIndexedUnsafe(TestUtils.TEST_DATA_SIZE / 4L * 4L, TestUtils.TEST_DATA_SIZE / 2) { i, v ->
            assert(v == array[i + TestUtils.TEST_DATA_SIZE / 4])
        }

        memArray.free()
    }

    @Test
    fun testIterationDouble() {
        val array = TestUtils.randomDoubles()

        val memArray = MemoryArray.malloc(array.size.toLong() * 8)

        memArray.pushDoubles(array)

        var index = 0
        memArray.forEachDouble { v ->
            assert(v == array[index++])
        }

        memArray.forEachDoubleIndexed(TestUtils.TEST_DATA_SIZE / 4L * 8L, TestUtils.TEST_DATA_SIZE / 2) { i, v ->
            assert(v == array[i + TestUtils.TEST_DATA_SIZE / 4])
        }

        memArray.free()
    }
}