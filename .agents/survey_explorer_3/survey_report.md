# HyperCeiler Survey Report: Requirements R2 & R4
**Requirements Focus**: 
- **R2: Dual Data Usage Panel** (Mobile & WiFi Data Display + Security App Intent)
- **R4: iOS-Style Connectivity Platter** (Expandable Connectivity Folder Grouping WiFi, BT, Mobile, Airplane Mode, Hotspot)
**Author**: Explorer 3 (`survey_explorer_3`)  
**Date**: 2026-08-21  
**Project**: HyperCeilerModded (`/data/data/com.termux/files/home/HyperCeilerModded`)

---

## 1. Executive Summary

This survey report provides the architectural analysis, target classes, method signatures, querying logic, layout injection strategies, and UI configuration points for implementing:
1. **Requirement R2 (Dual Data Usage Panel)**:
   - Hook into the Control Center header/footer data usage and carrier text views.
   - Calculate and display simultaneous Mobile and Wi-Fi data statistics in the format `"Mobile: X | WiFi: Y"`.
   - Override the long-press interaction on the data usage text view to dispatch an explicit/implicit Intent launching Xiaomi SecurityCenter's NetworkAssistant data usage management activity.
2. **Requirement R4 (iOS-Style Connectivity Platter / Folder)**:
   - Group core connectivity toggles (Wi-Fi, Bluetooth, Mobile Data, Airplane Mode, Hotspot, MiShare) into a unified iOS-style 2x2 platter card within the HyperOS Control Center.
   - Provide an expandable interaction model (long-press on the platter container) that expands into a modal overlay detailing connected SSID, paired Bluetooth devices, and secondary network toggles.
   - Wire individual sub-tiles to the underlying HyperOS / SystemUI `QSTile` controllers.

---

## 2. System Architecture Context

### 2.1 Dual-Context Execution Model in HyperOS
HyperOS splits Control Center execution across two contexts:
1. **Base SystemUI Process (`com.android.systemui`)**:
   - Host process containing core services (`NetworkStatsManager`, `ConnectivityManager`, `ActivityStarter`, `StatusBarStateController`, `MiuiCarrierTextController`, `ControlCenterStatusBar`).
   - Hooks registered directly in `SystemUiU.java` and `SystemUiV.java` via `BaseModule.initHook()`.
2. **SystemUI Plugin (`miui.systemui.plugin`)**:
   - Loaded dynamically by SystemUI (`PluginInstance$PluginFactory.createPluginContext()`).
   - Intercepted by HyperCeiler in `NewPluginHelperKt.kt` when `factory.mComponentName` matches `miui.systemui.controlcenter.MiuiControlCenter` or `miui.systemui.quicksettings.LocalMiuiQSTilePlugin`.
   - Contains modern Control Center card views (`QSCardItemView`, `QSCardsController`, `QSController`), tile grid layouts (`QSTileListLayout`), and tile views (`QSTileItemView`, `QSTileItemIconView`).

---

## 3. Requirement R2: Dual Data Usage Panel Deep Dive

### 3.1 Target Views and Text Controllers

In MIUI and HyperOS Control Center, network carrier and data consumption info are displayed in the status/header bar or footer bar of the Control Center panel.

| Component / Layer | Target Class | Methods / Fields | Hook Strategy |
|---|---|---|---|
| **Control Center Status Bar** | `com.android.systemui.controlcenter.phone.widget.ControlCenterStatusBar` | Fields: `carrierText`, `mCarrierText`, `dataUsage`<br>Methods: `onFinishInflate()`, `updateFlaresInfo()` | Intercept view inflation / update; inject dual data usage text into the `TextView`; attach `OnLongClickListener`. |
| **Control Center Header** | `com.android.systemui.qs.MiuiQSHeaderView` / `MiuiNotificationHeaderView` | Methods: `updateCarrierVisibility()`, `onFinishInflate()`, `updateDataUsage()` | Hook post-inflation to update `TextView` text and long-click handler. |
| **Carrier Text Controller** | `com.android.systemui.statusbar.policy.MiuiCarrierTextController` & `MiuiCarrierTextControllerImpl` | Methods: `fireCarrierTextChanged(String)`, `fireCarrierTextChanged(int, int, String)`, `updateCarrierText()` | Hook before or after to format text as `"Mobile: X \| WiFi: Y"` when dual data usage is enabled. |
| **Plugin Header / Footer** | `miui.systemui.controlcenter.panel.header.DataUsageView` (or `CarrierTextView`) | Methods: `updateDataUsage()`, `setText(CharSequence)` | In plugin ClassLoader, hook text setter and long press. |

