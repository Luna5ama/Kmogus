package dev.luna5ama.kmogus.struct

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Struct(val sizeAlignment: Long = 4L, val fieldAlignment: Boolean = false, val size: Long = -1)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Padding(val size: Long)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Field(val offset: Long, val size: Long)