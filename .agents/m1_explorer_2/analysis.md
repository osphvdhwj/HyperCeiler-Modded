# Architectural Analysis: QSTileListLayout vs MiuiTileLayout, Multi-Form-Factor Geometry & Dynamic Preference Reactivity

**Author**: Explorer 2 (`m1_explorer_2`)  
**Milestone**: M1 (Requirement R1: Adjustable Grid & Padding)  
**Date**: 2026-08-21  
**Project**: HyperCeilerModded (`/data/data/com.termux/files/home/HyperCeilerModded`)

---

## 1. Executive Summary

This report delivers an exhaustive architectural investigation into the Quick Settings (QS) grid and tile layouts in Xiaomi HyperOS / MIUI. Specifically, it resolves:
1. The exact relationship and coexistence between `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` (HyperOS plugin) and `com.android.systemui.qs.MiuiTileLayout` (legacy host SystemUI).
2. The measurement, column calculation, and spacing dynamics during orientation switches (portrait vs landscape) and across diverse form factors (foldables unfolded/folded and tablets).
3. The specification for preference keys, default values, bounds, and UI controls for Milestone M1 (Requirement R1).
4. A zero-restart dynamic reactivity architecture utilizing `SharedPrefsProvider` and `PrefsChangeObserver` to update grid columns, inter-tile margins, and edge padding on the fly.

---

## 2. Interaction Analysis: `QSTileListLayout` vs `MiuiTileLayout`

### 2.1 Dual-Context Execution Model

Xiaomi HyperOS divides the Quick Settings / Control Center rendering across two execution domains:

```
+-----------------------------------------------------------------------------------------+
|                                Host SystemUI Process                                    |
|                                (com.android.systemui)                                   |
|                                                                                         |
|  - ClassLoader: Base Application ClassLoader                                            |
|  - Classic QS View: com.android.systemui.qs.MiuiTileLayout                             |
|  - Legacy Control Center / AOSP Notification Shade                                      |
|  - Managed via: SystemUiV.java / SystemUiU.java -> QSGrid.kt                           |
|  - Preference Toggle: prefs_key_system_control_center_old_enable                        |
+--------------------------------------------+--------------------------------------------+
                                             |
                          Plugin Loader (NewPluginHelperKt.kt)
                                             |
                                             v
+-----------------------------------------------------------------------------------------+
|                             Dynamic Control Center Plugin                               |
|                                 (miui.systemui.plugin)                                  |
|                                                                                         |
|  - ClassLoader: Dynamic Plugin ClassLoader (PluginInstance$PluginFactory)               |
|  - Modern HyperOS Grid Container: miui.systemui.controlcenter.qs.tileview.QSTileListLayout |
|  - Modern Tile View: miui.systemui.controlcenter.qs.tileview.QSTileItemIconView        |
|  - Managed via: NewPluginHelperKt.kt -> CCGridForHyperOSKt.kt                           |
|  - Preference Toggle: prefs_key_system_ui_control_center_custom_grid                    |
+-----------------------------------------------------------------------------------------+
```

### 2.2 Comparison Matrix

| Property | `MiuiTileLayout` | `QSTileListLayout` |
|---|---|---|
| **Package** | `com.android.systemui.qs` | `miui.systemui.controlcenter.qs.tileview` |
| **Execution Context** | Base `com.android.systemui` APK | Dynamic plugin (`miui.systemui.plugin`) |
| **ClassLoader Target** | `BaseHook.lpparam.classLoader` | Captured via `NewPluginHelperKt.kt` (`factory.pluginCtxRef.get().classLoader`) |
| **Active When** | Old/Classic QS Shade enabled | Modern HyperOS Control Center enabled (default) |
| **Grid Fields** | `mColumns`, `mMaxAllowedRows`, `mCellMarginHorizontal` | `mColumns`, `mCellMarginHorizontal`, `mCellMarginVertical` |
| **Layout Hook Point** | `updateResources()`, `layoutTileRecords(int)` | `updateResources()`, `onMeasure(int, int)` |
| **Existing Hook** | `com.sevtinge.hyperceiler.hook.module.hook.systemui.controlcenter.QSGrid` | `com.sevtinge.hyperceiler.hook.module.hook.systemui.controlcenter.CCGridForHyperOSKt` |
| **Preference Domain** | `prefs_key_system_control_center_old_*` | `prefs_key_system_ui_control_center_*` |

