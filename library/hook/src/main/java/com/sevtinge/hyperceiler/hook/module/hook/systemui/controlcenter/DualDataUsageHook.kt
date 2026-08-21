/*
 * This file is part of HyperCeiler.
 *
 * HyperCeiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023-2025 HyperCeiler Contributions
 */
package com.sevtinge.hyperceiler.hook.module.hook.systemui.controlcenter

import android.app.usage.NetworkStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.api.Dependency
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logE
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefType
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefsChangeObserver
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefsUtils
import de.robv.android.xposed.XposedHelpers
import java.lang.ref.WeakReference
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DualDataUsageHelper {

    fun formatFileSize(bytes: Long): String {
        var b = bytes
        if (b < 0L) b = 0L
        if (b < 1024L) {
            return "$b B"
        }
        var exp = (Math.log(b.toDouble()) / Math.log(1024.0)).toInt()
        exp = Math.min(exp, 5) // up to PB
        val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
        val `val` = b.toDouble() / Math.pow(1024.0, exp.toDouble())
        return String.format(Locale.US, "%.2f %s", `val`, units[exp])
    }

    fun formatDualDataUsageText(context: Context): String {
        val (mobileBytes, wifiBytes) = queryTodayDataUsage(context)
        return formatDualDataText(mobileBytes, wifiBytes)
    }

    fun formatDualDataText(mobileBytes: Long, wifiBytes: Long): String {
        val mobileStr = formatFileSize(mobileBytes)
        val wifiStr = formatFileSize(wifiBytes)
        return "Mobile: $mobileStr | WiFi: $wifiStr"
    }

    fun calculateStartOfDayEpoch(currentTimestampMillis: Long, tz: TimeZone = TimeZone.getDefault()): Long {
        val calendar = Calendar.getInstance(tz).apply {
            timeInMillis = currentTimestampMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun queryTodayDataUsage(context: Context): Pair<Long, Long> {
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
            ?: return queryFallbackTrafficStats()

        val startTime = calculateStartOfDayEpoch(System.currentTimeMillis())
        val endTime = System.currentTimeMillis()

        var mobileBytes = 0L
        var wifiBytes = 0L

        // 1. Query Mobile Data (Cellular)
        try {
            val bucketMobile = nsm.querySummaryForDevice(
                ConnectivityManager.TYPE_MOBILE,
                null,
                startTime,
                endTime
            )
            mobileBytes = (bucketMobile.rxBytes + bucketMobile.txBytes).coerceAtLeast(0L)
        } catch (t: Throwable) {
            mobileBytes = (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()).coerceAtLeast(0L)
        }

        // 2. Query Wi-Fi Data
        try {
            val bucketWifi = nsm.querySummaryForDevice(
                ConnectivityManager.TYPE_WIFI,
                null,
                startTime,
                endTime
            )
            wifiBytes = (bucketWifi.rxBytes + bucketWifi.txBytes).coerceAtLeast(0L)
        } catch (t: Throwable) {
            val totalBytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()
            val mobileTotal = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()
            wifiBytes = (totalBytes - mobileTotal).coerceAtLeast(0L)
        }

        return Pair(mobileBytes, wifiBytes)
    }

    fun queryFallbackTrafficStats(): Pair<Long, Long> {
        val mobile = (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()).coerceAtLeast(0L)
        val total = (TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()).coerceAtLeast(0L)
        val wifi = (total - mobile).coerceAtLeast(0L)
        return Pair(mobile, wifi)
    }

    fun launchSecurityDataUsageActivity(context: Context) {
        val intent = Intent().apply {
            component = ComponentName(
                "com.miui.securitycenter",
                "com.miui.networkassistant.ui.activity.MainActivity"
            )
            action = "miui.intent.action.NETWORKASSISTANT_MAIN"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            val starterClass = runCatching { XposedHelpers.findClass("com.android.systemui.plugins.ActivityStarter", context.classLoader) }.getOrNull()
            val activityStarter = if (starterClass != null) Dependency.get(starterClass) else Dependency.getDependencyInner("com.android.systemui.plugins.ActivityStarter")
            if (activityStarter != null) {
                XposedHelpers.callMethod(activityStarter, "postStartActivityDismissingKeyguard", intent, 0)
                return
            }
        } catch (ignored: Throwable) {}

        try {
            context.startActivity(intent)
        } catch (e: Throwable) {
            try {
                val fallback = Intent("miui.intent.action.NETWORKASSISTANT_MAIN").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            } catch (t: Throwable) {
                logE("DualDataUsageHook", "Failed to launch security network assistant: $t")
            }
        }
    }
}

class DualDataUsageHook : BaseHook() {

    private var observerRegistered = false
    private val activeViews = mutableListOf<WeakReference<TextView>>()

    private val isDualDataEnabled: Boolean
        get() = PrefsUtils.mPrefsMap.getBoolean("system_ui_control_center_dual_data_usage", false)

    override fun init() {
        val updateTextHook = object : MethodHook() {
            override fun after(param: MethodHookParam) {
                if (!isDualDataEnabled) return

                val viewObj = runCatching {
                    XposedHelpers.getObjectField(param.thisObject, "carrierText")
                }.getOrElse {
                    runCatching {
                        XposedHelpers.getObjectField(param.thisObject, "mCarrierText")
                    }.getOrElse {
                        runCatching {
                            XposedHelpers.getObjectField(param.thisObject, "dataUsage")
                        }.getOrElse {
                            runCatching {
                                XposedHelpers.getObjectField(param.thisObject, "mDataUsage")
                            }.getOrNull()
                        }
                    }
                }

                if (viewObj is TextView) {
                    val context = viewObj.context
                    registerObserverIfNeeded(context)
                    trackTextView(viewObj)
                    applyDualDataText(viewObj)
                }
            }
        }

        // Hook ControlCenterStatusBar
        hookAllMethodsSilently("com.android.systemui.controlcenter.phone.widget.ControlCenterStatusBar", "updateFlaresInfo", updateTextHook)
        findAndHookMethodSilently("com.android.systemui.controlcenter.phone.widget.ControlCenterStatusBar", "onFinishInflate", updateTextHook)

        // Hook MiuiQSHeaderView
        findAndHookMethodSilently("com.android.systemui.qs.MiuiQSHeaderView", "updateCarrierVisibility", updateTextHook)
        findAndHookMethodSilently("com.android.systemui.qs.MiuiQSHeaderView", "onFinishInflate", updateTextHook)

        // Hook MiuiNotificationHeaderView
        findAndHookMethodSilently("com.android.systemui.qs.MiuiNotificationHeaderView", "updateCarrierTextVisibility", updateTextHook)
        findAndHookMethodSilently("com.android.systemui.qs.MiuiNotificationHeaderView", "onFinishInflate", updateTextHook)
    }

    private fun trackTextView(textView: TextView) {
        synchronized(activeViews) {
            activeViews.removeAll { it.get() == null }
            if (activeViews.none { it.get() === textView }) {
                activeViews.add(WeakReference(textView))
            }
        }
    }

    private fun applyDualDataText(textView: TextView) {
        val dualText = DualDataUsageHelper.formatDualDataUsageText(textView.context)
        textView.text = dualText
        textView.isLongClickable = true
        textView.setOnLongClickListener { v ->
            DualDataUsageHelper.launchSecurityDataUsageActivity(v.context)
            true
        }
    }

    private fun registerObserverIfNeeded(context: Context) {
        if (observerRegistered) return
        observerRegistered = true
        val handler = Handler(Looper.getMainLooper())
        val key = "prefs_key_system_ui_control_center_dual_data_usage"

        runCatching {
            object : PrefsChangeObserver(context, handler, true, PrefType.Boolean, key, false) {
                override fun onChange(type: PrefType?, uri: Uri?, name: String?, def: Any?) {
                    synchronized(activeViews) {
                        activeViews.removeAll { it.get() == null }
                        for (ref in activeViews) {
                            ref.get()?.let { tv ->
                                tv.post {
                                    if (isDualDataEnabled) {
                                        applyDualDataText(tv)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
