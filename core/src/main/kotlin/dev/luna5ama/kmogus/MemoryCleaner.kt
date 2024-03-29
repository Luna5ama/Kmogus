package dev.luna5ama.kmogus

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue

internal object MemoryCleaner : Runnable {
    private val refQueue = ReferenceQueue<ArrImpl>()
    private var refHead: Reference? = null

    init {
        val thread = Thread(this, "Memory Cleaner")
        thread.isDaemon = true
        thread.start()
    }

    fun register(container: ArrImpl) {
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

    private class Reference(ref: ArrImpl) : PhantomReference<ArrImpl>(ref, refQueue) {
        private val container = ref.delegated

        @JvmField
        @Volatile
        var prev: Reference? = null

        @JvmField
        @Volatile
        var next: Reference? = null

        fun free() {
            tryRemove()
            container.free()
        }

        private fun tryRemove() {
            synchronized(lock) {
                val thisNext = this.next
                val thisPrev = prev
                if (thisPrev === null && thisNext === null) {
                    return
                }
                thisPrev?.next = thisNext
                thisNext?.prev = thisPrev
                prev = null
                this.next = null
            }
        }

        companion object {
            val lock = Any()
        }
    }
}