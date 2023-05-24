package dev.luna5ama.kmogus

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

object TestUtils {
    const val TEST_DATA_SIZE = 1000

    fun allocDirectBuffer(capacity: Int): ByteBuffer {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder())
    }

    fun randomBytes(): ByteArray {
        val random = Random()
        val array = ByteArray(TEST_DATA_SIZE)
        random.nextBytes(array)
        return array
    }

    fun randomShorts(): ShortArray {
        val random = Random()
        val array = ShortArray(TEST_DATA_SIZE)
        for (i in array.indices) {
            array[i] = random.nextInt().toShort()
        }
        return array
    }

    fun randomInts(): IntArray {
        val random = Random()
        val array = IntArray(TEST_DATA_SIZE)
        for (i in array.indices) {
            array[i] = random.nextInt()
        }
        return array
    }

    fun randomLongs(): LongArray {
        val random = Random()
        val array = LongArray(TEST_DATA_SIZE)
        for (i in array.indices) {
            array[i] = random.nextLong()
        }
        return array
    }

    fun randomFloats(): FloatArray {
        val random = Random()
        val array = FloatArray(TEST_DATA_SIZE)
        for (i in array.indices) {
            array[i] = random.nextFloat()
        }
        return array
    }

    fun randomDoubles(): DoubleArray {
        val random = Random()
        val array = DoubleArray(TEST_DATA_SIZE)
        for (i in array.indices) {
            array[i] = random.nextDouble()
        }
        return array
    }
}