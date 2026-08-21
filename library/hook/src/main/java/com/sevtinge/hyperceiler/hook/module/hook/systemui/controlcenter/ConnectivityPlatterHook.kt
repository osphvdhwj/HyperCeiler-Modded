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

import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logE
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logI
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefType
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefsChangeObserver
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefsUtils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.ref.WeakReference
import java.util.Collections
import java.util.WeakHashMap

/**
 * Requirement R4: iOS-Style Connectivity Platter
 * Groups WiFi, Bluetooth, Mobile Data, and Airplane Mode into a 2x2 compact card platter.
 * Long-pressing expands into a modal dialog with 6 connectivity toggles and quick access to settings.
 */
object ConnectivityHostHelper {

    enum class TileState(val code: Int) {
        UNAVAILABLE(0), // Disabled / No hardware / No SIM
        INACTIVE(1),    // Enabled but disconnected / Idle / Off
        ACTIVE(2),      // Enabled & Connected / Transmitting / On
        TRANSIENT(3)    // Turning On / Off
    }

    data class SubTileInfo(
        val spec: String,
        val title: String,
        var subtitle: String,
        var state: TileState,
        val isExpandedItem: Boolean
    )

    val COMPACT_SPECS = listOf("wifi", "bluetooth", "cell", "airplane")
    val EXPANDED_SPECS = listOf("wifi", "bluetooth", "cell", "airplane", "hotspot", "mishare")

    fun createCompactPlatter(
        wifiOn: Boolean, wifiSsid: String?,
        btOn: Boolean, btDevice: String?,
        cellOn: Boolean, carrier: String?, hasSim: Boolean,
        airplaneOn: Boolean
    ): List<SubTileInfo> {
        val list = ArrayList<SubTileInfo>(4)

        // 1. Wi-Fi
        val wifiState = if (!wifiOn) TileState.INACTIVE else if (wifiSsid != null) TileState.ACTIVE else TileState.INACTIVE
        val wifiSub = if (!wifiOn) "Off" else (wifiSsid ?: "Not Connected")
        list.add(SubTileInfo("wifi", "Wi-Fi", wifiSub, wifiState, false))

        // 2. Bluetooth
        val btState = if (!btOn) TileState.INACTIVE else if (btDevice != null) TileState.ACTIVE else TileState.INACTIVE
        val btSub = if (!btOn) "Off" else (btDevice ?: "On")
        list.add(SubTileInfo("bluetooth", "Bluetooth", btSub, btState, false))

        // 3. Cellular Data
        val cellState: TileState
        val cellSub: String
        if (!hasSim) {
            cellState = TileState.UNAVAILABLE
            cellSub = "No SIM"
        } else if (!cellOn) {
            cellState = TileState.INACTIVE
            cellSub = "Off"
        } else {
            cellState = TileState.ACTIVE
            cellSub = carrier ?: "5G"
        }
        list.add(SubTileInfo("cell", "Mobile Data", cellSub, cellState, false))

        // 4. Airplane Mode
        val airState = if (airplaneOn) TileState.ACTIVE else TileState.INACTIVE
        val airSub = if (airplaneOn) "On" else "Off"
        list.add(SubTileInfo("airplane", "Airplane Mode", airSub, airState, false))

        return list
    }

    fun createExpandedDialogItems(
        compactItems: List<SubTileInfo>,
        hotspotOn: Boolean, hotspotClients: Int,
        miShareOn: Boolean
    ): List<SubTileInfo> {
        val expanded = ArrayList<SubTileInfo>(compactItems)

        // 5. Personal Hotspot
        val hotspotState = if (hotspotOn) TileState.ACTIVE else TileState.INACTIVE
        val hotspotSub = if (hotspotOn) "$hotspotClients Connected" else "Off"
        expanded.add(SubTileInfo("hotspot", "Personal Hotspot", hotspotSub, hotspotState, true))

        // 6. Mi Share / AirDrop
        val miShareState = if (miShareOn) TileState.ACTIVE else TileState.INACTIVE
        val miShareSub = if (miShareOn) "Everyone" else "Off"
        expanded.add(SubTileInfo("mishare", "Mi Share", miShareSub, miShareState, true))

        return expanded
    }

