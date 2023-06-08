package dev.luna5ama.kmogus

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue

internal object MemoryCleaner : Runnable {
    private val refQueue = ReferenceQueue<PointerContainerImpl>()
    private var refHead: Reference? = null

    init {
        val thread = Thread(this, "Memory Cleaner")
        thread.isDaemon = true
        thread.start()
    }

    fun register(container: PointerContainerImpl) {
        val ref = Reference(container)
        synchronized(Reference.lock) {
            val head = refHead
            if (head != null) {
                head.prev = ref
                ref.next = head
            }
            refHead = ref
        }
    }

    override fun run() {
        while (true) {
            (refQueue.remove() as Reference).free()
        }
    }

    private class Reference(ref: PointerContainerImpl) : PhantomReference<PointerContainerImpl>(ref, refQueue) {
        private val container = ref.delegated

        @JvmField
        @Volatile
        var prev = this

        @JvmField
        @Volatile
        var next = this

        fun free() {
            tryRemove()
            container.free()
        }

        private fun tryRemove() {
            synchronized(lock) {
                if (prev === this && next === this) {
                    return
                }
                prev.next = next
                next.prev = prev
                prev = this
                next = this
            }
        }

        companion object {
            val lock = Any()
        }
    }
}