### 3.2 Network Statistics Querying Mechanism

SystemUI runs under UID 1000 (`android.uid.system`) with elevated permissions (`Manifest.permission.READ_NETWORK_USAGE_STATS` and `ACCESS_NETWORK_STATE`). Data usage is queried directly via `NetworkStatsManager`:

```kotlin
package com.sevtinge.hyperceiler.hook.module.hook.systemui.controlcenter

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.text.format.Formatter
import java.util.Calendar

object DualDataUsageHelper {

    /**
     * Queries Mobile and WiFi data usage (bytes) from 00:00:00 today to the current time.
     * Returns Pair<MobileBytes, WifiBytes>.
     */
    fun queryTodayDataUsage(context: Context): Pair<Long, Long> {
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
            ?: return queryFallbackTrafficStats()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
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
            mobileBytes = bucketMobile.rxBytes + bucketMobile.txBytes
        } catch (t: Throwable) {
            mobileBytes = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()
        }

        // 2. Query Wi-Fi Data
        try {
            val bucketWifi = nsm.querySummaryForDevice(
                ConnectivityManager.TYPE_WIFI,
                null,
                startTime,
                endTime
            )
            wifiBytes = bucketWifi.rxBytes + bucketWifi.txBytes
        } catch (t: Throwable) {
            val totalBytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()
            val mobileTotal = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()
            wifiBytes = (totalBytes - mobileTotal).coerceAtLeast(0L)
        }

        return Pair(mobileBytes, wifiBytes)
    }

    private fun queryFallbackTrafficStats(): Pair<Long, Long> {
        val mobile = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()
        val total = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()
        val wifi = (total - mobile).coerceAtLeast(0L)
        return Pair(mobile, wifi)
    }

    /**
     * Formats data bytes into the required UI string: "Mobile: X | WiFi: Y"
     */
    fun formatDualDataUsageText(context: Context): String {
        val (mobileBytes, wifiBytes) = queryTodayDataUsage(context)
        val mobileStr = Formatter.formatShortFileSize(context, mobileBytes)
        val wifiStr = Formatter.formatShortFileSize(context, wifiBytes)
        return "Mobile: $mobileStr | WiFi: $wifiStr"
    }
}
```

### 3.3 Security App Intent for Data Usage Stats

To override the long-press interaction and launch SecurityCenter's data usage management:

- **Target Package**: `com.miui.securitycenter`
- **Primary Target Component**: `com.miui.securitycenter/com.miui.networkassistant.ui.activity.MainActivity`
- **Secondary Target Component**: `com.miui.securitycenter/com.miui.networkassistant.ui.activity.TrafficSortedActivity`
- **Standard Action**: `miui.intent.action.NETWORKASSISTANT_MAIN` / `miui.intent.action.NETWORKASSISTANT_STAT_MAIN`

```kotlin
fun launchSecurityDataUsageActivity(context: Context) {
    val intent = Intent().apply {
        component = ComponentName(
            "com.miui.securitycenter",
            "com.miui.networkassistant.ui.activity.MainActivity"
        )
        action = "miui.intent.action.NETWORKASSISTANT_MAIN"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    try {
        // Try starting via SystemUI ActivityStarter to dismiss Control Center cleanly
        val activityStarter = Dependency.get("com.android.systemui.plugins.ActivityStarter")
        if (activityStarter != null) {
            XposedHelpers.callMethod(activityStarter, "postStartActivityDismissingKeyguard", intent, 0)
            return
        }
    } catch (ignored: Throwable) {}

    try {
        context.startActivity(intent)
    } catch (e: Throwable) {
        val fallback = Intent("miui.intent.action.NETWORKASSISTANT_MAIN").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(fallback)
    }
}
```