    @SuppressLint("MissingPermission")
    fun querySubTileState(context: Context, spec: String): SubTileInfo {
        return when (spec) {
            "wifi" -> queryWifiState(context)
            "bluetooth", "bt" -> queryBluetoothState(context)
            "cell" -> queryCellularState(context)
            "airplane" -> queryAirplaneState(context)
            "hotspot" -> queryHotspotState(context)
            "mishare" -> queryMiShareState(context)
            else -> SubTileInfo(spec, spec.replaceFirstChar { it.uppercase() }, "Off", TileState.INACTIVE, false)
        }
    }

    @SuppressLint("MissingPermission")
    private fun queryWifiState(context: Context): SubTileInfo {
        var wifiOn = false
        var ssid: String? = null
        runCatching {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiOn = wifiManager?.isWifiEnabled == true
            if (wifiOn) {
                val info = wifiManager?.connectionInfo
                val rawSsid = info?.ssid
                if (rawSsid != null && rawSsid != "<unknown ssid>" && rawSsid.isNotEmpty()) {
                    ssid = rawSsid.trim('"')
                } else {
                    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    val activeNet = cm?.activeNetwork
                    val caps = cm?.getNetworkCapabilities(activeNet)
                    if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                        ssid = "Connected"
                    }
                }
            }
        }
        val state = if (!wifiOn) TileState.INACTIVE else if (ssid != null) TileState.ACTIVE else TileState.INACTIVE
        val subtitle = if (!wifiOn) "Off" else (ssid ?: "Not Connected")
        return SubTileInfo("wifi", "Wi-Fi", subtitle, state, false)
    }

    @SuppressLint("MissingPermission")
    private fun queryBluetoothState(context: Context): SubTileInfo {
        var btOn = false
        var deviceName: String? = null
        runCatching {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            btOn = adapter?.isEnabled == true
            if (btOn) {
                val bonded = adapter?.bondedDevices
                if (bonded != null) {
                    for (dev in bonded) {
                        val stateA2dp = adapter.getProfileConnectionState(BluetoothProfile.A2DP)
                        val stateHeadset = adapter.getProfileConnectionState(BluetoothProfile.HEADSET)
                        if (stateA2dp == BluetoothProfile.STATE_CONNECTED || stateHeadset == BluetoothProfile.STATE_CONNECTED) {
                            deviceName = dev.name ?: "Connected"
                            break
                        }
                    }
                }
            }
        }
        val state = if (!btOn) TileState.INACTIVE else if (deviceName != null) TileState.ACTIVE else TileState.INACTIVE
        val subtitle = if (!btOn) "Off" else (deviceName ?: "On")
        return SubTileInfo("bluetooth", "Bluetooth", subtitle, state, false)
    }

    private fun queryCellularState(context: Context): SubTileInfo {
        var hasSim = false
        var cellOn = false
        var carrier: String? = null
        runCatching {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            hasSim = tm?.simState == TelephonyManager.SIM_STATE_READY
            if (hasSim) {
                cellOn = tm?.isDataEnabled == true
                carrier = tm?.networkOperatorName?.takeIf { it.isNotBlank() } ?: "5G"
            }
        }
        val state = if (!hasSim) TileState.UNAVAILABLE else if (!cellOn) TileState.INACTIVE else TileState.ACTIVE
        val subtitle = if (!hasSim) "No SIM" else if (!cellOn) "Off" else (carrier ?: "5G")
        return SubTileInfo("cell", "Mobile Data", subtitle, state, false)
    }

    private fun queryAirplaneState(context: Context): SubTileInfo {
        val airplaneOn = runCatching {
            Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
        }.getOrDefault(false)
        val state = if (airplaneOn) TileState.ACTIVE else TileState.INACTIVE
        val subtitle = if (airplaneOn) "On" else "Off"
        return SubTileInfo("airplane", "Airplane Mode", subtitle, state, false)
    }

    private fun queryHotspotState(context: Context): SubTileInfo {
        var hotspotOn = false
        runCatching {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (wifiManager != null) {
                val isApEnabled = XposedHelpers.callMethod(wifiManager, "isWifiApEnabled") as? Boolean
                hotspotOn = isApEnabled == true
            }
        }
        val state = if (hotspotOn) TileState.ACTIVE else TileState.INACTIVE
        val subtitle = if (hotspotOn) "1 Connected" else "Off"
        return SubTileInfo("hotspot", "Personal Hotspot", subtitle, state, true)
    }

    private fun queryMiShareState(context: Context): SubTileInfo {
        val miShareOn = runCatching {
            Settings.System.getInt(context.contentResolver, "mishare_state", 0) != 0
        }.getOrDefault(false)
        val state = if (miShareOn) TileState.ACTIVE else TileState.INACTIVE
        val subtitle = if (miShareOn) "Everyone" else "Off"
        return SubTileInfo("mishare", "Mi Share", subtitle, state, true)
    }

    @SuppressLint("MissingPermission")
    fun toggleTile(context: Context, spec: String) {
        runCatching {
            when (spec) {
                "wifi" -> {
                    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                    wifiManager?.let { wm ->
                        val target = !wm.isWifiEnabled
                        runCatching { XposedHelpers.callMethod(wm, "setWifiEnabled", target) }
                    }
                }
                "bluetooth", "bt" -> {
                    val adapter = BluetoothAdapter.getDefaultAdapter()
                    if (adapter != null) {
                        if (adapter.isEnabled) adapter.disable() else adapter.enable()
                    }
                }
                "cell" -> {
                    val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                    tm?.let {
                        val target = !it.isDataEnabled
                        it.isDataEnabled = target
                    }
                }
                "airplane" -> {
                    val current = Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
                    val target = if (current) 0 else 1
                    Settings.Global.putInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, target)
                    val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                        putExtra("state", target == 1)
                    }
                    context.sendBroadcast(intent)
                }
                "hotspot" -> {
                    launchSettingsSubpage(context, "hotspot")
                }
                "mishare" -> {
                    val current = Settings.System.getInt(context.contentResolver, "mishare_state", 0) != 0
                    val target = if (current) 0 else 1
                    Settings.System.putInt(context.contentResolver, "mishare_state", target)
                }
            }
        }.onFailure {
            logE("ConnectivityHostHelper", "Failed to toggle tile $spec: $it")
        }
    }

    fun launchSettingsSubpage(context: Context, spec: String) {
        val action = when (spec) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth", "bt" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "cell" -> Settings.ACTION_DATA_ROAMING_SETTINGS
            "airplane" -> Settings.ACTION_AIRPLANE_MODE_SETTINGS
            "hotspot" -> Settings.ACTION_WIRELESS_SETTINGS
            "mishare" -> "com.miui.mishare.action.MI_SHARE_SETTINGS"
            else -> Settings.ACTION_SETTINGS
        }

        runCatching {
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }.onFailure {
            runCatching {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            }
        }
    }
}

