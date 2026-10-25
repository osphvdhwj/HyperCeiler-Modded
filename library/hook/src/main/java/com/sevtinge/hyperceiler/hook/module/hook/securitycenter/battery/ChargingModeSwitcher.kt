package com.sevtinge.hyperceiler.hook.module.hook.securitycenter.battery

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookBeforeMethod
import com.sevtinge.hyperceiler.hook.utils.shell.ShellUtils

import de.robv.android.xposed.XposedBridge

/**
 * Shows a notification while charging with Fast/Slow actions that write
 * /sys/class/power_supply/battery/charge_control_limit (0 = no limit,
 * full charger speed; 14 = heavily limited). Runs inside the Security
 * app process; writes go through the root shell.
 */
object ChargingModeSwitcher : BaseHook() {

    private const val CHANNEL = "hyperhand_charging"
    private const val NOTIFY_ID = 0x4848
    private const val NODE = "/sys/class/power_supply/battery/charge_control_limit"
    private const val ACTION_SET = "hyperhand.action.charging.SET_MODE"
    private const val MODE_EXTRA = "mode"

    private val modes = linkedMapOf(
        "Fast" to "0",
        "Balanced" to "8",
        "Slow" to "14",
    )
    private var currentMode = "Fast"

    override fun init() {
        // Hooks run before the Application exists — grab the context from
        // Application.attach or the receiver never registers.
        Application::class.java.hookBeforeMethod("attach", Context::class.java) { param ->
            register(param.args[0] as Context)
        }
    }

    private fun register(ctx: Context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_POWER_CONNECTED -> notify(context, null)
                    Intent.ACTION_POWER_DISCONNECTED ->
                        notifyManager(context).cancel(NOTIFY_ID)
                    ACTION_SET -> {
                        val mode = intent.getStringExtra(MODE_EXTRA) ?: return
                        modes[mode]?.let { ShellUtils.rootExecCmd("echo $it > $NODE") }
                        currentMode = mode
                        notify(context, mode)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(ACTION_SET)
        }
        runCatching {
            ctx.registerReceiver(receiver, filter)
        }
    }

    private fun notifyManager(ctx: Context): NotificationManager =
        ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun notify(ctx: Context, activeMode: String?) {
        val nm = notifyManager(ctx)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Charging mode", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val mode = activeMode ?: currentMode
        val b = Notification.Builder(ctx, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Charging mode: $mode")
            .setContentText("Choose the charge speed for this session")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
        for ((label, _) in modes) {
            if (label == mode) continue
            val intent = Intent(ACTION_SET).setPackage(ctx.packageName).putExtra(MODE_EXTRA, label)
            b.addAction(
                Notification.Action.Builder(
                    null, "→ $label",
                    PendingIntent.getBroadcast(ctx, label.hashCode(), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                ).build()
            )
        }
        nm.notify(NOTIFY_ID, b.build())
    }
}
