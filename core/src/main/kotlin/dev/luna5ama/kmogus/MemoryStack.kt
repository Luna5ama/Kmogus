package dev.luna5ama.kmogus

import java.util.*

class MemoryStack(initCapacity: Long) : AutoCloseable {
    private val base = Arr.malloc(initCapacity)
    private var baseOffset = 0L

    private val containerPool = ArrayDeque<Container>()
    private val containerStack = ContainerStack()
    private val counterStack = CounterStack()

    private fun newContainer(): Container {
        return containerPool.pollLast() ?: Container()
    }

    private fun freeContainer(frame: Container) {
        containerPool.offerLast(frame)
    }

    fun push(): MemoryStack {
        check(counterStack.index >= -1) { "Memory stack is corrupted: frameCounter=${counterStack.index}" }
        counterStack.push()
        return this
    }

    private fun malloc0(size: Long): Container {
        check(baseOffset >= 0L) { "Memory stack is corrupted: baseOffset=$baseOffset" }

        val container = newContainer()

        val alignedSize = (size + 7L) and 0xFFFFFFF8L

        val offset = baseOffset
        baseOffset += alignedSize
        base.ensureCapacity(baseOffset, false)

        counterStack.inc()
        container.frameIndex = counterStack.index
        container.stackIndex = containerStack.push(container)
        container.offset = offset
        container.len = size
        container.padding = alignedSize - size

        return container
    }

    fun malloc(size: Long): Arr {
        return malloc0(size)
    }

    fun calloc(size: Long): Arr {
        val container = malloc0(size)
        container.ptr.setMemory(size, 0)
        return container
    }

    fun checkEmpty() {
        check(counterStack.index == -1) { "Memory stack is not empty: frameCounter=${counterStack.index}" }
        check(containerStack.size == 0) { "Memory stack is not empty: containerStack.size=${containerStack.size}" }
    }

    override fun close() {
        check(counterStack.index >= -1) { "Memory stack is corrupted: frameCounter=${counterStack.index}" }
        check(counterStack.index != -1) { "Memory stack is empty: frameCounter=${counterStack.index}" }
        repeat(counterStack.pop()) {
            containerStack.peek().release()
        }
    }

    private class ContainerStack {
        var size = 0; private set

        private var array = arrayOfNulls<Container>(16)

        fun push(container: Container): Int {
            if (size == array.size) array = array.copyOf(size * 2)
            array[size] = container
            return size++
        }

        fun pop(): Container {
            return array[--size]!!
        }

        fun peek(): Container {
            return array[size - 1]!!
        }

        operator fun set(index: Int, container: Container) {
            array[index] = container
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

    private inner class Container : Arr {
        var stackIndex = 0
        var frameIndex = 0

        var offset = 0L

        override val ptr get() = base.ptr + offset
        override var len = 0L

        var padding = 0L

        override fun realloc(newLength: Long, init: Boolean): Arr {
            check(frameIndex == counterStack.index) { "Cannot reallocate ptr from previous stack frame" }

            val prevOffset = offset
            val prevLength = len
            val prevPadding = padding

            if (newLength == prevLength) return this

            val alignedLen = (newLength + 7L) and 0xFFFFFFF8L
            if (alignedLen == prevLength) {
                len = newLength
                padding = alignedLen - newLength
                return this
            }

            if (newLength > prevLength) {
                if (containerStack.peek() !== this) {
                    val otherPointer = malloc0(newLength)

                    offset = otherPointer.offset
                    padding = otherPointer.padding

                    otherPointer.offset = prevOffset
                    otherPointer.len = prevLength
                    otherPointer.padding = prevPadding

                    containerStack[stackIndex] = otherPointer
                    containerStack[otherPointer.stackIndex] = this

                    memcpy(otherPointer.ptr, ptr, prevLength)
                } else {
                    base.ensureCapacity(ptr.address - base.ptr.address + newLength, false)
                    baseOffset += (newLength + padding) - (prevLength + prevPadding)
                }

                len = newLength

                if (init) {
                    (ptr + prevLength).setMemory(newLength - prevLength, 0)
                }
            } else {
                len = newLength

                if (containerStack.peek() !== this) {
                    padding += prevLength - newLength
                } else {
                    baseOffset -= (prevLength + padding) - (alignedLen)
                    padding = alignedLen - newLength
                }
            }

            return this
        }

        override fun free() {
            throw UnsupportedOperationException("Cannot free stack frame")
        }

        fun release() {
            val stackTop = counterStack.index + 1
            check(frameIndex == stackTop) { "Frame stack is corrupted while releasing top pointers, expected current frame: $frameIndex, actual: $stackTop" }
            val last = containerStack.pop()
            check(last === this) { "Frame stack is corrupted while releasing top pointers, expected ptr: $this, actual: $last" }
            baseOffset -= len
            check(baseOffset >= 0) { "Frame stack is corrupted while releasing top pointers, baseOffset: $baseOffset" }
            freeContainer(this)
        }
    }

    companion object {
        private val threadLocal = ThreadLocal.withInitial { MemoryStack(1024L * 1024L) }

        fun get(): MemoryStack {
            return threadLocal.get()
        }

        inline operator fun <T> invoke(block: MemoryStack.() -> T): T {
            return get().push().use(block)
        }
    }
}