/**
 * Custom vector icon painter for standalone rendering of connectivity icons
 */
class ConnectivityIconDrawable(
    private val spec: String,
    private var iconColor: Int = Color.WHITE
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun setIconColor(color: Int) {
        if (iconColor != color) {
            iconColor = color
            invalidateSelf()
        }
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val size = Math.min(bounds.width(), bounds.height()).toFloat()
        if (size <= 0f) return

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val scale = size / 48f

        paint.color = iconColor
        paint.strokeWidth = 3f * scale
        fillPaint.color = iconColor

        canvas.save()
        when (spec) {
            "wifi" -> drawWifi(canvas, cx, cy, scale)
            "bluetooth", "bt" -> drawBluetooth(canvas, cx, cy, scale)
            "cell" -> drawCellular(canvas, cx, cy, scale)
            "airplane" -> drawAirplane(canvas, cx, cy, scale)
            "hotspot" -> drawHotspot(canvas, cx, cy, scale)
            "mishare" -> drawMiShare(canvas, cx, cy, scale)
            else -> drawGeneric(canvas, cx, cy, scale)
        }
        canvas.restore()
    }

    private fun drawWifi(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        // Dot at bottom
        canvas.drawCircle(cx, cy + 12f * s, 2.5f * s, fillPaint)
        // 3 Arcs
        val rect1 = RectF(cx - 8f * s, cy + 2f * s, cx + 8f * s, cy + 18f * s)
        canvas.drawArc(rect1, 215f, 110f, false, paint)
        val rect2 = RectF(cx - 15f * s, cy - 5f * s, cx + 15f * s, cy + 25f * s)
        canvas.drawArc(rect2, 215f, 110f, false, paint)
        val rect3 = RectF(cx - 22f * s, cy - 12f * s, cx + 22f * s, cy + 32f * s)
        canvas.drawArc(rect3, 215f, 110f, false, paint)
    }

    private fun drawBluetooth(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        val path = Path()
        path.moveTo(cx, cy - 16f * s)
        path.lineTo(cx, cy + 16f * s)
        path.lineTo(cx + 9f * s, cy + 8f * s)
        path.lineTo(cx - 8f * s, cy - 6f * s)
        path.moveTo(cx - 8f * s, cy + 6f * s)
        path.lineTo(cx + 9f * s, cy - 8f * s)
        path.lineTo(cx, cy - 16f * s)
        canvas.drawPath(path, paint)
    }

    private fun drawCellular(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        // 4 Signal Bars
        val barWidth = 3.5f * s
        val spacing = 5.5f * s
        val startX = cx - 1.5f * spacing
        val baseY = cy + 12f * s

        for (i in 0..3) {
            val h = (6f + i * 6f) * s
            val x = startX + i * spacing
            val r = RectF(x - barWidth / 2, baseY - h, x + barWidth / 2, baseY)
            canvas.drawRoundRect(r, 1.5f * s, 1.5f * s, fillPaint)
        }
    }

    private fun drawAirplane(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        val path = Path()
        path.moveTo(cx, cy - 16f * s) // nose
        path.lineTo(cx + 3f * s, cy - 6f * s)
        path.lineTo(cx + 17f * s, cy + 1f * s) // right wing
        path.lineTo(cx + 17f * s, cy + 4f * s)
        path.lineTo(cx + 3f * s, cy + 3f * s)
        path.lineTo(cx + 3f * s, cy + 10f * s)
        path.lineTo(cx + 8f * s, cy + 14f * s) // right tail
        path.lineTo(cx + 8f * s, cy + 16f * s)
        path.lineTo(cx, cy + 14f * s) // center tail
        path.lineTo(cx - 8f * s, cy + 16f * s) // left tail
        path.lineTo(cx - 8f * s, cy + 14f * s)
        path.lineTo(cx - 3f * s, cy + 10f * s)
        path.lineTo(cx - 3f * s, cy + 3f * s)
        path.lineTo(cx - 17f * s, cy + 4f * s) // left wing
        path.lineTo(cx - 17f * s, cy + 1f * s)
        path.lineTo(cx - 3f * s, cy - 6f * s)
        path.close()
        canvas.drawPath(path, fillPaint)
    }

    private fun drawHotspot(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawCircle(cx, cy, 3f * s, fillPaint)
        val r1 = RectF(cx - 9f * s, cy - 9f * s, cx + 9f * s, cy + 9f * s)
        canvas.drawArc(r1, 45f, 90f, false, paint)
        canvas.drawArc(r1, 225f, 90f, false, paint)
        val r2 = RectF(cx - 17f * s, cy - 17f * s, cx + 17f * s, cy + 17f * s)
        canvas.drawArc(r2, 45f, 90f, false, paint)
        canvas.drawArc(r2, 225f, 90f, false, paint)
    }

    private fun drawMiShare(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        // Node 1 (center)
        canvas.drawCircle(cx - 7f * s, cy, 4f * s, fillPaint)
        // Node 2 (top right)
        canvas.drawCircle(cx + 7f * s, cy - 8f * s, 4f * s, fillPaint)
        // Node 3 (bottom right)
        canvas.drawCircle(cx + 7f * s, cy + 8f * s, 4f * s, fillPaint)

        // Connecting lines
        canvas.drawLine(cx - 7f * s, cy, cx + 7f * s, cy - 8f * s, paint)
        canvas.drawLine(cx - 7f * s, cy, cx + 7f * s, cy + 8f * s, paint)
    }

    private fun drawGeneric(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawCircle(cx, cy, 8f * s, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        fillPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        fillPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

/**
 * 2x2 Compact ViewGroup Platter for Control Center
 */
class ConnectivityPlatterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val buttonViews = HashMap<String, View>()
    private val iconDrawables = HashMap<String, ConnectivityIconDrawable>()
    private val subTileInfoMap = HashMap<String, ConnectivityHostHelper.SubTileInfo>()
    private var isBound = false

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val density = context.resources.displayMetrics.density

        // Acrylic container styling
        val bg = GradientDrawable().apply {
            setColor(Color.parseColor("#33222222"))
            cornerRadius = 24f * density
            setStroke((1f * density).toInt(), Color.parseColor("#22FFFFFF"))
        }
        background = bg

        val pad = (12 * density).toInt()
        setPadding(pad, pad, pad, pad)

        buildPlatterLayout(density)

        isLongClickable = true
        isClickable = true

        setOnLongClickListener {
            showExpandDialog()
            true
        }
    }

    private fun buildPlatterLayout(density: Float) {
        removeAllViews()
        buttonViews.clear()
        iconDrawables.clear()

        // 2 Rows of 2 Buttons
        val row1 = createRowLayout(density)
        val row2 = createRowLayout(density)

        val btnAirplane = createTileButton("airplane", density)
        val btnCell = createTileButton("cell", density)
        val btnWifi = createTileButton("wifi", density)
        val btnBt = createTileButton("bluetooth", density)

        row1.addView(btnAirplane)
        row1.addView(btnCell)
        row2.addView(btnWifi)
        row2.addView(btnBt)

        addView(row1)
        addView(row2)

        refreshAllStates()
    }

    private fun createRowLayout(density: Float): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
        }
    }

    private fun createTileButton(spec: String, density: Float): View {
        val container = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f).apply {
                val m = (4 * density).toInt()
                setMargins(m, m, m, m)
            }
        }

        val iconDrawable = ConnectivityIconDrawable(spec, Color.WHITE)
        iconDrawables[spec] = iconDrawable

        val iconView = ImageView(context).apply {
            val size = (48 * density).toInt()
            layoutParams = LayoutParams(size, size).apply {
                gravity = Gravity.CENTER
            }
            setImageDrawable(iconDrawable)
            val pad = (10 * density).toInt()
            setPadding(pad, pad, pad, pad)

            val circleBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#44888888"))
            }
            background = circleBg
        }

        container.addView(iconView)
        buttonViews[spec] = iconView

        container.setOnClickListener {
            ConnectivityHostHelper.toggleTile(context, spec)
            postDelayed({ refreshSubTile(spec) }, 250)
            postDelayed({ refreshSubTile(spec) }, 800)
        }

        container.setOnLongClickListener {
            showExpandDialog()
            true
        }

        return container
    }

    fun refreshAllStates() {
        for (spec in ConnectivityHostHelper.COMPACT_SPECS) {
            refreshSubTile(spec)
        }
    }

    fun refreshSubTile(spec: String) {
        val info = ConnectivityHostHelper.querySubTileState(context, spec)
        updateSubTile(info.spec, info.state, info.subtitle)
    }

    fun updateSubTile(spec: String, state: ConnectivityHostHelper.TileState, subtitle: String) {
        val targetSpec = if (spec == "bt") "bluetooth" else spec
        subTileInfoMap[targetSpec] = ConnectivityHostHelper.SubTileInfo(targetSpec, targetSpec, subtitle, state, false)

        val btn = buttonViews[targetSpec] ?: return
        val icon = iconDrawables[targetSpec]

        post {
            val bg = btn.background as? GradientDrawable
            when (state) {
                ConnectivityHostHelper.TileState.ACTIVE -> {
                    bg?.setColor(Color.parseColor("#FF007AFF")) // iOS Blue
                    icon?.setIconColor(Color.WHITE)
                }
                ConnectivityHostHelper.TileState.INACTIVE -> {
                    bg?.setColor(Color.parseColor("#44888888")) // Semi-translucent Gray
                    icon?.setIconColor(Color.WHITE)
                }
                ConnectivityHostHelper.TileState.UNAVAILABLE -> {
                    bg?.setColor(Color.parseColor("#22555555")) // Dimmed
                    icon?.setIconColor(Color.parseColor("#88FFFFFF"))
                }
                ConnectivityHostHelper.TileState.TRANSIENT -> {
                    bg?.setColor(Color.parseColor("#88007AFF"))
                    icon?.setIconColor(Color.WHITE)
                }
            }
            btn.invalidate()
        }
    }

    fun showExpandDialog() {
        ConnectivityExpandDialog(context).show()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        refreshAllStates()
    }
}