### 3.4 Hook Implementation: `DualDataUsageHook.kt`

```kotlin
package com.sevtinge.hyperceiler.hook.module.hook.systemui.controlcenter

import android.content.Context
import android.view.View
import android.widget.TextView
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

class DualDataUsageHook : BaseHook() {

    override fun init() {
        val isDualEnabled = mPrefsMap.getBoolean("system_ui_control_center_dual_data_usage")
        if (!isDualEnabled) return

        val updateTextHook = object : MethodHook() {
            @Override
            protected fun after(param: MethodHookParam) {
                val viewObj = runCatching {
                    XposedHelpers.getObjectField(param.thisObject, "carrierText")
                }.getOrElse {
                    runCatching {
                        XposedHelpers.getObjectField(param.thisObject, "mCarrierText")
                    }.getOrNull()
                }

                if (viewObj is TextView) {
                    val context = viewObj.context
                    val dualText = DualDataUsageHelper.formatDualDataUsageText(context)
                    viewObj.text = dualText
                    viewObj.isLongClickable = true
                    viewObj.setOnLongClickListener { v ->
                        DualDataUsageHelper.launchSecurityDataUsageActivity(v.context)
                        true
                    }
                }
            }
        }

        // Hook ControlCenterStatusBar
        hookAllMethods("com.android.systemui.controlcenter.phone.widget.ControlCenterStatusBar", "updateFlaresInfo", updateTextHook)
        findAndHookMethodSilently("com.android.systemui.controlcenter.phone.widget.ControlCenterStatusBar", "onFinishInflate", updateTextHook)

        // Hook MiuiQSHeaderView
        findAndHookMethodSilently("com.android.systemui.qs.MiuiQSHeaderView", "updateCarrierVisibility", updateTextHook)
        findAndHookMethodSilently("com.android.systemui.qs.MiuiQSHeaderView", "onFinishInflate", updateTextHook)
    }
}
```

---

## 4. Requirement R4: iOS-Style Connectivity Platter Deep Dive

### 4.1 Concept and Visual Structure

In iOS Control Center, the top-left area is a unified **Connectivity Folder** platter:
- **Default (Collapsed) State**:
  - Dimensions: 2x2 grid card (same footprint as 4 small tiles or 2 wide cards).
  - Contains 4 circular toggle buttons:
    1. **Airplane Mode** (Icon + State)
    2. **Cellular Data** (Icon + State)
    3. **Wi-Fi** (Icon + State)
    4. **Bluetooth** (Icon + State)
  - Tapping any button directly toggles its state (e.g. Wi-Fi on/off).
- **Expanded (Modal Overlay) State**:
  - Triggered by long-pressing anywhere on the platter container (or expand button).
  - Displays a full-screen or centered frosted-glass acrylic card containing 6 connectivity items (2 columns x 3 rows):
    1. **Airplane Mode**: Icon + "Airplane Mode" + "Off/On"
    2. **Cellular Data**: Icon + "Cellular Data" + "Carrier / On / Off"
    3. **Wi-Fi**: Icon + "Wi-Fi" + "SSID / Not Connected"
    4. **Bluetooth**: Icon + "Bluetooth" + "Connected Device / On / Off"
    5. **AirDrop / MiShare**: Icon + "MiShare" + "Contacts Only / Off"
    6. **Personal Hotspot**: Icon + "Personal Hotspot" + "Discoverable / Off"
  - In expanded state, tapping toggles the item; long-pressing an item opens the specific system Settings subpage (e.g., `Settings.ACTION_WIFI_SETTINGS`).
  - Tapping outside dismisses the modal.

