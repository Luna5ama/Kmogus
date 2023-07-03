package dev.luna5ama.kmogus

import org.joml.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MatrixAdapterTest {
    private inline fun test(block: () -> Unit) {
        repeat(3) {
            when (it) {
                0 -> useSlow()
                1 -> useFast()
                2 -> useOptimal()
            }

            block()
        }
    }

    @Test
    fun matrix2fcCopyTo() {
        test {
            val m = Matrix2f(
                114.514f, -1f,
                1919f, 6f
            ) as Matrix2fc
            val arr = Arr.malloc(4L * 4L)
            val ptr = arr.ptr

            m.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919f, ptr.getFloat(8L))
            assertEquals(6f, ptr.getFloat(12L))

            arr.free()
        }
    }

    @Test
    fun matrix2fcMutableArray() {
        test {
            val m = Matrix2f(
                114.514f, -1f,
                1919f, 6f
            ) as Matrix2fc
            val arr = Arr.malloc(4L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.pos = 8L

            m.copyToMutableArr(arr)

            assertEquals(24L, arr.pos)
            assertEquals(ptr.getFloat(8L), 114.514f)
            assertEquals(ptr.getFloat(12L), -1f)
            assertEquals(ptr.getFloat(16L), 1919f)
            assertEquals(ptr.getFloat(20L), 6f)

            arr.free()
        }
    }

    @Test
    fun matrix3fcCopyTo() {
        test {
            val m = Matrix3f(
                114.514f, -1f, 1919f,
                6.9f, -420.69f, 1919.810f,
                -69.0f, -2.0f, -114514.0f
            ) as Matrix3fc

            val arr = Arr.malloc(9L * 4L)
            val ptr = arr.ptr

            m.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919f, ptr.getFloat(8L))
            assertEquals(6.9f, ptr.getFloat(12L))
            assertEquals(-420.69f, ptr.getFloat(16L))
            assertEquals(1919.810f, ptr.getFloat(20L))
            assertEquals(-69.0f, ptr.getFloat(24L))
            assertEquals(-2.0f, ptr.getFloat(28L))
            assertEquals(-114514.0f, ptr.getFloat(32L))

            arr.free()
        }
    }

    @Test
    fun matrix3fcMutableArray() {
        test {
            val m = Matrix3f(
                114.514f, -1f, 1919f,
                6.9f, -420.69f, 1919.810f,
                -69.0f, -2.0f, -114514.0f
            ) as Matrix3fc

            val arr = Arr.malloc(9L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.pos = 8L

            m.copyToMutableArr(arr)

            assertEquals(44L, arr.pos)
            assertEquals(ptr.getFloat(8L), 114.514f)
            assertEquals(ptr.getFloat(12L), -1f)
            assertEquals(ptr.getFloat(16L), 1919f)
            assertEquals(ptr.getFloat(20L), 6.9f)
            assertEquals(ptr.getFloat(24L), -420.69f)
            assertEquals(ptr.getFloat(28L), 1919.810f)
            assertEquals(ptr.getFloat(32L), -69.0f)
            assertEquals(ptr.getFloat(36L), -2.0f)
            assertEquals(ptr.getFloat(40L), -114514.0f)

            arr.free()
        }
    }

    @Test
    fun matrix3fcCopyTo2() {
        test {
            val m = Matrix3f(
                114.514f, -1f, 1919f,
                6.9f, -420.69f, 1919.810f,
                -69.0f, -2.0f, -114514.0f
            ) as Matrix3fc

            val arr = Arr.malloc(9L * 4L)
            val ptr = arr.ptr

            m.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919f, ptr.getFloat(8L))
            assertEquals(6.9f, ptr.getFloat(12L))
            assertEquals(-420.69f, ptr.getFloat(16L))
            assertEquals(1919.810f, ptr.getFloat(20L))
            assertEquals(-69.0f, ptr.getFloat(24L))
            assertEquals(-2.0f, ptr.getFloat(28L))
            assertEquals(-114514.0f, ptr.getFloat(32L))

            arr.free()
        }
    }

    @Test
    fun matrix3fcMutableArray2() {
        test {
            val m = Matrix3f(
                114.514f, -1f, 1919f,
                6.9f, -420.69f, 1919.810f,
                -69.0f, -2.0f, -114514.0f
            ) as Matrix3fc

            val arr = Arr.malloc(9L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.pos = 8L

            m.copyToMutableArr(arr)

            assertEquals(44L, arr.pos)
            assertEquals(ptr.getFloat(8L), 114.514f)
            assertEquals(ptr.getFloat(12L), -1f)
            assertEquals(ptr.getFloat(16L), 1919f)
            assertEquals(ptr.getFloat(20L), 6.9f)
            assertEquals(ptr.getFloat(24L), -420.69f)
            assertEquals(ptr.getFloat(28L), 1919.810f)
            assertEquals(ptr.getFloat(32L), -69.0f)
            assertEquals(ptr.getFloat(36L), -2.0f)
            assertEquals(ptr.getFloat(40L), -114514.0f)

            arr.free()
        }
    }

    @Test
    fun matrix4fcCopyTo() {
        test {
            val m = Matrix4f(
                114.514f, -1f, 1919f, 6.9f,
                -420.69f, 1919.810f, -69.0f, -2.0f,
                -114514.0f, 1337.0f, 2333.333f, 0.0f,
                1.0f, 2.0f, 3.0f, 4.0f
            ) as Matrix4fc

            val arr = Arr.malloc(16L * 4L)
            val ptr = arr.ptr

            m.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919f, ptr.getFloat(8L))
            assertEquals(6.9f, ptr.getFloat(12L))
            assertEquals(-420.69f, ptr.getFloat(16L))
            assertEquals(1919.810f, ptr.getFloat(20L))
            assertEquals(-69.0f, ptr.getFloat(24L))
            assertEquals(-2.0f, ptr.getFloat(28L))
            assertEquals(-114514.0f, ptr.getFloat(32L))
            assertEquals(1337.0f, ptr.getFloat(36L))
            assertEquals(2333.333f, ptr.getFloat(40L))
            assertEquals(0.0f, ptr.getFloat(44L))
            assertEquals(1.0f, ptr.getFloat(48L))
            assertEquals(2.0f, ptr.getFloat(52L))
            assertEquals(3.0f, ptr.getFloat(56L))
            assertEquals(4.0f, ptr.getFloat(60L))

            arr.free()
        }
    }

    @Test
    fun matrix4fcMutableArray() {
        test {
            val m = Matrix4f(
                114.514f, -1f, 1919f, 6.9f,
                -420.69f, 1919.810f, -69.0f, -2.0f,
                -114514.0f, 1337.0f, 2333.333f, 0.0f,
                1.0f, 2.0f, 3.0f, 4.0f
            ) as Matrix4fc

            val arr = Arr.malloc(16L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.pos = 8L

            m.copyToMutableArr(arr)

            assertEquals(72L, arr.pos)
            assertEquals(ptr.getFloat(8L), 114.514f)
            assertEquals(ptr.getFloat(12L), -1f)
            assertEquals(ptr.getFloat(16L), 1919f)
            assertEquals(ptr.getFloat(20L), 6.9f)
            assertEquals(ptr.getFloat(24L), -420.69f)
            assertEquals(ptr.getFloat(28L), 1919.810f)
            assertEquals(ptr.getFloat(32L), -69.0f)
            assertEquals(ptr.getFloat(36L), -2.0f)
            assertEquals(ptr.getFloat(40L), -114514.0f)
            assertEquals(ptr.getFloat(44L), 1337.0f)
            assertEquals(ptr.getFloat(48L), 2333.333f)
            assertEquals(ptr.getFloat(52L), 0.0f)
            assertEquals(ptr.getFloat(56L), 1.0f)
            assertEquals(ptr.getFloat(60L), 2.0f)
            assertEquals(ptr.getFloat(64L), 3.0f)
            assertEquals(ptr.getFloat(68L), 4.0f)

            arr.free()
        }
    }

    @Test
    fun matrix3x2fcCopyTo() {
        test {
            val m = Matrix3x2f(
                114.514f, -1f, 1919f,
                6.9f, -420.69f, 1919.810f
            ) as Matrix3x2fc

            val arr = Arr.malloc(6L * 4L)
            val ptr = arr.ptr

            m.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919f, ptr.getFloat(8L))
            assertEquals(6.9f, ptr.getFloat(12L))
            assertEquals(-420.69f, ptr.getFloat(16L))
            assertEquals(1919.810f, ptr.getFloat(20L))

            arr.free()
        }
    }

    @Test
    fun matrix3x2fcMutableArray() {
        test {
            val m = Matrix3x2f(
                114.514f, -1f, 1919f,
                6.9f, -420.69f, 1919.810f
            ) as Matrix3x2fc

            val arr = Arr.malloc(6L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.pos = 8L

            m.copyToMutableArr(arr)

            assertEquals(32L, arr.pos)
            assertEquals(ptr.getFloat(8L), 114.514f)
            assertEquals(ptr.getFloat(12L), -1f)
            assertEquals(ptr.getFloat(16L), 1919f)
            assertEquals(ptr.getFloat(20L), 6.9f)
            assertEquals(ptr.getFloat(24L), -420.69f)
            assertEquals(ptr.getFloat(28L), 1919.810f)

            arr.free()
        }
    }

    @Test
    fun matrix4x3fcCopyTo() {
        test {
            val m = Matrix4x3f(
                114.514f, -1f, 1919f, 6.9f,
                -420.69f, 1919.810f, -69.0f, -2.0f,
                -114514.0f, 1337.0f, 2333.333f, 0.0f
            ) as Matrix4x3fc

            val arr = Arr.malloc(12L * 4L)
            val ptr = arr.ptr

            m.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919f, ptr.getFloat(8L))
            assertEquals(6.9f, ptr.getFloat(12L))
            assertEquals(-420.69f, ptr.getFloat(16L))
            assertEquals(1919.810f, ptr.getFloat(20L))
            assertEquals(-69.0f, ptr.getFloat(24L))
            assertEquals(-2.0f, ptr.getFloat(28L))
            assertEquals(-114514.0f, ptr.getFloat(32L))
            assertEquals(1337.0f, ptr.getFloat(36L))
            assertEquals(2333.333f, ptr.getFloat(40L))
            assertEquals(0.0f, ptr.getFloat(44L))

            arr.free()
        }
    }

    @Test
    fun matrix4x3fcMutableArray() {
        test {
            val m = Matrix4x3f(
                114.514f, -1f, 1919f, 6.9f,
                -420.69f, 1919.810f, -69.0f, -2.0f,
                -114514.0f, 1337.0f, 2333.333f, 0.0f
            ) as Matrix4x3fc

            val arr = Arr.malloc(12L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.pos = 8L

            m.copyToMutableArr(arr)

            assertEquals(56L, arr.pos)
            assertEquals(ptr.getFloat(8L), 114.514f)
            assertEquals(ptr.getFloat(12L), -1f)
            assertEquals(ptr.getFloat(16L), 1919f)
            assertEquals(ptr.getFloat(20L), 6.9f)
            assertEquals(ptr.getFloat(24L), -420.69f)
            assertEquals(ptr.getFloat(28L), 1919.810f)
            assertEquals(ptr.getFloat(32L), -69.0f)
            assertEquals(ptr.getFloat(36L), -2.0f)
            assertEquals(ptr.getFloat(40L), -114514.0f)
            assertEquals(ptr.getFloat(44L), 1337.0f)
            assertEquals(ptr.getFloat(48L), 2333.333f)
            assertEquals(ptr.getFloat(52L), 0.0f)

            arr.free()
        }
    }

    @Test
    fun matrix2fCopyTo() {
        test {
            val m = Matrix2f(
                114.514f, -1f,
                1919f, 6.9f
            )

            val arr = Arr.malloc(4L * 4L)
            val ptr = arr.ptr

            m.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919f, ptr.getFloat(8L))
            assertEquals(6.9f, ptr.getFloat(12L))

            arr.free()
        }
    }

    @Test
    fun matrix2fCopyFrom() {
        test {
            val arr = Arr.malloc(4L * 4L)
            val ptr = arr.ptr

            ptr.setFloat(0L, 114.514f)
            ptr.setFloat(4L, -1f)
            ptr.setFloat(8L, 1919f)
            ptr.setFloat(12L, 6.9f)

            val m = Matrix2f()
            m.copyFrom(ptr)

            assertEquals(114.514f, m.m00())
            assertEquals(-1f, m.m01())
            assertEquals(1919f, m.m10())
            assertEquals(6.9f, m.m11())

            arr.free()
        }
    }

    @Test
    fun matrix2fMutableArray() {
        test {
            val m = Matrix2f(
                114.514f, -1f,
                1919f, 6.9f
            )

            val arr = Arr.malloc(4L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.pos = 8L

            m.copyToMutableArr(arr)

            assertEquals(24L, arr.pos)
            assertEquals(ptr.getFloat(8L), 114.514f)
            assertEquals(ptr.getFloat(12L), -1f)
            assertEquals(ptr.getFloat(16L), 1919f)
            assertEquals(ptr.getFloat(20L), 6.9f)

            arr.free()
        }
    }

    @Test
    fun matrix3fCopyTo() {
        test {
            val m = Matrix3f(
                114.514f, -1f, 1919f,
                6.9f, -420.69f, 1919.810f,
                -69.0f, -2.0f, -114514.0f
            )

            val arr = Arr.malloc(9L * 4L)
            val ptr = arr.ptr

            m.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919f, ptr.getFloat(8L))
            assertEquals(6.9f, ptr.getFloat(12L))
            assertEquals(-420.69f, ptr.getFloat(16L))
            assertEquals(1919.810f, ptr.getFloat(20L))
            assertEquals(-69.0f, ptr.getFloat(24L))
            assertEquals(-2.0f, ptr.getFloat(28L))
            assertEquals(-114514.0f, ptr.getFloat(32L))

            arr.free()
        }
    }

    @Test
    fun matrix3fCopyFrom() {
        test {
            val arr = Arr.malloc(9L * 4L)
            val ptr = arr.ptr

            ptr.setFloat(0L, 114.514f)
            ptr.setFloat(4L, -1f)
            ptr.setFloat(8L, 1919f)
            ptr.setFloat(12L, 6.9f)
            ptr.setFloat(16L, -420.69f)
            ptr.setFloat(20L, 1919.810f)
            ptr.setFloat(24L, -69.0f)
            ptr.setFloat(28L, -2.0f)
            ptr.setFloat(32L, -114514.0f)

            val m = Matrix3f()
            m.copyFrom(ptr)

            assertEquals(114.514f, m.m00())
            assertEquals(-1f, m.m01())
            assertEquals(1919f, m.m02())
            assertEquals(6.9f, m.m10())
            assertEquals(-420.69f, m.m11())
            assertEquals(1919.810f, m.m12())
            assertEquals(-69.0f, m.m20())
            assertEquals(-2.0f, m.m21())
            assertEquals(-114514.0f, m.m22())

            arr.free()
        }
    }

    @Test
    fun matrix3fMutableArray() {
        test {
            val m = Matrix3f(
                114.514f, -1f, 1919f,
                6.9f, -420.69f, 1919.810f,
                -69.0f, -2.0f, -114514.0f
            )

            val arr = Arr.malloc(9L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.pos = 8L

            m.copyToMutableArr(arr)

            assertEquals(44L, arr.pos)
            assertEquals(ptr.getFloat(8L), 114.514f)
            assertEquals(ptr.getFloat(12L), -1f)
            assertEquals(ptr.getFloat(16L), 1919f)
            assertEquals(ptr.getFloat(20L), 6.9f)
            assertEquals(ptr.getFloat(24L), -420.69f)
            assertEquals(ptr.getFloat(28L), 1919.810f)
            assertEquals(ptr.getFloat(32L), -69.0f)
            assertEquals(ptr.getFloat(36L), -2.0f)
            assertEquals(ptr.getFloat(40L), -114514.0f)

            arr.free()
        }
    }

    @Test
    fun matrix4fCopyTo() {
        test {
            val m = Matrix4f(
                114.514f, -1f, 1919f, 6.9f,
                -420.69f, 1919.810f, -69.0f, -2.0f,
                -114514.0f, 1337.0f, 2333.333f, 0.0f,
                1.0f, 2.0f, 3.0f, 4.0f
            )

            val arr = Arr.malloc(16L * 4L)
            val ptr = arr.ptr

            m.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919f, ptr.getFloat(8L))
            assertEquals(6.9f, ptr.getFloat(12L))
            assertEquals(-420.69f, ptr.getFloat(16L))
            assertEquals(1919.810f, ptr.getFloat(20L))
            assertEquals(-69.0f, ptr.getFloat(24L))
            assertEquals(-2.0f, ptr.getFloat(28L))
            assertEquals(-114514.0f, ptr.getFloat(32L))
            assertEquals(1337.0f, ptr.getFloat(36L))
            assertEquals(2333.333f, ptr.getFloat(40L))
            assertEquals(0.0f, ptr.getFloat(44L))
            assertEquals(1.0f, ptr.getFloat(48L))
            assertEquals(2.0f, ptr.getFloat(52L))
            assertEquals(3.0f, ptr.getFloat(56L))
            assertEquals(4.0f, ptr.getFloat(60L))

            arr.free()
        }
    }

    @Test
    fun matrix4fCopyFrom() {
        test {
            val arr = Arr.malloc(16L * 4L)
            val ptr = arr.ptr

            ptr.setFloat(0L, 114.514f)
            ptr.setFloat(4L, -1f)
            ptr.setFloat(8L, 1919f)
            ptr.setFloat(12L, 6.9f)
            ptr.setFloat(16L, -420.69f)
            ptr.setFloat(20L, 1919.810f)
            ptr.setFloat(24L, -69.0f)
            ptr.setFloat(28L, -2.0f)
            ptr.setFloat(32L, -114514.0f)
            ptr.setFloat(36L, 1337.0f)
            ptr.setFloat(40L, 2333.333f)
            ptr.setFloat(44L, 0.0f)
            ptr.setFloat(48L, 1.0f)
            ptr.setFloat(52L, 2.0f)
            ptr.setFloat(56L, 3.0f)
            ptr.setFloat(60L, 4.0f)

            val m = Matrix4f()
            m.copyFrom(ptr)

            assertEquals(114.514f, m.m00())
            assertEquals(-1f, m.m01())
            assertEquals(1919f, m.m02())
            assertEquals(6.9f, m.m03())
            assertEquals(-420.69f, m.m10())
            assertEquals(1919.810f, m.m11())
            assertEquals(-69.0f, m.m12())
            assertEquals(-2.0f, m.m13())
            assertEquals(-114514.0f, m.m20())
            assertEquals(1337.0f, m.m21())
            assertEquals(2333.333f, m.m22())
            assertEquals(0.0f, m.m23())
            assertEquals(1.0f, m.m30())
            assertEquals(2.0f, m.m31())
            assertEquals(3.0f, m.m32())
            assertEquals(4.0f, m.m33())

            arr.free()
        }
    }

    @Test
    fun matrix4fMutableArray() {
        test {
            val m = Matrix4f(
                114.514f, -1f, 1919f, 6.9f,
                -420.69f, 1919.810f, -69.0f, -2.0f,
                -114514.0f, 1337.0f, 2333.333f, 0.0f,
                1.0f, 2.0f, 3.0f, 4.0f
            )

            val arr = Arr.malloc(16L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.pos = 8L

            m.copyToMutableArr(arr)

            assertEquals(72L, arr.pos)
            assertEquals(ptr.getFloat(8L), 114.514f)
            assertEquals(ptr.getFloat(12L), -1f)
            assertEquals(ptr.getFloat(16L), 1919f)
            assertEquals(ptr.getFloat(20L), 6.9f)
            assertEquals(ptr.getFloat(24L), -420.69f)
            assertEquals(ptr.getFloat(28L), 1919.810f)
            assertEquals(ptr.getFloat(32L), -69.0f)
            assertEquals(ptr.getFloat(36L), -2.0f)
            assertEquals(ptr.getFloat(40L), -114514.0f)
            assertEquals(ptr.getFloat(44L), 1337.0f)
            assertEquals(ptr.getFloat(48L), 2333.333f)
            assertEquals(ptr.getFloat(52L), 0.0f)
            assertEquals(ptr.getFloat(56L), 1.0f)
            assertEquals(ptr.getFloat(60L), 2.0f)
            assertEquals(ptr.getFloat(64L), 3.0f)
            assertEquals(ptr.getFloat(68L), 4.0f)

            arr.free()
        }
    }

    @Test
    fun matrix3x2fCopyTo() {
        test {
            val m = Matrix3x2f(
                114.514f, -1f, 1919f,
                -420.69f, 1919.810f, -69.0f
            )

            val arr = Arr.malloc(6L * 4L)
            val ptr = arr.ptr

            m.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919f, ptr.getFloat(8L))
            assertEquals(-420.69f, ptr.getFloat(12L))
            assertEquals(1919.810f, ptr.getFloat(16L))
            assertEquals(-69.0f, ptr.getFloat(20L))

            arr.free()
        }
    }

    @Test
    fun matrix3x2fCopyFrom() {
        test {
            val arr = Arr.malloc(6L * 4L)
            val ptr = arr.ptr

            ptr.setFloat(0L, 114.514f)
            ptr.setFloat(4L, -1f)
            ptr.setFloat(8L, 1919f)
            ptr.setFloat(12L, -420.69f)
            ptr.setFloat(16L, 1919.810f)
            ptr.setFloat(20L, -69.0f)

            val m = Matrix3x2f()
            m.copyFrom(ptr)

            assertEquals(114.514f, m.m00())
            assertEquals(-1f, m.m01())
            assertEquals(1919f, m.m10())
            assertEquals(-420.69f, m.m11())
            assertEquals(1919.810f, m.m20())
            assertEquals(-69.0f, m.m21())

            arr.free()
        }
    }

    @Test
    fun matrix3x2fMutableArray() {
        test {
            val m = Matrix3x2f(
                114.514f, -1f, 1919f,
                -420.69f, 1919.810f, -69.0f
            )

            val arr = Arr.malloc(6L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.pos = 8L

            m.copyToMutableArr(arr)

            assertEquals(32L, arr.pos)
            assertEquals(ptr.getFloat(8L), 114.514f)
            assertEquals(ptr.getFloat(12L), -1f)
            assertEquals(ptr.getFloat(16L), 1919f)
            assertEquals(ptr.getFloat(20L), -420.69f)
            assertEquals(ptr.getFloat(24L), 1919.810f)
            assertEquals(ptr.getFloat(28L), -69.0f)

            arr.free()
        }
    }

    @Test
    fun matrix4x3fCopyTo() {
        test {
            val m = Matrix4x3f(
                114.514f, -1f, 1919f, 6.9f,
                -420.69f, 1919.810f, -69.0f, -2.0f,
                -114514.0f, 1337.0f, 2333.333f, 0.0f
            )

            val arr = Arr.malloc(12L * 4L)
            val ptr = arr.ptr

            m.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919f, ptr.getFloat(8L))
            assertEquals(6.9f, ptr.getFloat(12L))
            assertEquals(-420.69f, ptr.getFloat(16L))
            assertEquals(1919.810f, ptr.getFloat(20L))
            assertEquals(-69.0f, ptr.getFloat(24L))
            assertEquals(-2.0f, ptr.getFloat(28L))
            assertEquals(-114514.0f, ptr.getFloat(32L))
            assertEquals(1337.0f, ptr.getFloat(36L))
            assertEquals(2333.333f, ptr.getFloat(40L))
            assertEquals(0.0f, ptr.getFloat(44L))

            arr.free()
        }
    }

    @Test
    fun matrix4x3fCopyFrom() {
        test {
            val arr = Arr.malloc(12L * 4L)
            val ptr = arr.ptr

            ptr.setFloat(0L, 114.514f)
            ptr.setFloat(4L, -1f)
            ptr.setFloat(8L, 1919f)
            ptr.setFloat(12L, 6.9f)
            ptr.setFloat(16L, -420.69f)
            ptr.setFloat(20L, 1919.810f)
            ptr.setFloat(24L, -69.0f)
            ptr.setFloat(28L, -2.0f)
            ptr.setFloat(32L, -114514.0f)
            ptr.setFloat(36L, 1337.0f)
            ptr.setFloat(40L, 2333.333f)
            ptr.setFloat(44L, 0.0f)

            val m = Matrix4x3f()
            m.copyFrom(ptr)

            assertEquals(114.514f, m.m00())
            assertEquals(-1f, m.m01())
            assertEquals(1919f, m.m02())
            assertEquals(6.9f, m.m10())
            assertEquals(-420.69f, m.m11())
            assertEquals(1919.810f, m.m12())
            assertEquals(-69.0f, m.m20())
            assertEquals(-2.0f, m.m21())
            assertEquals(-114514.0f, m.m22())
            assertEquals(1337.0f, m.m30())
            assertEquals(2333.333f, m.m31())
            assertEquals(0.0f, m.m32())

            arr.free()
        }
    }

    @Test
    fun matrix4x3fMutableArray() {
        test {
            val m = Matrix4x3f(
                114.514f, -1f, 1919f, 6.9f,
                -420.69f, 1919.810f, -69.0f, -2.0f,
                -114514.0f, 1337.0f, 2333.333f, 0.0f
            )

            val arr = Arr.malloc(12L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.pos = 8L

            m.copyToMutableArr(arr)

            assertEquals(56L, arr.pos)
            assertEquals(ptr.getFloat(8L), 114.514f)
            assertEquals(ptr.getFloat(12L), -1f)
            assertEquals(ptr.getFloat(16L), 1919f)
            assertEquals(ptr.getFloat(20L), 6.9f)
            assertEquals(ptr.getFloat(24L), -420.69f)
            assertEquals(ptr.getFloat(28L), 1919.810f)
            assertEquals(ptr.getFloat(32L), -69.0f)
            assertEquals(ptr.getFloat(36L), -2.0f)
            assertEquals(ptr.getFloat(40L), -114514.0f)
            assertEquals(ptr.getFloat(44L), 1337.0f)
            assertEquals(ptr.getFloat(48L), 2333.333f)
            assertEquals(ptr.getFloat(52L), 0.0f)

            arr.free()
        }
    }
}