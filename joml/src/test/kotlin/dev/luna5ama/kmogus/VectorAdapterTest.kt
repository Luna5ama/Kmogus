package dev.luna5ama.kmogus

import org.joml.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VectorAdapterTest {
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
    fun vector2icCopyTo() {
        test {
            val v = Vector2i(114, -1) as Vector2ic
            val arr = Arr.malloc(2L * 4L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114, ptr.getInt(0L))
            assertEquals(-1, ptr.getInt(4L))

            arr.free()
        }
    }

    @Test
    fun vector2icMutableArray() {
        test {
            val v = Vector2i(114, -1) as Vector2ic
            val arr = Arr.malloc(2L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 8L

            v.copyToMutableArr(arr)

            assertEquals(16L, arr.offset)
            assertEquals(ptr.getInt(8L), 114)
            assertEquals(ptr.getInt(12L), -1)

            arr.free()
        }
    }

    @Test
    fun vector3icCopyTo() {
        test {
            val v = Vector3i(114, -1, 1919) as Vector3ic
            val arr = Arr.malloc(3L * 4L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114, ptr.getInt(0L))
            assertEquals(-1, ptr.getInt(4L))
            assertEquals(1919, ptr.getInt(8L))

            arr.free()
        }
    }

    @Test
    fun vector3icMutableArray() {
        test {
            val v = Vector3i(114, -1, 1919) as Vector3ic
            val arr = Arr.malloc(3L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 4L

            v.copyToMutableArr(arr)

            assertEquals(16L, arr.offset)
            assertEquals(ptr.getInt(4L), 114)
            assertEquals(ptr.getInt(8L), -1)
            assertEquals(ptr.getInt(12L), 1919)

            arr.free()
        }
    }

    @Test
    fun vector4icCopyTo() {
        test {
            val v = Vector4i(114, -1, 1919, 6) as Vector4ic
            val arr = Arr.malloc(4L * 4L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114, ptr.getInt(0L))
            assertEquals(-1, ptr.getInt(4L))
            assertEquals(1919, ptr.getInt(8L))
            assertEquals(6, ptr.getInt(12L))

            arr.free()
        }
    }

    @Test
    fun vector4icMutableArray() {
        test {
            val v = Vector4i(114, -1, 1919, 6) as Vector4ic
            val arr = Arr.malloc(4L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 8L

            v.copyToMutableArr(arr)

            assertEquals(24L, arr.offset)
            assertEquals(ptr.getInt(8L), 114)
            assertEquals(ptr.getInt(12L), -1)
            assertEquals(ptr.getInt(16L), 1919)
            assertEquals(ptr.getInt(20L), 6)

            arr.free()
        }
    }


    @Test
    fun vector2fcCopyTo() {
        test {
            val v = Vector2f(114.514f, -1f) as Vector2fc
            val arr = Arr.malloc(2L * 4L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))

            arr.free()
        }
    }

    @Test
    fun vector2fcMutableArray() {
        test {
            val v = Vector2f(114.514f, -1f) as Vector2fc
            val arr = Arr.malloc(2L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 8L

            v.copyToMutableArr(arr)

            assertEquals(16L, arr.offset)
            assertEquals(ptr.getFloat(8L), 114.514f)
            assertEquals(ptr.getFloat(12L), -1f)

            arr.free()
        }
    }

    @Test
    fun vector3fcCopyTo() {
        test {
            val v = Vector3f(114.514f, -1f, 1919f) as Vector3fc
            val arr = Arr.malloc(3L * 4L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919f, ptr.getFloat(8L))

            arr.free()
        }
    }

    @Test
    fun vector3fcMutableArray() {
        test {
            val v = Vector3f(114.514f, -1f, 1919f) as Vector3fc
            val arr = Arr.malloc(3L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 4L

            v.copyToMutableArr(arr)

            assertEquals(16L, arr.offset)
            assertEquals(ptr.getFloat(4L), 114.514f)
            assertEquals(ptr.getFloat(8L), -1f)
            assertEquals(ptr.getFloat(12L), 1919f)

            arr.free()
        }
    }

    @Test
    fun vector4fcCopyTo() {
        test {
            val v = Vector4f(114.514f, -1f, 1919f, 6f) as Vector4fc
            val arr = Arr.malloc(4L * 4L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919f, ptr.getFloat(8L))
            assertEquals(6f, ptr.getFloat(12L))

            arr.free()
        }
    }


    @Test
    fun vector4fcMutableArray() {
        test {
            val v = Vector4f(114.514f, -1f, 1919f, 6f) as Vector4fc
            val arr = Arr.malloc(4L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 8L

            v.copyToMutableArr(arr)

            assertEquals(24L, arr.offset)
            assertEquals(ptr.getFloat(8L), 114.514f)
            assertEquals(ptr.getFloat(12L), -1f)
            assertEquals(ptr.getFloat(16L), 1919f)
            assertEquals(ptr.getFloat(20L), 6f)

            arr.free()
        }
    }

    @Test
    fun vector2dcCopyTo() {
        test {
            val v = Vector2d(114.514, -1.0) as Vector2dc
            val arr = Arr.malloc(2L * 8L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114.514, ptr.getDouble(0L))
            assertEquals(-1.0, ptr.getDouble(8L))

            arr.free()
        }
    }

    @Test
    fun vector2dcMutableArray() {
        test {
            val v = Vector2d(114.514, -1.0) as Vector2dc
            val arr = Arr.malloc(2L * 8L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 8L

            v.copyToMutableArr(arr)

            assertEquals(24L, arr.offset)
            assertEquals(ptr.getDouble(8L), 114.514)
            assertEquals(ptr.getDouble(16L), -1.0)

            arr.free()
        }
    }

    @Test
    fun vector3dcCopyTo() {
        test {
            val v = Vector3d(114.514, -1.0, 1919.0) as Vector3dc
            val arr = Arr.malloc(3L * 8L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114.514, ptr.getDouble(0L))
            assertEquals(-1.0, ptr.getDouble(8L))
            assertEquals(1919.0, ptr.getDouble(16L))

            arr.free()
        }
    }

    @Test
    fun vector3dcMutableArray() {
        test {
            val v = Vector3d(114.514, -1.0, 1919.0) as Vector3dc
            val arr = Arr.malloc(3L * 8L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 8L

            v.copyToMutableArr(arr)

            assertEquals(32L, arr.offset)
            assertEquals(ptr.getDouble(8L), 114.514)
            assertEquals(ptr.getDouble(16L), -1.0)
            assertEquals(ptr.getDouble(24L), 1919.0)

            arr.free()
        }
    }

    @Test
    fun vector4dcCopyTo() {
        test {
            val v = Vector4d(114.514, -1.0, 1919.0, 6.0) as Vector4dc
            val arr = Arr.malloc(4L * 8L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114.514, ptr.getDouble(0L))
            assertEquals(-1.0, ptr.getDouble(8L))
            assertEquals(1919.0, ptr.getDouble(16L))
            assertEquals(6.0, ptr.getDouble(24L))

            arr.free()
        }
    }

    @Test
    fun vector4dcMutableArray() {
        test {
            val v = Vector4d(114.514, -1.0, 1919.0, 6.0) as Vector4dc
            val arr = Arr.malloc(4L * 8L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 8L

            v.copyToMutableArr(arr)

            assertEquals(40L, arr.offset)
            assertEquals(ptr.getDouble(8L), 114.514)
            assertEquals(ptr.getDouble(16L), -1.0)
            assertEquals(ptr.getDouble(24L), 1919.0)
            assertEquals(ptr.getDouble(32L), 6.0)

            arr.free()
        }
    }

    @Test
    fun vector2iCopyTo() {
        test {
            val v = Vector2i(114514, -1)
            val arr = Arr.malloc(2L * 4L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114514, ptr.getInt(0L))
            assertEquals(-1, ptr.getInt(4L))

            arr.free()
        }
    }

    @Test
    fun vector2iCopyFrom() {
        test {
            val arr = Arr.malloc(2L * 4L)
            val ptr = arr.ptr
            ptr.setInt(0L, 114514)
            ptr.setInt(4L, -1)

            val v = Vector2i()
            v.copyFrom(ptr)

            assertEquals(114514, v.x)
            assertEquals(-1, v.y)

            arr.free()
        }
    }

    @Test
    fun vector2iMutableArray() {
        test {
            val v = Vector2i(114514, -1)
            val arr = Arr.malloc(2L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 4L

            v.copyToMutableArr(arr)

            assertEquals(12L, arr.offset)
            assertEquals(ptr.getInt(4L), 114514)
            assertEquals(ptr.getInt(8L), -1)

            arr.free()
        }
    }

    @Test
    fun vector3iCopyTo() {
        test {
            val v = Vector3i(114514, -1, 1919810)
            val arr = Arr.malloc(3L * 4L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114514, ptr.getInt(0L))
            assertEquals(-1, ptr.getInt(4L))
            assertEquals(1919810, ptr.getInt(8L))

            arr.free()
        }
    }

    @Test
    fun vector3iCopyFrom() {
        test {
            val arr = Arr.malloc(3L * 4L)
            val ptr = arr.ptr
            ptr.setInt(0L, 114514)
            ptr.setInt(4L, -1)
            ptr.setInt(8L, 1919810)

            val v = Vector3i()
            v.copyFrom(ptr)

            assertEquals(114514, v.x)
            assertEquals(-1, v.y)
            assertEquals(1919810, v.z)

            arr.free()
        }
    }

    @Test
    fun vector3iMutableArray() {
        test {
            val v = Vector3i(114514, -1, 1919810)
            val arr = Arr.malloc(3L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 4L

            v.copyToMutableArr(arr)

            assertEquals(16L, arr.offset)
            assertEquals(ptr.getInt(4L), 114514)
            assertEquals(ptr.getInt(8L), -1)
            assertEquals(ptr.getInt(12L), 1919810)

            arr.free()
        }
    }

    @Test
    fun vector4iCopyTo() {
        test {
            val v = Vector4i(114514, -1, 1919810, 69420)
            val arr = Arr.malloc(4L * 4L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114514, ptr.getInt(0L))
            assertEquals(-1, ptr.getInt(4L))
            assertEquals(1919810, ptr.getInt(8L))
            assertEquals(69420, ptr.getInt(12L))

            arr.free()
        }
    }

    @Test
    fun vector4iCopyFrom() {
        test {
            val arr = Arr.malloc(4L * 4L)
            val ptr = arr.ptr
            ptr.setInt(0L, 114514)
            ptr.setInt(4L, -1)
            ptr.setInt(8L, 1919810)
            ptr.setInt(12L, 810)

            val v = Vector4i()
            v.copyFrom(ptr)

            assertEquals(114514, v.x)
            assertEquals(-1, v.y)
            assertEquals(1919810, v.z)
            assertEquals(810, v.w)

            arr.free()
        }
    }

    @Test
    fun vector4iMutableArray() {
        test {
            val v = Vector4i(114514, -1, 1919810, 810)
            val arr = Arr.malloc(4L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 4L

            v.copyToMutableArr(arr)

            assertEquals(20L, arr.offset)
            assertEquals(ptr.getInt(4L), 114514)
            assertEquals(ptr.getInt(8L), -1)
            assertEquals(ptr.getInt(12L), 1919810)
            assertEquals(ptr.getInt(16L), 810)

            arr.free()
        }
    }

    @Test
    fun vector2fCopyTo() {
        test {
            val v = Vector2f(114.514f, -1f)
            val arr = Arr.malloc(2L * 4L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))

            arr.free()
        }
    }

    @Test
    fun vector2fCopyFrom() {
        test {
            val arr = Arr.malloc(2L * 4L)
            val ptr = arr.ptr
            ptr.setFloat(0L, 114.514f)
            ptr.setFloat(4L, -1f)

            val v = Vector2f()
            v.copyFrom(ptr)

            assertEquals(114.514f, v.x)
            assertEquals(-1f, v.y)

            arr.free()
        }
    }

    @Test
    fun vector2fMutableArray() {
        test {
            val v = Vector2f(114.514f, -1f)
            val arr = Arr.malloc(2L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 4L

            v.copyToMutableArr(arr)

            assertEquals(12L, arr.offset)
            assertEquals(ptr.getFloat(4L), 114.514f)
            assertEquals(ptr.getFloat(8L), -1f)

            arr.free()
        }
    }

    @Test
    fun vector3fCopyTo() {
        test {
            val v = Vector3f(114.514f, -1f, 1919.810f)
            val arr = Arr.malloc(3L * 4L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919.810f, ptr.getFloat(8L))

            arr.free()
        }
    }

    @Test
    fun vector3fCopyFrom() {
        test {
            val arr = Arr.malloc(3L * 4L)
            val ptr = arr.ptr
            ptr.setFloat(0L, 114.514f)
            ptr.setFloat(4L, -1f)
            ptr.setFloat(8L, 1919.810f)

            val v = Vector3f()
            v.copyFrom(ptr)

            assertEquals(114.514f, v.x)
            assertEquals(-1f, v.y)
            assertEquals(1919.810f, v.z)

            arr.free()
        }
    }

    @Test
    fun vector3fMutableArray() {
        test {
            val v = Vector3f(114.514f, -1f, 1919.810f)
            val arr = Arr.malloc(3L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 4L

            v.copyToMutableArr(arr)

            assertEquals(16L, arr.offset)
            assertEquals(ptr.getFloat(4L), 114.514f)
            assertEquals(ptr.getFloat(8L), -1f)
            assertEquals(ptr.getFloat(12L), 1919.810f)

            arr.free()
        }
    }

    @Test
    fun vector4fCopyTo() {
        test {
            val v = Vector4f(114.514f, -1f, 1919.810f, 6.9f)
            val arr = Arr.malloc(4L * 4L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114.514f, ptr.getFloat(0L))
            assertEquals(-1f, ptr.getFloat(4L))
            assertEquals(1919.810f, ptr.getFloat(8L))
            assertEquals(6.9f, ptr.getFloat(12L))

            arr.free()
        }
    }

    @Test
    fun vector4fCopyFrom() {
        test {
            val arr = Arr.malloc(4L * 4L)
            val ptr = arr.ptr
            ptr.setFloat(0L, 114.514f)
            ptr.setFloat(4L, -1f)
            ptr.setFloat(8L, 1919.810f)
            ptr.setFloat(12L, 6.9f)

            val v = Vector4f()
            v.copyFrom(ptr)

            assertEquals(114.514f, v.x)
            assertEquals(-1f, v.y)
            assertEquals(1919.810f, v.z)
            assertEquals(6.9f, v.w)

            arr.free()
        }
    }

    @Test
    fun vector4fMutableArray() {
        test {
            val v = Vector4f(114.514f, -1f, 1919.810f, 6.9f)
            val arr = Arr.malloc(4L * 4L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 4L

            v.copyToMutableArr(arr)

            assertEquals(20L, arr.offset)
            assertEquals(ptr.getFloat(4L), 114.514f)
            assertEquals(ptr.getFloat(8L), -1f)
            assertEquals(ptr.getFloat(12L), 1919.810f)
            assertEquals(ptr.getFloat(16L), 6.9f)

            arr.free()
        }
    }

    @Test
    fun vector2dCopyTo() {
        test {
            val v = Vector2d(114.514, -1.0)
            val arr = Arr.malloc(2L * 8L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114.514, ptr.getDouble(0L))
            assertEquals(-1.0, ptr.getDouble(8L))

            arr.free()
        }
    }

    @Test
    fun vector2dCopyFrom() {
        test {
            val arr = Arr.malloc(2L * 8L)
            val ptr = arr.ptr
            ptr.setDouble(0L, 114.514)
            ptr.setDouble(8L, -1.0)

            val v = Vector2d()
            v.copyFrom(ptr)

            assertEquals(114.514, v.x)
            assertEquals(-1.0, v.y)

            arr.free()
        }
    }

    @Test
    fun vector2dMutableArray() {
        test {
            val v = Vector2d(114.514, -1.0)
            val arr = Arr.malloc(2L * 8L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 8L

            v.copyToMutableArr(arr)

            assertEquals(24L, arr.offset)
            assertEquals(ptr.getDouble(8L), 114.514)
            assertEquals(ptr.getDouble(16L), -1.0)

            arr.free()
        }
    }

    @Test
    fun vector3dCopyTo() {
        test {
            val v = Vector3d(114.514, -1.0, 1919.810)
            val arr = Arr.malloc(3L * 8L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114.514, ptr.getDouble(0L))
            assertEquals(-1.0, ptr.getDouble(8L))
            assertEquals(1919.810, ptr.getDouble(16L))

            arr.free()
        }
    }

    @Test
    fun vector3dCopyFrom() {
        test {
            val arr = Arr.malloc(3L * 8L)
            val ptr = arr.ptr
            ptr.setDouble(0L, 114.514)
            ptr.setDouble(8L, -1.0)
            ptr.setDouble(16L, 1919.810)

            val v = Vector3d()
            v.copyFrom(ptr)

            assertEquals(114.514, v.x)
            assertEquals(-1.0, v.y)
            assertEquals(1919.810, v.z)

            arr.free()
        }
    }

    @Test
    fun vector3dMutableArray() {
        test {
            val v = Vector3d(114.514, -1.0, 1919.810)
            val arr = Arr.malloc(3L * 8L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 8L

            v.copyToMutableArr(arr)

            assertEquals(32L, arr.offset)
            assertEquals(ptr.getDouble(8L), 114.514)
            assertEquals(ptr.getDouble(16L), -1.0)
            assertEquals(ptr.getDouble(24L), 1919.810)

            arr.free()
        }
    }

    @Test
    fun vector4dCopyTo() {
        test {
            val v = Vector4d(114.514, -1.0, 1919.810, 6.9)
            val arr = Arr.malloc(4L * 8L)
            val ptr = arr.ptr

            v.copyTo(ptr)

            assertEquals(114.514, ptr.getDouble(0L))
            assertEquals(-1.0, ptr.getDouble(8L))
            assertEquals(1919.810, ptr.getDouble(16L))
            assertEquals(6.9, ptr.getDouble(24L))

            arr.free()
        }
    }

    @Test
    fun vector4dCopyFrom() {
        test {
            val arr = Arr.malloc(4L * 8L)
            val ptr = arr.ptr
            ptr.setDouble(0L, 114.514)
            ptr.setDouble(8L, -1.0)
            ptr.setDouble(16L, 1919.810)
            ptr.setDouble(24L, 6.9)

            val v = Vector4d()
            v.copyFrom(ptr)

            assertEquals(114.514, v.x)
            assertEquals(-1.0, v.y)
            assertEquals(1919.810, v.z)
            assertEquals(6.9, v.w)

            arr.free()
        }
    }

    @Test
    fun vector4dMutableArray() {
        test {
            val v = Vector4d(114.514, -1.0, 1919.810, 6.9)
            val arr = Arr.malloc(4L * 8L + 8L).asMutable()
            val ptr = arr.ptr
            arr.offset = 8L

            v.copyToMutableArr(arr)

            assertEquals(40L, arr.offset)
            assertEquals(ptr.getDouble(8L), 114.514)
            assertEquals(ptr.getDouble(16L), -1.0)
            assertEquals(ptr.getDouble(24L), 1919.810)
            assertEquals(ptr.getDouble(32L), 6.9)

            arr.free()
        }
    }
}