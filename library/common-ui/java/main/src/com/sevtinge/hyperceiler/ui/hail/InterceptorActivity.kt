package com.sevtinge.hyperceiler.ui.hail

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.sevtinge.hyperceiler.ui.R

class InterceptorActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hail_interceptor)
        
        val targetPkg = intent.getStringExtra("target_pkg")
        val originalIntent = intent.getParcelableExtra<Intent>("original_intent")

        if (targetPkg == null) {
            finish()
            return
        }

        findViewById<Button>(R.id.btn_ok).setOnClickListener {
            forceStopAndFinish(targetPkg)
        }

        findViewById<Button>(R.id.btn_use_temp).setOnClickListener {
            showCustomTimeDialog(targetPkg, originalIntent)
        }
    }

    private fun showCustomTimeDialog(targetPkg: String, originalIntent: Intent?) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Minutes"
        }
        
        AlertDialog.Builder(this)
            .setTitle("Use for how many minutes?")
            .setView(input)
            .setPositiveButton("Start") { _, _ ->
                val minsStr = input.text.toString()
                if (minsStr.isNotEmpty()) {
                    val mins = minsStr.toLongOrNull() ?: 0L
                    if (mins > 0) {
                        grantTemporaryUsage(targetPkg, mins, originalIntent)
                    } else {
                        Toast.makeText(this, "Invalid time", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun grantTemporaryUsage(targetPkg: String, minutes: Long, originalIntent: Intent?) {
        val expireTime = System.currentTimeMillis() + (minutes * 60 * 1000)
        
        // Save the expire time using SharedPreferences
        val prefs = getSharedPreferences("hail_apps_prefs", MODE_PRIVATE)
        prefs.edit().putLong("grant_$targetPkg", expireTime).apply()

        // Schedule timer receiver
        val timerIntent = Intent(this, UsageTimerReceiver::class.java).apply {
            putExtra("target_pkg", targetPkg)
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(this, targetPkg.hashCode(), timerIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
        alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, expireTime, pendingIntent)

        // Launch the app
        if (originalIntent != null) {
            try {
                startActivity(originalIntent)
            } catch (e: Exception) {
                val launchIntent = packageManager.getLaunchIntentForPackage(targetPkg)
                if (launchIntent != null) {
                    startActivity(launchIntent)
                }
            }
        } else {
            val launchIntent = packageManager.getLaunchIntentForPackage(targetPkg)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        }
        finish()
    }

    override fun onBackPressed() {
        val targetPkg = intent.getStringExtra("target_pkg")
        if (targetPkg != null) {
            forceStopAndFinish(targetPkg)
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onUserLeaveHint() {
        // Equivalent to "remove app from recent or swipe back to close" 
        val targetPkg = intent.getStringExtra("target_pkg")
        if (targetPkg != null) {
            forceStopAndFinish(targetPkg)
        }
        super.onUserLeaveHint()
    }

    private fun forceStopAndFinish(pkg: String) {
        Thread {
            try {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "am force-stop $pkg"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
        finish()
    }
}
