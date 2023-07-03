package dev.luna5ama.kmogus

import sun.misc.Unsafe
import kotlin.reflect.KClass

internal val UNSAFE = run {
    val field = Unsafe::class.java.getDeclaredField("theUnsafe")
    field.isAccessible = true
    field.get(null) as Unsafe
}

internal fun getOffset(clazz: KClass<*>, name: String): Long {
    return UNSAFE.objectFieldOffset(clazz.java.getDeclaredField(name))
}

internal interface IAdapter<T> {
    fun copyTo(o: T, ptr: Ptr)
    fun copyFrom(o: T, ptr: Ptr)
}

internal fun checkContinuity(clazz: KClass<*>, vararg fieldNames: String): Boolean {
    check(fieldNames.isNotEmpty())
    var offset = getOffset(clazz, fieldNames[0])
    for (i in 1 until fieldNames.size) {
        val nextOffset = getOffset(clazz, fieldNames[i])
        if (i % 2 == 1 && nextOffset - offset != 4L) {
            return false
        }
        offset = nextOffset
    }
    return true
}

internal inline fun <reified T> selectAdapter(
    fast: IAdapter<T>,
    slow: IAdapter<T>,
    vararg fieldNames: String
): IAdapter<T> {
    return if (checkContinuity(T::class, *fieldNames)) {
        fast
    } else {
        slow
    }
}