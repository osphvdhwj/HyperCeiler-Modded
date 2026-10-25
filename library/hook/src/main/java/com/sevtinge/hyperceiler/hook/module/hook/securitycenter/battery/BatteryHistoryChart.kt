package com.sevtinge.hyperceiler.hook.module.hook.securitycenter.battery

import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XposedHelpers

/**
 * Adds a "Battery history chart" entry to the battery page
 * (ChargeProtectFragment). Long history: 24h / 7d / 30d ranges drawn
 * from the CSV the sampler keeps.
 */
object BatteryHistoryChart : BaseHook() {

    override fun init() {
        findAndHookMethod(
            "com.miui.powercenter.nightcharge.ChargeProtectFragment",
            "onCreatePreferences", Bundle::class.java, String::class.java,
            object : MethodHook() {
                override fun after(param: MethodHookParam) {
                    val frag = param.thisObject ?: return
                    val ctx = (runCatching { XposedHelpers.callMethod(frag, "requireContext") }
                        .getOrElse { XposedHelpers.callMethod(frag, "activity") }) as? Context ?: return
                    BatteryHistory.start(ctx)
                    val screen = XposedHelpers.callMethod(frag, "getPreferenceScreen") ?: return
                    val prefCls = XposedHelpers.findClass(
                        "miuix.preference.Preference", frag.javaClass.classLoader)
                    val pref = XposedHelpers.newInstance(prefCls, ctx)
                    XposedHelpers.callMethod(pref, "setKey", "hyperhand_battery_history")
                    XposedHelpers.callMethod(pref, "setTitle", "Battery history chart")
                    XposedHelpers.callMethod(pref, "setSummary", "Level & temperature over time (24h / 7d / 30d)")
                    // androidx.preference isn't on the module compile classpath —
                    // proxy the listener interface at runtime instead.
                    val listenerCls = XposedHelpers.findClass(
                        "androidx.preference.Preference\$OnPreferenceClickListener",
                        frag.javaClass.classLoader)
                    val listener = java.lang.reflect.Proxy.newProxyInstance(
                        listenerCls.classLoader, arrayOf(listenerCls)) { _, _, _ ->
                        showChart(ctx); true
                    }
                    XposedHelpers.callMethod(pref, "setOnPreferenceClickListener", listener)
                    XposedHelpers.callMethod(screen, "addPreference", pref)
                }
            })
    }

    private fun showChart(ctx: Context) {
        var rangeMs = 24L * 60 * 60 * 1000
        val chart = object : View(ctx) {
            override fun onDraw(c: Canvas) {
                val pts = BatteryHistory.read(ctx)
                    .filter { it.t >= System.currentTimeMillis() - rangeMs }
                drawChart(c, width.toFloat(), height.toFloat(), pts)
            }
        }
        val title = TextView(ctx).apply {
            text = "Battery history (24h)"
            setPadding(48, 40, 48, 8); textSize = 16f
        }
        val buttons = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; setPadding(48, 8, 48, 8) }
        fun mk(label: String, ms: Long): Button = Button(ctx).apply {
            text = label
            setOnClickListener {
                rangeMs = ms
                title.text = "Battery history ($label)"
                chart.invalidate()
            }
        }
        buttons.addView(mk("24h", 24L * 3600_000))
        buttons.addView(mk("7d", 7L * 24 * 3600_000))
        buttons.addView(mk("30d", 30L * 24 * 3600_000))

        chart.layoutParams = ViewGroup.LayoutParams(2000, 700)
        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            addView(title)
            addView(buttons)
            addView(HorizontalScrollView(ctx).apply { addView(chart) },
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 700))
        }
        AlertDialog.Builder(ctx)
            .setTitle("Battery history")
            .setView(box)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun drawChart(c: Canvas, w: Float, h: Float, pts: List<BatteryHistory.Point>) {
        c.drawColor(Color.argb(255, 18, 18, 20))
        val padL = 80f; val padB = 60f; val padT = 30f; val padR = 20f
        if (pts.size < 2) {
            c.drawText("Collecting samples… check back later", padL, h / 2, Paint().apply {
                color = Color.LTGRAY; textSize = 40f
            })
            return
        }
        val t0 = pts.first().t.toFloat(); val t1 = pts.last().t.toFloat()
        val span = (t1 - t0).coerceAtLeast(1f)
        fun x(t: Float) = padL + (t - t0) / span * (w - padL - padR)
        fun y(v: Float, max: Float) = padT + (1f - v / max) * (h - padT - padB)

        val grid = Paint().apply { color = Color.argb(80, 255, 255, 255); strokeWidth = 1f }
        val label = Paint().apply { color = Color.LTGRAY; textSize = 28f }
        for (i in 0..4) {
            val yy = padT + i * (h - padT - padB) / 4
            c.drawLine(padL, yy, w - padR, yy, grid)
            c.drawText("${100 - i * 25}%", 10f, yy + 10f, label)
        }
        val level = Paint().apply {
            color = Color.rgb(80, 200, 120); strokeWidth = 6f; style = Paint.Style.STROKE; isAntiAlias = true
        }
        val temp = Paint().apply {
            color = Color.rgb(255, 160, 60); strokeWidth = 5f; style = Paint.Style.STROKE; isAntiAlias = true
        }
        val lp = Path(); val tp = Path()
        var lFirst = true; var tFirst = true
        for (p in pts) {
            val px = x(p.t.toFloat())
            val py = y(p.level.toFloat(), 100f)
            if (lFirst) { lp.moveTo(px, py); lFirst = false } else lp.lineTo(px, py)
            val ty = y((p.temp / 10f - 20f).coerceIn(0f, 80f), 80f)
            if (tFirst) { tp.moveTo(px, ty); tFirst = false } else tp.lineTo(px, ty)
        }
        c.drawPath(lp, level)
        c.drawPath(tp, temp)
        c.drawText("— level %", padL, h - 28f, Paint().apply { color = level.color; textSize = 30f })
        c.drawText("— temp °C", padL + 320f, h - 28f, Paint().apply { color = temp.color; textSize = 30f })
    }
}