### 2.3 Coexistence Rules
1. **Isolation**: `QSTileListLayout` and `MiuiTileLayout` never interact directly; they exist in separate ClassLoaders and are instantiated under different UI mode selections.
2. **No Hook Conflicts**: Modifying `QSTileListLayout` in the plugin ClassLoader does not interfere with `QSGrid.kt` hooking `MiuiTileLayout` in the host ClassLoader.
3. **M1 Scope**: Requirement R1 targets `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` in the dynamic plugin ClassLoader.

---

## 3. Orientation, Foldables, and Tablet Column Geometry

### 3.1 Measurement & Layout Lifecycle

When `QSTileListLayout` measures child tiles in `onMeasure(int widthMeasureSpec, int heightMeasureSpec)`:

1. **Available Width Calculation**:
   $$\text{Available Width} = \text{Container Width} - \text{PaddingLeft} - \text{PaddingRight} - (\text{mColumns} - 1) \times \text{mCellMarginHorizontal}$$

2. **Tile Cell Width**:
   $$\text{Tile Width} = \left\lfloor \frac{\text{Available Width}}{\text{mColumns}} \right\rfloor$$

3. **Grid Layout**:
   Tiles are arranged row by row with `mColumns` items per row, spacing each item horizontally by `mCellMarginHorizontal` and vertically by `mCellMarginVertical`.

### 3.2 Orientation Handling (Portrait vs Landscape)

- On rotation, `updateResources()` is called by the framework.
- Hooking `updateResources()` allows inspecting `resources.configuration.orientation`:
  - `Configuration.ORIENTATION_PORTRAIT`: Assign `mColumns = colsPortrait` (default 4, range 3..6).
  - `Configuration.ORIENTATION_LANDSCAPE`: Assign `mColumns = colsLandscape` (default 6, range 4..8).
- Additionally, inter-tile margins (`mCellMarginHorizontal`, `mCellMarginVertical`) and container padding (`setPadding(left, top, right, bottom)`) must be refreshed to match user settings.

### 3.3 Foldables & Tablets / Pads Support

- Foldable unfolded screens and tablets exhibit wide aspect ratios even in portrait orientation (`smallestScreenWidthDp >= 600`).
- By providing independent controls for **Portrait Columns** (3..6) and **Landscape Columns** (4..8), foldable/pad users can configure 5 or 6 columns in portrait to prevent oversized icons.
- Container edge padding (`grid_padding_h`) allows pad users to center or expand the grid within wide display margins without clipping.

---

## 4. Preference Design & Specification Recommendations

### 4.1 Recommended Keys & Default Values

To maintain perfect alignment with `PROJECT.md` and HyperCeiler conventions:

| Preference Key | Type | Default | Bounds / Step | Description |
|---|---|---|---|---|
| `prefs_key_system_ui_control_center_custom_grid` | Boolean | `false` | `true` / `false` | Master toggle for modern Control Center grid customization |
| `prefs_key_system_ui_control_center_grid_columns` | Integer | `4` | `min="3"`, `max="6"`, `step="1"` | Column count in Portrait orientation |
| `prefs_key_system_ui_control_center_grid_columns_horizontal` | Integer | `6` | `min="4"`, `max="8"`, `step="1"` | Column count in Landscape orientation |
| `prefs_key_system_ui_control_center_tile_margin_h` | Integer | `8` (dp) | `min="0"`, `max="30"`, `step="1"` | Horizontal spacing between adjacent small tiles |
| `prefs_key_system_ui_control_center_tile_margin_v` | Integer | `8` (dp) | `min="0"`, `max="30"`, `step="1"` | Vertical spacing between adjacent tile rows |
| `prefs_key_system_ui_control_center_grid_padding_h` | Integer | `16` (dp) | `min="0"`, `max="40"`, `step="2"` | Left and right edge container padding |

### 4.2 Preference XML Schema (`system_ui_control_center_tiles.xml`)

