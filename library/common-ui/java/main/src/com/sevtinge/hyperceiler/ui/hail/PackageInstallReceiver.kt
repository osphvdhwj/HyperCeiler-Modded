package com.sevtinge.hyperceiler.ui.hail

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat

class PackageInstallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
            val data = intent.data ?: return
            val pkg = data.schemeSpecificPart ?: return

            try {
                val pm = context.packageManager
                val appInfo = pm.getApplicationInfo(pkg, 0)
                
                // Check if it's a user app
                if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    showNotification(context, pkg, appName)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        } else if (intent.action == ACTION_ADD_APP) {
            val pkg = intent.getStringExtra(EXTRA_PKG) ?: return
            val appListManager = AppListManager(context)
            appListManager.addApp(pkg)
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(pkg.hashCode())
        }
    }

    private fun showNotification(context: Context, pkg: String, appName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "hail_new_app_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "New App Installation",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val addIntent = Intent(context, PackageInstallReceiver::class.java).apply {
            action = ACTION_ADD_APP
            putExtra(EXTRA_PKG, pkg)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            pkg.hashCode(),
            addIntent,
            flags
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle("New app installed")
            .setContentText("Add $appName to Force Stop list?")
            .addAction(android.R.drawable.ic_menu_add, "Add", pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(pkg.hashCode(), notification)
    }

    companion object {
        const val ACTION_ADD_APP = "com.sevtinge.hyperceiler.intent.action.ADD_APP"
        const val EXTRA_PKG = "extra_pkg"
    }
}
