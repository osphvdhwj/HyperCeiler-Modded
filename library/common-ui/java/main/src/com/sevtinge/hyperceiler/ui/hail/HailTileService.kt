package com.sevtinge.hyperceiler.ui.hail

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import java.io.IOException

class HailTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val appListManager = AppListManager(this)
        val apps = appListManager.getApps()

        for (pkg in apps) {
            if (pkg.isBlank()) continue
            try {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "am force-stop $pkg"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        
        val tile = qsTile
        if (tile != null) {
            tile.state = Tile.STATE_INACTIVE
            tile.updateTile()
        }
    }
}
