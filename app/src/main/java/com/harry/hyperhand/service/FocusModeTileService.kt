package com.harry.hyperhand.service

import android.content.Context
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.harry.hyperhand.hook.utils.shell.ShellUtils
import com.harry.hyperhand.hook.utils.prefs.PrefsUtils

class FocusModeTileService : TileService() {
    private val PREF_IS_FOCUS_MODE_ON = "prefs_key_is_focus_mode_on"

    private var isFocusModeOn: Boolean
        get() {
            val prefs = applicationContext.getSharedPreferences(PrefsUtils.mPrefsName, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_IS_FOCUS_MODE_ON, false)
        }
        set(value) {
            val prefs = applicationContext.getSharedPreferences(PrefsUtils.mPrefsName, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(PREF_IS_FOCUS_MODE_ON, value).apply()
        }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        isFocusModeOn = !isFocusModeOn
        updateTileState()
        applyFocusMode()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        tile.state = if (isFocusModeOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Focus/Hail"
        tile.updateTile()
    }

    private fun applyFocusMode() {
        val prefs = applicationContext.getSharedPreferences(PrefsUtils.mPrefsName, Context.MODE_PRIVATE)
        val selectedApps = prefs.getStringSet("prefs_key_focus_mode_apps", emptySet()) ?: emptySet()
        val isActive = isFocusModeOn

        if (selectedApps.isEmpty()) return

        Thread {
            for (pkg in selectedApps) {
                val cmd = if (isActive) {
                    "pm suspend $pkg"
                } else {
                    "pm unsuspend $pkg"
                }
                ShellUtils.rootExecCmd(cmd)
            }
        }.start()
    }
}