```xml
<PreferenceCategory android:title="@string/system_ui_control_center_grid_layout_title">
    <SwitchPreference
        android:defaultValue="false"
        android:key="prefs_key_system_ui_control_center_custom_grid"
        android:title="@string/system_ui_control_center_custom_grid" />

    <SeekBarPreferenceCompat
        app:defaultValue="4"
        android:dependency="prefs_key_system_ui_control_center_custom_grid"
        android:key="prefs_key_system_ui_control_center_grid_columns"
        android:title="@string/system_control_center_qs_columns"
        app:defaultValueTitle="@string/array_default"
        app:format="@string/system_control_center_qs_columns_format"
        app:maxValue="6"
        app:minValue="3"
        app:showSeekBarValue="true"
        app:stepValue="1" />

    <SeekBarPreferenceCompat
        app:defaultValue="6"
        android:dependency="prefs_key_system_ui_control_center_custom_grid"
        android:key="prefs_key_system_ui_control_center_grid_columns_horizontal"
        android:title="@string/system_control_center_qs_columns_horizontal"
        app:defaultValueTitle="@string/array_default"
        app:format="@string/system_control_center_qs_columns_format"
        app:maxValue="8"
        app:minValue="4"
        app:showSeekBarValue="true"
        app:stepValue="1" />

    <SeekBarPreferenceCompat
        app:defaultValue="8"
        android:dependency="prefs_key_system_ui_control_center_custom_grid"
        android:key="prefs_key_system_ui_control_center_tile_margin_h"
        android:title="@string/system_ui_control_center_tile_margin_h"
        app:defaultValueTitle="@string/array_default"
        app:format="%d dp"
        app:maxValue="30"
        app:minValue="0"
        app:showSeekBarValue="true"
        app:stepValue="1" />

    <SeekBarPreferenceCompat
        app:defaultValue="8"
        android:dependency="prefs_key_system_ui_control_center_custom_grid"
        android:key="prefs_key_system_ui_control_center_tile_margin_v"
        android:title="@string/system_ui_control_center_tile_margin_v"
        app:defaultValueTitle="@string/array_default"
        app:format="%d dp"
        app:maxValue="30"
        app:minValue="0"
        app:showSeekBarValue="true"
        app:stepValue="1" />

    <SeekBarPreferenceCompat
        app:defaultValue="16"
        android:dependency="prefs_key_system_ui_control_center_custom_grid"
        android:key="prefs_key_system_ui_control_center_grid_padding_h"
        android:title="@string/system_ui_control_center_grid_padding_h"
        app:defaultValueTitle="@string/array_default"
        app:format="%d dp"
        app:maxValue="40"
        app:minValue="0"
        app:showSeekBarValue="true"
        app:stepValue="2" />
</PreferenceCategory>
```

---

## 5. Dynamic Preference Reactivity (Zero-Restart Strategy)

### 5.1 Real-Time IPC Flow

HyperCeiler includes a built-in cross-process notification mechanism:
1. When a user alters any preference in `TileSettings`, `registerOnSharedPreferenceChangeListener` notifies the Android `ContentResolver` at `content://com.harry.hyperhand.prefs/integer/prefs_key_...` or `content://com.harry.hyperhand.prefs/boolean/prefs_key_...`.
2. Inside the hooked SystemUI process, `PrefsChangeObserver` (extending `ContentObserver`) intercepts this URI change.
3. With `autoApplyChange = true`, `PrefsChangeObserver` queries `SharedPrefsProvider` via `Cursor` and synchronously updates `PrefsUtils.mPrefsMap`.
4. In `QSTileListLayout`, registering a `PrefsChangeObserver` and retaining a `WeakReference<ViewGroup>` allows instantly triggering:
   ```kotlin
   view.post {
       applyGridParams(view)
       view.requestLayout()
       view.invalidate()
   }
   ```
5. As a result, changes to columns, margins, or padding reflect **immediately** without requiring SystemUI reboot or process kill!

---

## 6. Concrete Code Implementation Blueprint for M1

### 6.1 Kotlin Hook Implementation (`CCGridForHyperOSKt.kt`)