/**
 * Expandable Modal Dialog showing 6 Connectivity Controls
 */
class ConnectivityExpandDialog(context: Context) : Dialog(context) {

    private val expandedViews = HashMap<String, View>()
    private val subTitleViews = HashMap<String, TextView>()
    private val iconDrawables = HashMap<String, ConnectivityIconDrawable>()
    private val density = context.resources.displayMetrics.density

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)

        val rootCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#EE1C1C1E")) // Frosted dark acrylic
                cornerRadius = 32f * density
                setStroke((1f * density).toInt(), Color.parseColor("#33FFFFFF"))
            }
            background = bg
            val pad = (20 * density).toInt()
            setPadding(pad, pad, pad, pad)
            layoutParams = ViewGroup.LayoutParams(
                (360 * density).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val grid = GridLayout(context).apply {
            columnCount = 2
            rowCount = 3
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        for (spec in ConnectivityHostHelper.EXPANDED_SPECS) {
            val itemView = createExpandedTileView(spec)
            grid.addView(itemView)
        }

        rootCard.addView(grid)
        setContentView(rootCard)

        refreshAllItems()
    }

    private fun createExpandedTileView(spec: String): View {
        val itemContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val padH = (12 * density).toInt()
            val padV = (10 * density).toInt()
            setPadding(padH, padV, padH, padV)

            val lp = GridLayout.LayoutParams().apply {
                width = 0
                height = (72 * density).toInt()
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                val m = (6 * density).toInt()
                setMargins(m, m, m, m)
            }
            layoutParams = lp

            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#26FFFFFF"))
                cornerRadius = 18f * density
            }
            background = bg
        }

        // Circular Icon
        val iconDrawable = ConnectivityIconDrawable(spec, Color.WHITE)
        iconDrawables[spec] = iconDrawable

        val iconView = ImageView(context).apply {
            val size = (40 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            setImageDrawable(iconDrawable)
            val p = (8 * density).toInt()
            setPadding(p, p, p, p)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#44888888"))
            }
        }
        expandedViews[spec] = iconView

        // Text Column
        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            val padL = (10 * density).toInt()
            setPadding(padL, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
        }

        val titleView = TextView(context).apply {
            text = when (spec) {
                "wifi" -> "Wi-Fi"
                "bluetooth" -> "Bluetooth"
                "cell" -> "Mobile Data"
                "airplane" -> "Airplane Mode"
                "hotspot" -> "Personal Hotspot"
                "mishare" -> "Mi Share"
                else -> spec.replaceFirstChar { it.uppercase() }
            }
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            paint.isFakeBoldText = true
            isSingleLine = true
        }

        val subView = TextView(context).apply {
            text = "Off"
            setTextColor(Color.parseColor("#B0FFFFFF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f)
            isSingleLine = true
        }
        subTitleViews[spec] = subView

        textLayout.addView(titleView)
        textLayout.addView(subView)

        itemContainer.addView(iconView)
        itemContainer.addView(textLayout)

        // Actions
        itemContainer.setOnClickListener {
            ConnectivityHostHelper.toggleTile(context, spec)
            itemContainer.postDelayed({ refreshItem(spec) }, 250)
            itemContainer.postDelayed({ refreshItem(spec) }, 800)
        }

        itemContainer.setOnLongClickListener {
            ConnectivityHostHelper.launchSettingsSubpage(context, spec)
            dismiss()
            true
        }

        return itemContainer
    }

    fun refreshAllItems() {
        for (spec in ConnectivityHostHelper.EXPANDED_SPECS) {
            refreshItem(spec)
        }
    }

    fun refreshItem(spec: String) {
        val info = ConnectivityHostHelper.querySubTileState(context, spec)
        val iconView = expandedViews[spec] ?: return
        val subView = subTitleViews[spec] ?: return
        val icon = iconDrawables[spec]

        subView.post {
            subView.text = info.subtitle
            val bg = iconView.background as? GradientDrawable
            when (info.state) {
                ConnectivityHostHelper.TileState.ACTIVE -> {
                    bg?.setColor(Color.parseColor("#FF007AFF"))
                    icon?.setIconColor(Color.WHITE)
                }
                ConnectivityHostHelper.TileState.INACTIVE -> {
                    bg?.setColor(Color.parseColor("#44888888"))
                    icon?.setIconColor(Color.WHITE)
                }
                ConnectivityHostHelper.TileState.UNAVAILABLE -> {
                    bg?.setColor(Color.parseColor("#22555555"))
                    icon?.setIconColor(Color.parseColor("#88FFFFFF"))
                }
                ConnectivityHostHelper.TileState.TRANSIENT -> {
                    bg?.setColor(Color.parseColor("#88007AFF"))
                    icon?.setIconColor(Color.WHITE)
                }
            }
            iconView.invalidate()
        }
    }
}