```
+-----------------------------------------------------------+
|                    CONTROL CENTER                         |
|  +---------------------+   +---------------------------+  |
|  | CONNECTIVITY FOLDER |   |  NOW PLAYING / MEDIA CARD |  |
|  |  [Airplane]  [Cell] |   |                           |  |
|  |  [ Wi-Fi  ]  [ BT ] |   +---------------------------+  |
|  +---------------------+   +-------------+ +-----------+  |
|                            | Brightness  | |  Volume   |  |
|  +----+ +----+ +----+ +----+                            |  |
|  |Flash|Lock |Rotate|Mute |                            |  |
+-----------------------------------------------------------+
                              | Long Press Platter
                              v
+-----------------------------------------------------------+
|              EXPANDED CONNECTIVITY PLATTER                |
|  +-----------------------------------------------------+  |
|  |  ( ) Airplane Mode      |  ( ) Cellular Data        |  |
|  |      Off                |      China Mobile 5G      |  |
|  |-------------------------+---------------------------|  |
|  |  ( ) Wi-Fi              |  ( ) Bluetooth            |  |
|  |      Home_5G            |      AirPods Pro Connected|  |
|  |-------------------------+---------------------------|  |
|  |  ( ) AirDrop / MiShare  |  ( ) Personal Hotspot     |  |
|  |      Contacts Only      |      Not Discoverable     |  |
|  +-----------------------------------------------------+  |
|                   [Tap outside to close]                  |
+-----------------------------------------------------------+
```

### 4.2 HyperOS Plugin Hook Targets for R4

Inside `miui.systemui.plugin` (injected via `NewPluginHelperKt`):

| Target Class | Method | Hook Point | Purpose |
|---|---|---|---|
| `miui.systemui.controlcenter.qs.QSController` | `getCardStyleTileSpecs` | `beforeHookedMethod` | Insert `"connectivity_folder"` or replace `"wifi"`, `"cell"`, `"bt"` specs with the composite platter spec. |
| `miui.systemui.controlcenter.panel.main.qs.QSCardsController` | `createCardView(String, ViewGroup)` or `onBindViewHolder(...)` | `afterHookedMethod` | Instantiate custom `ConnectivityPlatterView` when spec == `"connectivity_folder"`. |
| `miui.systemui.controlcenter.qs.tileview.QSCardItemView` | `updateState(QSTile$State, boolean, boolean)` | `beforeHookedMethod` | Intercept tile state updates and dispatch to sub-tiles inside the platter. |
| `com.android.systemui.plugins.qs.QSTile` / `QSTileImpl` | `click(Expandable)` / `handleLongClick()` | Direct Invocation | Wire mini-tile button clicks to trigger native QS tile actions. |

### 4.3 Concrete Platter Implementation Design

#### 1. `ConnectivityPlatterView.kt`
A custom `LinearLayout` / `GridLayout` subclass constructed dynamically in the plugin context:

```kotlin
package com.sevtinge.hyperceiler.hook.module.hook.systemui.controlcenter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import de.robv.android.xposed.XposedHelpers

class ConnectivityPlatterView(context: Context) : LinearLayout(context) {

    private val gridLayout: GridLayout
    private val subButtons = mutableMapOf<String, ImageView>()

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        
        // Acrylic rounded background
        val bg = GradientDrawable().apply {
            setColor(Color.parseColor("#33FFFFFF"))
            cornerRadius = 48f
        }
        background = bg
        setPadding(24, 24, 24, 24)

        // 2x2 Grid for collapsed state
        gridLayout = GridLayout(context).apply {
            columnCount = 2
            rowCount = 2
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        addView(gridLayout)

        val specs = listOf("airplane", "cell", "wifi", "bt")
        specs.forEach { spec ->
            val btn = createMiniTileButton(context, spec)
            subButtons[spec] = btn
            gridLayout.addView(btn)
        }

        // Long press on container triggers expanded dialog
        isLongClickable = true
        setOnLongClickListener {
            showExpandedPlatterDialog(context)
            true
        }
    }

    private fun createMiniTileButton(context: Context, spec: String): ImageView {
        return ImageView(context).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 120
                height = 120
                setMargins(12, 12, 12, 12)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#44888888"))
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setOnClickListener {
                ConnectivityHostHelper.toggleTile(spec)
            }
        }
    }

    private fun showExpandedPlatterDialog(context: Context) {
        ConnectivityExpandDialog(context).show()
    }
}
```

