package dev.luna5ama.kmogus

import java.util.*

class MemoryStack private constructor(initCapacity: Long) : AutoCloseable {
    private val base = MemoryPointer.malloc(initCapacity)
    private var baseOffset = 0L

    private val framesPool = ArrayDeque<Frame>()
    private val frameStack = ArrayDeque<Frame>()
    private val stackCount = IntArray(16)
    private var stackCountPointer = 0

    private fun newFrame(): Frame {
        return framesPool.pollLast() ?: Frame(WrappedMemoryArray(base))
    }

    private fun freeFrame(frame: Frame) {
        framesPool.offerLast(frame)
    }

    fun malloc(size: Long): Frame {
        val newOffset = baseOffset + size
        base.ensureCapacity(newOffset)
        val frame = newFrame()
        frameStack.offerLast(frame)
        frame.delegated.offset = baseOffset
        frame.delegated.length = size
        baseOffset = newOffset
        stackCount[stackCountPointer - 1]++
        return frame
    }

    fun calloc(size: Long): Frame {
        val frame = malloc(size)
        UNSAFE.setMemory(frame.address, size, 0)
        return frame
    }

    fun push(): MemoryStack {
        assert(stackCountPointer >= 0)
        stackCountPointer++
        return this
    }

    override fun close() {
        check(stackCountPointer > 0) { "Memory stack is empty: stackCountPointer=$stackCountPointer" }
        val count = stackCount[stackCountPointer - 1]
        repeat(count) {
            frameStack.peekLast().close()
        }
        stackCountPointer--
    }

    fun checkEmpty() {
        check(frameStack.isEmpty()) { "Memory stack is not empty" }
        check(stackCountPointer >= 0) { "Memory stack is not empty: stackCountPointer=$stackCountPointer" }
    }

    fun free() {
        base.free()
    }

    inner class Frame internal constructor(internal val delegated: WrappedMemoryArray) : MemoryArray by delegated,
        AutoCloseable {
        override fun close() {
            check(frameStack.pollLast() === this) { "Frame stack is corrupted" }
            stackCount[stackCountPointer - 1]--
            baseOffset -= length
            freeFrame(this)
        }
    }

    companion object {
        private val threadLocal = ThreadLocal.withInitial { MemoryStack(1024L * 1024L) }

        internal fun initAndGet(): MemoryStack {
            return synchronized(threadLocal) {
                val new = MemoryStack(1024L * 1024L)
                val prev = threadLocal.get()
                threadLocal.set(new)
                prev?.free()
                new
            }
        }

        fun get(): MemoryStack {
            return threadLocal.get()
        }

        inline operator fun <T> invoke(crossinline block: MemoryStack.() -> T): T {
            return get().push().use(block)
        }
    }
}