/**
 * Main Xposed Hook Module for iOS-Style Connectivity Platter in Plugin ClassLoader
 */
class ConnectivityPlatterHook : BaseHook() {

    override fun init() {
        // Base hook initialization if loaded directly
    }

    companion object {
        private const val TAG = "ConnectivityPlatterHook"
        private const val PLATTER_SPEC = "connectivity_platter"

        private val activePlatters = Collections.newSetFromMap(WeakHashMap<ConnectivityPlatterView, Boolean>())
        private var observerRegistered = false

        private val isPlatterEnabled: Boolean
            get() = PrefsUtils.mPrefsMap.getBoolean("system_ui_control_center_ios_connectivity_platter", false) ||
                PrefsUtils.mPrefsMap.getBoolean("system_ui_control_center_connectivity_platter", false)

        @JvmStatic
        fun initConnectivityPlatterHook(classLoader: ClassLoader?) {
            if (classLoader == null) return
            initPlatterHooks(classLoader)
        }

        @JvmStatic
        fun initPlatterHooks(classLoader: ClassLoader) {
            // 1. Hook QSController.getCardStyleTileSpecs
            runCatching {
                XposedHelpers.findAndHookMethod(
                    "miui.systemui.controlcenter.qs.QSController",
                    classLoader,
                    "getCardStyleTileSpecs",
                    object : XC_MethodHook() {
                        @Suppress("UNCHECKED_CAST")
                        override fun afterHookedMethod(param: MethodHookParam) {
                            if (!isPlatterEnabled) return
                            val originalList = param.result as? List<String> ?: return
                            val modified = ArrayList<String>()

                            // Inject platter as primary card spec, removing redundant wifi/cell/bt cards
                            modified.add(PLATTER_SPEC)
                            for (spec in originalList) {
                                if (spec != "wifi" && spec != "cell" && spec != "bt" && spec != PLATTER_SPEC) {
                                    modified.add(spec)
                                }
                            }
                            param.result = modified
                        }
                    }
                )
                logI(TAG, "QSController getCardStyleTileSpecs hooked successfully.")
            }.onFailure {
                logE(TAG, "Failed to hook QSController.getCardStyleTileSpecs: $it")
            }

            // 2. Hook QSCardItemView constructor and view lifecycle
            runCatching {
                XposedHelpers.findAndHookConstructor(
                    "miui.systemui.controlcenter.qs.tileview.QSCardItemView",
                    classLoader,
                    Context::class.java,
                    AttributeSet::class.java,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val cardView = param.thisObject as? LinearLayout ?: return
                            registerObserverIfNeeded(cardView.context)
                        }
                    }
                )
            }.onFailure {
                logE(TAG, "Failed to hook QSCardItemView constructor: $it")
            }