```kotlin
package com.sevtinge.hyperceiler.hook.module.hook.systemui.controlcenter

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.ViewGroup
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logE
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefType
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefsChangeObserver
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefsUtils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import java.lang.ref.WeakReference

object CCGridForHyperOSKt {

    private var activeLayoutRef: WeakReference<ViewGroup>? = null
    private var observerRegistered = false

    @JvmStatic
    fun initCCGridForHyperOS(classLoader: ClassLoader?) {
        if (classLoader == null) return

        // Hook QSTileListLayout constructor or updateResources
        XposedHelpers.findAndHookMethod(
            "miui.systemui.controlcenter.qs.tileview.QSTileListLayout",
            classLoader,
            "updateResources",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val layout = param.thisObject as? ViewGroup ?: return
                    activeLayoutRef = WeakReference(layout)
                    registerObserverIfNeeded(layout.context)
                    applyGridGeometry(layout)
                }
            }
        )

        // Ensure onMeasure applies margins if necessary
        XposedHelpers.findAndHookMethod(
            "miui.systemui.controlcenter.qs.tileview.QSTileListLayout",
            classLoader,
            "onMeasure",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val layout = param.thisObject as? ViewGroup ?: return
                    applyGridGeometry(layout)
                }
            }
        )
    }

    private fun registerObserverIfNeeded(context: Context) {
        if (observerRegistered) return
        observerRegistered = true
        val handler = Handler(Looper.getMainLooper())

        val keys = listOf(
            "prefs_key_system_ui_control_center_custom_grid",
            "prefs_key_system_ui_control_center_grid_columns",
            "prefs_key_system_ui_control_center_grid_columns_horizontal",
            "prefs_key_system_ui_control_center_tile_margin_h",
            "prefs_key_system_ui_control_center_tile_margin_v",
            "prefs_key_system_ui_control_center_grid_padding_h"
        )

        for (key in keys) {
            val isBool = key.contains("custom_grid")
            val type = if (isBool) PrefType.Boolean else PrefType.Integer
            val defVal: Any = if (isBool) false else 0

            object : PrefsChangeObserver(context, handler, true, type, key, defVal) {
                override fun onChange(type: PrefType?, uri: Uri?, name: String?, def: Any?) {
                    activeLayoutRef?.get()?.let { view ->
                        view.post {
                            applyGridGeometry(view)
                            view.requestLayout()
                            view.invalidate()
                        }
                    }
                }
            }
        }
    }

    fun applyGridGeometry(view: ViewGroup) {
        val enabled = PrefsUtils.mPrefsMap.getBoolean("system_ui_control_center_custom_grid")
        if (!enabled) return

        val isPortrait = view.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val colsPortrait = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_columns", 4)
        val colsLandscape = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_columns_horizontal", 6)
        val cols = if (isPortrait) colsPortrait else colsLandscape

        val marginH_dp = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_tile_margin_h", 8)
        val marginV_dp = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_tile_margin_v", 8)
        val paddingH_dp = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_padding_h", 16)

        val dm = view.resources.displayMetrics
        val marginHPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginH_dp.toFloat(), dm).toInt()
        val marginVPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginV_dp.toFloat(), dm).toInt()
        val paddingHPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingH_dp.toFloat(), dm).toInt()

        runCatching { XposedHelpers.setIntField(view, "mColumns", cols) }
        runCatching { XposedHelpers.setIntField(view, "mCellMarginHorizontal", marginHPx) }
        runCatching { XposedHelpers.setIntField(view, "mCellMarginVertical", marginVPx) }
        
        // Preserve vertical padding while overriding horizontal edge padding
        view.setPadding(paddingHPx, view.paddingTop, paddingHPx, view.paddingBottom)
    }
}
```

### 6.2 Plugin Registration in `NewPluginHelperKt.kt`

In `NewPluginHelperKt.kt`, ensure `CCGridForHyperOS` is registered under both `MiuiControlCenter` and `LocalMiuiQSTilePlugin`:

```kotlin
Triple(
    "CCGridForHyperOS",
    mPrefsMap.getBoolean("system_ui_control_center_custom_grid") ||
    mPrefsMap.getBoolean("system_ui_control_center_rounded_rect"),
    CCGridForHyperOSKt::initCCGridForHyperOS
),
```

---

## 7. Verification Method

1. **Static Analysis & Inspection**:
   - Verify `system_ui_control_center_tiles.xml` contains all 6 preference elements with correct keys and dependencies.
   - Verify `CCGridForHyperOSKt.kt` compiles with `./gradlew compileDebugKotlin`.
2. **Build Verification**:
   - Run `./gradlew assembleDebug` to ensure whole-project compilation.
3. **Runtime & Dynamic Verification**:
   - In HyperCeiler Settings -> Quick Settings Tiles -> Grid & Spacing:
     - Toggle `Enable Custom Grid Layout`.
     - Change Portrait columns from 4 to 5 or 3. Observe immediate grid reflow in Control Center.
     - Adjust tile margin sliders (horizontal and vertical). Observe immediate spacing adjustment.
     - Adjust edge padding slider. Observe immediate margin shift from screen edges.
     - Rotate device to landscape. Confirm landscape columns apply without crash.
