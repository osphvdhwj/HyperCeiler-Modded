package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.util.concurrent.atomic.AtomicLong

object QueryExecutor {

    private val thread = HandlerThread("hh-gs-query").apply { start() }
    private val bg = Handler(thread.looper)
    private val main = Handler(Looper.getMainLooper())
    private val seq = AtomicLong(0)

    fun <T> submit(work: () -> T, onResult: (T) -> Unit) {
        val token = seq.incrementAndGet()
        bg.post {
            val r = runCatching(work).getOrNull()
            if (seq.get() != token) return@post
            main.post { onResult(r as T) }
        }
    }

    fun invalidate() {
        seq.incrementAndGet()
    }
}