#### 2. `ConnectivityExpandDialog.kt`
A modal overlay containing the 6 full-detail connectivity rows:

```kotlin
package com.sevtinge.hyperceiler.hook.module.hook.systemui.controlcenter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class ConnectivityExpandDialog(context: Context) : Dialog(context) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)

        val rootCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E6202020"))
                cornerRadius = 64f
            }
            setPadding(40, 40, 40, 40)
        }

        val grid = GridLayout(context).apply {
            columnCount = 2
            rowCount = 3
        }

        val items = listOf(
            ExpandedItem("airplane", "Airplane Mode", "Off", Settings.ACTION_AIRPLANE_MODE_SETTINGS),
            ExpandedItem("cell", "Cellular Data", "Connected", Settings.ACTION_DATA_ROAMING_SETTINGS),
            ExpandedItem("wifi", "Wi-Fi", "Connected", Settings.ACTION_WIFI_SETTINGS),
            ExpandedItem("bt", "Bluetooth", "On", Settings.ACTION_BLUETOOTH_SETTINGS),
            ExpandedItem("mishare", "MiShare", "Off", null),
            ExpandedItem("hotspot", "Personal Hotspot", "Off", Settings.ACTION_WIRELESS_SETTINGS)
        )

        items.forEach { item ->
            grid.addView(createExpandedTileView(context, item))
        }

        rootCard.addView(grid)
        setContentView(rootCard)
    }

    private fun createExpandedTileView(context: Context, item: ExpandedItem): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20, 20, 20, 20)
            layoutParams = GridLayout.LayoutParams().apply {
                width = 380
                height = 160
                setMargins(16, 16, 16, 16)
            }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#33FFFFFF"))
                cornerRadius = 32f
            }

            val icon = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(96, 96)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#FF007AFF"))
                }
            }

            val textLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 0, 0, 0)
                
                val title = TextView(context).apply {
                    text = item.title
                    setTextColor(Color.WHITE)
                    textSize = 14f
                }
                val subtitle = TextView(context).apply {
                    text = item.subtitle
                    setTextColor(Color.LTGRAY)
                    textSize = 11f
                }
                addView(title)
                addView(subtitle)
            }

            addView(icon)
            addView(textLayout)

            setOnClickListener {
                ConnectivityHostHelper.toggleTile(item.spec)
            }
            setOnLongClickListener {
                item.settingsAction?.let { action ->
                    val intent = Intent(action).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    context.startActivity(intent)
                    dismiss()
                }
                true
            }
        }
    }

    data class ExpandedItem(val spec: String, val title: String, val subtitle: String, val settingsAction: String?)
}
```

#### 3. Platter Registration in `NewPluginHelperKt.kt`

```kotlin
Triple(
    "ConnectivityPlatter",
    mPrefsMap.getBoolean("system_ui_control_center_connectivity_platter")
) { cl -> ConnectivityPlatterHook.init(cl) }
```

---

## 5. UI Preferences & Localization Design

### 5.1 Preferences XML (`system_ui_control_center.xml` & `system_ui_control_center_tiles.xml`)

Add the following preference declarations:

```xml
<!-- In system_ui_control_center.xml -->
<PreferenceCategory android:title="@string/system_ui_control_center_data_usage_category">
    <SwitchPreference
        android:defaultValue="false"
        android:key="prefs_key_system_ui_control_center_dual_data_usage"
        android:title="@string/system_ui_control_center_dual_data_usage"
        android:summary="@string/system_ui_control_center_dual_data_usage_desc" />
</PreferenceCategory>

<!-- In system_ui_control_center_tiles.xml -->
<PreferenceCategory android:title="@string/system_ui_control_center_platter_category">
    <SwitchPreference
        android:defaultValue="false"
        android:key="prefs_key_system_ui_control_center_connectivity_platter"
        android:title="@string/system_ui_control_center_connectivity_platter"
        android:summary="@string/system_ui_control_center_connectivity_platter_desc" />
</PreferenceCategory>
```

