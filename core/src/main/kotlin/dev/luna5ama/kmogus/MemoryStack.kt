package dev.luna5ama.kmogus

import java.util.*

class MemoryStack private constructor(initCapacity: Long) : AutoCloseable {
    private val base = MemoryPointer.malloc(initCapacity)
    private var baseOffset = 0L

    private val pointerPool = ArrayDeque<Pointer>()
    private val pointerStack = PointerStack()
    private val counterStack = CounterStack()

    private fun newPointer(): Pointer {
        return pointerPool.pollLast() ?: Pointer()
    }

    private fun freePointer(frame: Pointer) {
        pointerPool.offerLast(frame)
    }

    fun push(): MemoryStack {
        check(counterStack.index >= -1) { "Memory stack is corrupted: frameCounter=${counterStack.index}" }
        counterStack.push()
        return this
    }

    private fun malloc0(size: Long): Pointer {
        val pointer = newPointer()

        val offset = baseOffset
        baseOffset += size
        base.ensureCapacity(baseOffset, false)

        counterStack.inc()
        pointer.frameIndex = counterStack.index
        pointer.stackIndex = pointerStack.push(pointer)
        pointer.address = base.address + offset
        pointer.length = size

        return pointer
    }

    fun malloc(size: Long): MemoryPointer {
        return malloc0(size)
    }

    fun calloc(size: Long): MemoryPointer {
        val pointer = malloc0(size)
        UNSAFE.setMemory(pointer.address, size, 0)
        return pointer
    }

    fun checkEmpty() {
        check(counterStack.index == -1) { "Memory stack is not empty: frameCounter=${counterStack.index}" }
        check(pointerStack.size == 0) { "Memory stack is not empty: pointerStack.size=${pointerStack.size}" }
    }

    override fun close() {
        check(counterStack.index >= -1) { "Memory stack is corrupted: frameCounter=${counterStack.index}" }
        check(counterStack.index != -1) { "Memory stack is empty: frameCounter=${counterStack.index}" }
        repeat(counterStack.pop()) {
            pointerStack.peek().release()
        }
    }

    private class PointerStack {
        var size = 0; private set

        private var array = arrayOfNulls<Pointer>(16)

        fun push(pointer: Pointer): Int {
            if (size == array.size) array = array.copyOf(size * 2)
            array[size] = pointer
            return size++
        }

        fun pop(): Pointer {
            return array[--size]!!
        }

        fun peek(): Pointer {
            return array[size - 1]!!
        }

        operator fun set(index: Int, pointer: Pointer) {
            array[index] = pointer
        }
    }

    private class CounterStack {
        var index = -1; private set

        private var array = IntArray(16)

        fun push(): Int {
            if (++index == array.size) array = array.copyOf(array.size * 2)
            array[index] = 0
            return index
        }

        fun pop(): Int {
            return array[index--]
        }

        fun inc() {
            array[index]++
        }
    }

    private inner class Pointer : MemoryPointer {
        var stackIndex = 0
        var frameIndex = 0

        override var address: Long = 0L
        override var length: Long = 0L

        override fun reallocate(newLength: Long, init: Boolean) {
            check(frameIndex == pointerStack.size - 1) { "Cannot reallocate pointer from previous stack frame" }

            val prevAddress = address
            val prevLength = length

            if (newLength == prevLength) return

            if (newLength > prevLength) {
                val otherPointer = malloc0(newLength)

                address = otherPointer.address
                length = newLength

                otherPointer.address = prevAddress
                otherPointer.length = prevLength

                pointerStack[stackIndex] = otherPointer
                pointerStack[otherPointer.stackIndex] = this

                UNSAFE.copyMemory(prevAddress, address, prevLength)
                if (init) {
                    UNSAFE.setMemory(address + prevLength, newLength - prevLength, 0)
                }
            } else {
                length = newLength
                val dummy = newPointer()
                dummy.frameIndex = frameIndex
                dummy.stackIndex = pointerStack.push(dummy)
                dummy.address = address + newLength
                dummy.length = prevLength - newLength
                counterStack.inc()
            }
        }

        override fun free() {
            throw UnsupportedOperationException("Cannot free stack frame")
        }

        fun release() {
            val stackTop = counterStack.index + 1
            check(frameIndex == stackTop) { "Frame stack is corrupted while releasing top pointers, expected current frame: $frameIndex, actual: $stackTop" }
            val last = pointerStack.pop()
            check(last === this) { "Frame stack is corrupted while releasing top pointers, expected pointer: $this, actual: $last" }
            baseOffset -= length
            freePointer(this)
        }
    }

    companion object {
        private val threadLocal = ThreadLocal.withInitial { MemoryStack(1024L * 1024L) }

        fun get(): MemoryStack {
            return threadLocal.get()
        }

        inline operator fun <T> invoke(crossinline block: MemoryStack.() -> T): T {
            return get().push().use(block)
        }
    }
}