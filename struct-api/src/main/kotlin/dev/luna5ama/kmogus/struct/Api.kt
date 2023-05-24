package dev.luna5ama.kmogus.struct

import sun.misc.Unsafe

@JvmField
val UNSAFE = run {
    val field = Unsafe::class.java.getDeclaredField("theUnsafe")
    field.isAccessible = true
    field.get(null) as Unsafe
}

@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class StructSource(val pkg: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Struct(val sizeAlignment: Long = 4L, val fieldAlignment: Long = 1L)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Padding(val size: Long)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Field(val offset: Long, val size: Long)