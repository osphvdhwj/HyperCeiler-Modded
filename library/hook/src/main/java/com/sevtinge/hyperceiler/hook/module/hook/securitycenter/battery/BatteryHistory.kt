package com.sevtinge.hyperceiler.hook.module.hook.securitycenter.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.HandlerThread
import java.io.File

/**
 * Long-term battery sampler: every 5 minutes appends
 * timestamp,level,temperature,status,current to a CSV in the Security
 * app's own storage, keeping ~30 days. Read by the history chart.
 */
object BatteryHistory {

    data class Point(val t: Long, val level: Int, val temp: Int, val status: Int, val ua: Int)

    private const val MAX_AGE_MS = 30L * 24 * 60 * 60 * 1000
    private const val INTERVAL_MS = 5L * 60 * 1000
    private var started = false

    fun file(ctx: Context): File = File(ctx.filesDir, "hyperhand_battery_history.csv")

    fun start(ctx: Context) {
        if (started) return
        synchronized(this) {
            if (started) return
            val ht = HandlerThread("hh-batt-history")
            ht.start()
            Handler(ht.looper).post(object : Runnable {
                override fun run() {
                    runCatching { sample(ctx) }
                    Handler(ht.looper).postDelayed(this, INTERVAL_MS)
                }
            })
            started = true
        }
    }

    private fun sample(ctx: Context) {
        val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        if (level !in 0..100) return
        val temp = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?.getIntExtra("temperature", 0) ?: 0
        val status = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?.getIntExtra("status", 0) ?: 0
        val ua = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val f = file(ctx)
        val keep = System.currentTimeMillis() - MAX_AGE_MS
        val old = if (f.exists()) f.readLines().filter {
            it.split(',').firstOrNull()?.toLongOrNull()?.let { t -> t >= keep } ?: false
        } else emptyList()
        f.writeText((old + "${System.currentTimeMillis()},$level,$temp,$status,$ua").joinToString("\n"))
    }

    fun read(ctx: Context): List<Point> = runCatching {
        file(ctx).readLines().mapNotNull {
            val p = it.split(',')
            if (p.size < 5) return@mapNotNull null
            Point(
                p[0].toLongOrNull() ?: return@mapNotNull null,
                p[1].toIntOrNull() ?: return@mapNotNull null,
                p[2].toIntOrNull() ?: 0,
                p[3].toIntOrNull() ?: 0,
                p[4].toIntOrNull() ?: 0,
            )
        }
    }.getOrDefault(emptyList())
}
