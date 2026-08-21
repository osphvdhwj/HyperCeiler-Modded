package com.sevtinge.hyperceiler.ui.hail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class UsageTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val targetPkg = intent.getStringExtra("target_pkg")
        
        if (targetPkg != null) {
            val prefs = context.getSharedPreferences("hail_apps_prefs", Context.MODE_PRIVATE)
            val expireTime = prefs.getLong("grant_$targetPkg", 0L)
            
            // Clear the grant
            prefs.edit().remove("grant_$targetPkg").apply()

            // Launch InterceptorActivity again
            val interceptorIntent = Intent(context, InterceptorActivity::class.java).apply {
                putExtra("target_pkg", targetPkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(interceptorIntent)
        }
    }
}