            // 3. Hook QSCardItemView.updateState
            runCatching {
                val stateClass = "com.android.systemui.plugins.qs.QSTile\$State"
                XposedHelpers.findAndHookMethod(
                    "miui.systemui.controlcenter.qs.tileview.QSCardItemView",
                    classLoader,
                    "updateState",
                    stateClass,
                    Boolean::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            if (!isPlatterEnabled) return
                            val cardView = param.thisObject as? LinearLayout ?: return
                            val stateObj = param.args[0] ?: return
                            val spec = XposedHelpers.getObjectField(stateObj, "spec") as? String ?: return

                            if (spec == PLATTER_SPEC || spec == "wifi") {
                                injectPlatterViewIfNeeded(cardView)
                            }

                            // Forward state updates to platter
                            val stateInt = XposedHelpers.getIntField(stateObj, "state")
                            val subtitle = (XposedHelpers.getObjectField(stateObj, "secondaryLabel") as? CharSequence)?.toString() ?: ""
                            val tileState = when (stateInt) {
                                2 -> ConnectivityHostHelper.TileState.ACTIVE
                                1 -> ConnectivityHostHelper.TileState.INACTIVE
                                else -> ConnectivityHostHelper.TileState.UNAVAILABLE
                            }

                            for (platter in activePlatters) {
                                platter.updateSubTile(spec, tileState, subtitle)
                            }
                        }
                    }
                )
            }.onFailure {
                logE(TAG, "Failed to hook QSCardItemView.updateState: $it")
            }

            // 4. Hook QSCardItemView.updateBackground
            runCatching {
                XposedHelpers.findAndHookMethod(
                    "miui.systemui.controlcenter.qs.tileview.QSCardItemView",
                    classLoader,
                    "updateBackground",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            if (!isPlatterEnabled) return
                            val cardView = param.thisObject as? LinearLayout ?: return
                            val stateObj = runCatching { XposedHelpers.getObjectField(cardView, "state") }.getOrNull() ?: return
                            val spec = (XposedHelpers.getObjectField(stateObj, "spec") as? String) ?: ""
                            if (spec == PLATTER_SPEC) {
                                cardView.background = ColorDrawable(Color.TRANSPARENT)
                            }
                        }
                    }
                )
            }
        }

        private fun injectPlatterViewIfNeeded(cardView: LinearLayout) {
            val existing = cardView.findViewWithTag<View>("connectivity_platter_view")
            if (existing == null) {
                val platter = ConnectivityPlatterView(cardView.context).apply {
                    tag = "connectivity_platter_view"
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                }
                cardView.removeAllViews()
                cardView.addView(platter)
                activePlatters.add(platter)
            }
        }

        private fun registerObserverIfNeeded(context: Context) {
            if (observerRegistered) return
            observerRegistered = true
            val handler = Handler(Looper.getMainLooper())
            val key = "prefs_key_system_ui_control_center_ios_connectivity_platter"

            runCatching {
                object : PrefsChangeObserver(context, handler, true, PrefType.Boolean, key, false) {
                    override fun onChange(type: PrefType?, uri: Uri?, name: String?, def: Any?) {
                        handler.post {
                            for (platter in activePlatters) {
                                platter.refreshAllStates()
                            }
                        }
                    }
                }
            }
        }
    }
}
