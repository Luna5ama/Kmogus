package dev.luna5ama.kmogus

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue

internal object MemoryCleaner : Runnable {
    private val refQueue = ReferenceQueue<AddressHolder>()
    private var refHead: Reference? = null

    init {
        val thread = Thread(this, "Memory Cleaner")
        thread.isDaemon = true
        thread.start()
    }

    fun register(pointer: AddressHolder) {
        val ref = Reference(pointer)
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

    private class Reference(ref: AddressHolder) : PhantomReference<AddressHolder>(ref, refQueue) {
        private val container = ref.container

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
            if (prev === this && next === this) {
                return
            }
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