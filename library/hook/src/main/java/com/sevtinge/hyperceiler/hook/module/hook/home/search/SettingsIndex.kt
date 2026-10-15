package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import com.sevtinge.hyperceiler.hook.R

object SettingsIndex {

    private data class Entry(
        val label: String,
        val action: String,
        val iconRes: Int,
    )

    private val entries: List<Entry> = listOf(
        Entry("Wi-Fi", Settings.ACTION_WIFI_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Bluetooth", Settings.ACTION_BLUETOOTH_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Display", Settings.ACTION_DISPLAY_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Sound", Settings.ACTION_SOUND_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Battery", Intent.ACTION_POWER_USAGE_SUMMARY, R.drawable.ic_search_section_settings),
        Entry("Storage", Settings.ACTION_INTERNAL_STORAGE_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Apps", Settings.ACTION_APPLICATION_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Notifications", Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Location", Settings.ACTION_LOCATION_SOURCE_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Security", Settings.ACTION_SECURITY_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Privacy", Settings.ACTION_PRIVACY_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Accessibility", Settings.ACTION_ACCESSIBILITY_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Language & input", Settings.ACTION_LOCALE_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Date & time", Settings.ACTION_DATE_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Developer options", Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("About phone", Settings.ACTION_DEVICE_INFO_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Data usage", Settings.ACTION_DATA_USAGE_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Hotspot", Settings.ACTION_WIRELESS_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Network", Settings.ACTION_NETWORK_OPERATOR_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("VPN", Settings.ACTION_VPN_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Accounts", Settings.ACTION_SYNC_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("NFC", Settings.ACTION_NFC_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Gestures", Settings.ACTION_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Wallpaper", Settings.ACTION_SETTINGS, R.drawable.ic_search_section_settings),
        Entry("Home screen", Settings.ACTION_SETTINGS, R.drawable.ic_search_section_settings),
    )

    fun build(ctx: Context): List<SearchResult> {
        val pm = ctx.packageManager
        val out = ArrayList<SearchResult>(entries.size)
        val defaultIcon: Drawable? = runCatching {
            androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.ic_search_section_settings)
        }.getOrNull()
        for (e in entries) {
            val intent = Intent(e.action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (pm.resolveActivity(intent, 0) == null) continue
            val icon = runCatching {
                androidx.core.content.ContextCompat.getDrawable(ctx, e.iconRes)
            }.getOrNull() ?: defaultIcon
            out.add(
                SearchResult(
                    type = ResultType.SETTING,
                    label = e.label,
                    subtitle = "Settings",
                    icon = icon,
                    intent = intent,
                )
            )
        }
        return out
    }
}