### 5.2 String Resources (`strings_app.xml`)

```xml
<!-- English: values/strings_app.xml -->
<string name="system_ui_control_center_data_usage_category">Data Usage Display</string>
<string name="system_ui_control_center_dual_data_usage">Dual Data Usage (Mobile &amp; Wi-Fi)</string>
<string name="system_ui_control_center_dual_data_usage_desc">Display simultaneous Mobile and Wi-Fi data in Control Center. Long-press to open Security Center data stats.</string>

<string name="system_ui_control_center_platter_category">Connectivity Folder</string>
<string name="system_ui_control_center_connectivity_platter">iOS-Style Connectivity Platter</string>
<string name="system_ui_control_center_connectivity_platter_desc">Group Wi-Fi, Bluetooth, Mobile Data, and Airplane Mode into an expandable 2x2 platter.</string>

<!-- Chinese: values-zh-rCN/strings_app.xml -->
<string name="system_ui_control_center_data_usage_category">流量数据显示</string>
<string name="system_ui_control_center_dual_data_usage">双流量数据显示 (移动 &amp; Wi-Fi)</string>
<string name="system_ui_control_center_dual_data_usage_desc">在控制中心同时显示移动网络与 Wi-Fi 消耗流量。长按打开手机管家网络助手。</string>

<string name="system_ui_control_center_platter_category">网络连接文件夹</string>
<string name="system_ui_control_center_connectivity_platter">iOS 风格连接大卡片 (文件夹)</string>
<string name="system_ui_control_center_connectivity_platter_desc">将 Wi-Fi、蓝牙、移动数据和飞行模式整合为 2x2 聚合卡片，长按展开更多设置。</string>
```

---

## 6. Synthesis & Risk Assessment

| Feature Area | Technical Challenge | Mitigation Strategy |
|---|---|---|
| **R2: Data Usage Query Performance** | `NetworkStatsManager` queries can take ~5-15ms if executed synchronously on the main thread. | Cache queries with a 10-second debounce timer or perform asynchronous background updates (`ThreadPoolManager`), updating the `TextView` on the main Looper. |
| **R2: Multi-SIM Devices** | Dual SIM setups have multiple `subscriberId`s. | Passing `null` as `subscriberId` in `querySummaryForDevice(TYPE_MOBILE, null, ...)` aggregates all active SIM subscriptions across the device. |
| **R4: QSTile Callback Threading** | Tile state updates occur on worker/handler threads. | Ensure all UI state modifications on `ConnectivityPlatterView` and `ConnectivityExpandDialog` are dispatched to `Looper.getMainLooper()`. |
| **R4: Plugin ClassLoader Isolation** | Resources and custom classes defined in HyperCeiler APK are not directly reachable from `miui.systemui.plugin`. | Build views dynamically in code using standard Android ViewGroup widgets and SystemUI plugin context, styling programmatically with `GradientDrawable`. |

---

## 7. Next Steps for Implementation Phase
1. Create `DualDataUsageHook.kt` and `DualDataUsageHelper.kt` in `com.sevtinge.hyperceiler.hook.module.hook.systemui.controlcenter`.
2. Register `DualDataUsageHook` in `SystemUiU.java` and `SystemUiV.java`.
3. Create `ConnectivityPlatterHook.kt`, `ConnectivityPlatterView.kt`, and `ConnectivityExpandDialog.kt` in `com.sevtinge.hyperceiler.hook.module.hook.systemui.controlcenter`.
4. Register `ConnectivityPlatterHook` in `NewPluginHelperKt.kt` within the `LocalMiuiQSTilePlugin` / `MiuiControlCenter` loader list.
5. Add preference toggles to `system_ui_control_center.xml` and `system_ui_control_center_tiles.xml`, and update `ControlCenterSettings.java` and `TileSettings.java`.
