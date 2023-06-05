package dev.luna5ama.kmogus

@JvmInline
value class Span private constructor(val address: Long) {
    fun setByte(value: Byte) {
        UNSAFE.putByte(address, value)
    }

    fun setShort(value: Short) {
        UNSAFE.putShort(address, value)
    }

    fun setInt(value: Int) {
        UNSAFE.putInt(address, value)
    }

    fun setLong(value: Long) {
        UNSAFE.putLong(address, value)
    }

    fun setFloat(value: Float) {
        UNSAFE.putFloat(address, value)
    }

    fun setDouble(value: Double) {
        UNSAFE.putDouble(address, value)
    }


    fun setByte(offset: Long, value: Byte) {
        UNSAFE.putByte(address + offset, value)
    }

    fun setShort(offset: Long, value: Short) {
        UNSAFE.putShort(address + offset, value)
    }

    fun setInt(offset: Long, value: Int) {
        UNSAFE.putInt(address + offset, value)
    }

    fun setLong(offset: Long, value: Long) {
        UNSAFE.putLong(address + offset, value)
    }

    fun setFloat(offset: Long, value: Float) {
        UNSAFE.putFloat(address + offset, value)
    }

    fun setDouble(offset: Long, value: Double) {
        UNSAFE.putDouble(address + offset, value)
    }


    fun getByte(): Byte {
        return UNSAFE.getByte(address)
    }

    fun getShort(): Short {
        return UNSAFE.getShort(address)
    }

    fun getInt(): Int {
        return UNSAFE.getInt(address)
    }

    fun getLong(): Long {
        return UNSAFE.getLong(address)
    }

    fun getFloat(): Float {
        return UNSAFE.getFloat(address)
    }

    fun getDouble(): Double {
        return UNSAFE.getDouble(address)
    }


    fun getByte(offset: Long): Byte {
        return UNSAFE.getByte(address + offset)
    }

    fun getShort(offset: Long): Short {
        return UNSAFE.getShort(address + offset)
    }

    fun getInt(offset: Long): Int {
        return UNSAFE.getInt(address + offset)
    }

    fun getLong(offset: Long): Long {
        return UNSAFE.getLong(address + offset)
    }

    fun getFloat(offset: Long): Float {
        return UNSAFE.getFloat(address + offset)
    }

    fun getDouble(offset: Long): Double {
        return UNSAFE.getDouble(address + offset)
    }


    operator fun plus(offset: Long) = Span(address + offset)

    operator fun minus(offset: Long) = Span(address - offset)

    operator fun inc() = Span(address + 1)

    operator fun dec() = Span(address - 1)

    operator fun get(offset: Long) = Span(address + offset)

    operator fun set(offset: Long, value: Byte) {
        UNSAFE.putByte(address + offset, value)
    }

    operator fun set(offset: Long, value: Short) {
        UNSAFE.putShort(address + offset, value)
    }

    operator fun set(offset: Long, value: Int) {
        UNSAFE.putInt(address + offset, value)
    }

    operator fun set(offset: Long, value: Long) {
        UNSAFE.putLong(address + offset, value)
    }

    operator fun set(offset: Long, value: Float) {
        UNSAFE.putFloat(address + offset, value)
    }

    operator fun set(offset: Long, value: Double) {
        UNSAFE.putDouble(address + offset, value)
    }

    companion object {
        @JvmStatic
        fun MemoryPointer.asSpan() = Span(address)

        @JvmStatic
        fun MemoryPointer.asSpan(offset: Long) = Span(address + offset)

        @JvmStatic
        fun MemoryArray.asSpan() = Span(address + offset)

        @JvmStatic
        fun MemoryArray.asSpan(offset: Long) = Span(address + this.offset + offset)
    }
}