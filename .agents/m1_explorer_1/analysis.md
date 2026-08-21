# Milestone M1 (Requirement R1: Adjustable Grid & Padding) - Technical Analysis & Design

## 1. Overview & Scope
Requirement R1 calls for customizable grid columns (default 4 portrait, customizable landscape), horizontal and vertical inter-tile spacing/margins, and container edge padding for HyperOS Control Center small quick settings tiles.

This analysis provides the complete architectural design, target classes, exact hook methods, fields, and UI preference definitions/bindings for:
1. **Hook Layer**:
   - `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` (loaded via dynamic plugin `miui.systemui.plugin`).
   - Integration into `CCGridForHyperOSKt.kt` and dispatch registration in `NewPluginHelperKt.kt`.
2. **Settings UI Layer**:
   - `library/common-ui/java/main/res/xml/system_ui_control_center_tiles.xml` preference schema.
   - `library/common-ui/java/main/src/com/sevtinge/hyperceiler/ui/hooker/systemui/TileSettings.java` controller logic and lifecycle bindings.
   - `library/common-ui/java/main/res/values/strings_app.xml` and `values-zh-rCN/strings_app.xml` localization entries.

---

## 2. Target Class & Hook Mechanics

### 2.1 Target Class: `miui.systemui.controlcenter.qs.tileview.QSTileListLayout`
In HyperOS (both HyperOS 1.0 and 2.0), the small QS tiles in the modern Control Center panel are contained and laid out by `miui.systemui.controlcenter.qs.tileview.QSTileListLayout`.

| Target Field / Method | Type / Signature | Execution Role & Hook Strategy |
|---|---|---|
| `mColumns` | `int` field | Number of tile columns in the grid. Dynamically overridden based on portrait vs landscape orientation. |
| `updateResources()` | `void updateResources()` | Called on initial layout, configuration (orientation/density) changes, and theme changes. Hook `after` to assign columns, margins, padding, and invoke `requestLayout()`. |
| `onMeasure(int, int)` | `void onMeasure(int widthSpec, int heightSpec)` | Measures tile layout and child tile dimensions. Hook `before` to ensure `mColumns`, margins, and padding are set immediately before measure calculations. |
| `mCellMarginHorizontal` / `mTileMarginHorizontal` | `int` field | Horizontal spacing between adjacent tile columns. Set from user preference dp converted to pixels. |
| `mCellMarginVertical` / `mTileMarginVertical` | `int` field | Vertical spacing between adjacent tile rows. Set from user preference dp converted to pixels. |
| `setPaddingRelative(int, int, int, int)` / `mPaddingStart`, `mPaddingEnd` | method / `int` fields | Container edge margins/padding. Set from user preference dp converted to pixels. |
| `getColumns()` / `getColumnCount()` | `int ()` methods (optional) | Queried by controllers in some ROM variants. Hook `after` to return user columns. |

### 2.2 Execution Context & Dynamic Plugin Loader
Because modern Control Center views are part of `miui.systemui.plugin`, hooks targeting `QSTileListLayout` cannot be bound at initial zygote boot with the system classloader. Instead, `NewPluginHelperKt.kt` intercepts `com.android.systemui.shared.plugins.PluginInstance$PluginFactory.createPluginContext()` and obtains the plugin's `ClassLoader` (`factory.pluginCtxRef.get().classLoader`).

Dispatch condition in `NewPluginHelperKt.kt`:
```kotlin
Triple(
    "CCGridForHyperOS",
    mPrefsMap.getBoolean("system_ui_control_center_rounded_rect") ||
        mPrefsMap.getBoolean("system_ui_control_center_custom_grid"),
    CCGridForHyperOSKt::initCCGridForHyperOS
)
```

---

## 3. Implementation Code: Hook Layer

### 3.1 `CCGridForHyperOSKt.kt`
**File Target**: `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/CCGridForHyperOSKt.kt`

```kotlin
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

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logE
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefsUtils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers

// from YunZiA & HyperCeiler Mod
object CCGridForHyperOSKt {
    private val radius by lazy {
        PrefsUtils.mPrefsMap.getInt("system_ui_control_center_rounded_rect_radius", 72).toFloat()
    }
    private val isCustomGrid by lazy {
        PrefsUtils.mPrefsMap.getBoolean("system_ui_control_center_custom_grid", false)
    }
    private val isRoundedRect by lazy {
        PrefsUtils.mPrefsMap.getBoolean("system_ui_control_center_rounded_rect", false)
    }

    @JvmStatic
    fun initCCGridForHyperOS(classLoader: ClassLoader?) {
        if (classLoader == null) return

        if (isCustomGrid) {
            initCCGridLayout(classLoader)
        }

        if (isRoundedRect) {
            initCCCornerRadius(classLoader)
        }
    }

    @JvmStatic
    fun initCCGridLayout(classLoader: ClassLoader) {
        val colsPortrait = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_columns", 4)
        val colsLandscape = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_columns_horizontal", 6)
        val marginH = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_tile_margin_h", 8)
        val marginV = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_tile_margin_v", 8)
        val paddingH = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_padding_h", 16)
        val paddingV = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_padding_v", 0)

        val targetClass = "miui.systemui.controlcenter.qs.tileview.QSTileListLayout"

        // Hook updateResources (called on init, orientation changes, and density/font updates)
        runCatching {
            XposedHelpers.findAndHookMethod(
                targetClass,
                classLoader,
                "updateResources",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? ViewGroup ?: return
                        applyGridLayout(view, colsPortrait, colsLandscape, marginH, marginV, paddingH, paddingV)
                        view.requestLayout()
                    }
                }
            )
        }.onFailure {
            logE("initCCGridForHyperOS", "QSTileListLayout updateResources hook failed: $it")
        }

        // Hook onMeasure (ensure columns and padding are applied before measurement)
        runCatching {
            XposedHelpers.findAndHookMethod(
                targetClass,
                classLoader,
                "onMeasure",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? ViewGroup ?: return
                        applyGridLayout(view, colsPortrait, colsLandscape, marginH, marginV, paddingH, paddingV)
                    }
                }
            )
        }.onFailure {
            logE("initCCGridForHyperOS", "QSTileListLayout onMeasure hook failed: $it")
        }

        // Optional getter hooks for ROM variants querying column count dynamically
        runCatching {
            XposedHelpers.findAndHookMethod(
                targetClass,
                classLoader,
                "getColumns",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? ViewGroup ?: return
                        val isPortrait = view.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                        param.result = if (isPortrait) colsPortrait else colsLandscape
                    }
                }
            )
        }

        runCatching {
            XposedHelpers.findAndHookMethod(
                targetClass,
                classLoader,
                "getColumnCount",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? ViewGroup ?: return
                        val isPortrait = view.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                        param.result = if (isPortrait) colsPortrait else colsLandscape
                    }
                }
            )
        }
    }

    private fun applyGridLayout(
        view: ViewGroup,
        colsPortrait: Int,
        colsLandscape: Int,
        marginHDp: Int,
        marginVDp: Int,
        paddingHDp: Int,
        paddingVDp: Int
    ) {
        runCatching {
            val resources = view.resources ?: return
            val density = resources.displayMetrics.density
            val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            val cols = if (isPortrait) colsPortrait else colsLandscape

            // 1. Set column count
            runCatching { XposedHelpers.setIntField(view, "mColumns", cols) }

            // 2. Set horizontal and vertical inter-tile spacing
            val marginHPx = (marginHDp * density).toInt()
            val marginVPx = (marginVDp * density).toInt()
            runCatching { XposedHelpers.setIntField(view, "mCellMarginHorizontal", marginHPx) }
            runCatching { XposedHelpers.setIntField(view, "mCellMarginVertical", marginVPx) }
            // Additional fallback field names for varied HyperOS builds
            runCatching { XposedHelpers.setIntField(view, "mTileMarginHorizontal", marginHPx) }
            runCatching { XposedHelpers.setIntField(view, "mTileMarginVertical", marginVPx) }
            runCatching { XposedHelpers.setIntField(view, "mTileMargin", marginHPx) }
            runCatching { XposedHelpers.setIntField(view, "mCellMargin", marginHPx) }

            // 3. Set container edge padding
            val padHPx = (paddingHDp * density).toInt()
            val padVPx = (paddingVDp * density).toInt()
            val currentTop = if (paddingVDp > 0) padVPx else view.paddingTop
            val currentBottom = if (paddingVDp > 0) padVPx else view.paddingBottom
            view.setPaddingRelative(padHPx, currentTop, padHPx, currentBottom)

            // Direct field reflection fallback for padding
            runCatching { XposedHelpers.setIntField(view, "mPaddingStart", padHPx) }
            runCatching { XposedHelpers.setIntField(view, "mPaddingEnd", padHPx) }
            if (paddingVDp > 0) {
                runCatching { XposedHelpers.setIntField(view, "mPaddingTop", padVPx) }
                runCatching { XposedHelpers.setIntField(view, "mPaddingBottom", padVPx) }
            }
        }.onFailure {
            logE("initCCGridForHyperOS", "applyGridLayout failed: $it")
        }
    }

    @JvmStatic
    fun initCCCornerRadius(classLoader: ClassLoader) {
        val targetClass = "miui.systemui.controlcenter.qs.tileview.QSTileItemIconView"

        XposedHelpers.findAndHookMethod(targetClass, classLoader, "setDisabledBg", Drawable::class.java, object : XC_MethodHook(){
            override fun beforeHookedMethod(param: MethodHookParam?) {
                runCatching {
                    val drawable = param?.args?.get(0) as Drawable
                    if (drawable is GradientDrawable) drawable.cornerRadius = radius
                    param.args[0] = drawable
                }.onFailure {
                    logE("initCCGridForHyperOS", "radius 1 crash, $it")
                }
            }
        })

        XposedHelpers.findAndHookMethod(targetClass, classLoader, "setEnabledBg", Drawable::class.java, object : XC_MethodHook(){
            override fun beforeHookedMethod(param: MethodHookParam?) {
                runCatching {
                    val drawable = param?.args?.get(0) as Drawable
                    if (drawable is GradientDrawable) drawable.cornerRadius = radius
                    param.args[0] = drawable
                }.onFailure {
                    logE("initCCGridForHyperOS", "radius 2 crash, $it")
                }
            }
        })

        // OS1 corner radius hook
        XposedHelpers.findAndHookMethod(targetClass,
            classLoader, "setCornerRadius", Float::class.java,
            object : XC_MethodHook(){
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    runCatching {
                        param?.args?.set(0, radius)
                    }.onFailure {
                        logE("initCCGridForHyperOS", "radius 3 crash, $it")
                    }
                }
            }
        )

        // OS2 corner radius getter hook
        runCatching {
            XposedHelpers.findAndHookMethod(targetClass,
                classLoader, "getCornerRadius", object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam?): Any {
                        return radius
                    }
                })
        }.onFailure {
            logE("initCCGridForHyperOS", "radius 4 crash, $it")
        }
    }
}
```

### 3.2 Diff for `NewPluginHelperKt.kt`
**File Target**: `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt`

```diff
--- a/library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt
+++ b/library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt
@@ -148,7 +148,8 @@
                     Triple(
                         "CCGridForHyperOS",
-                        mPrefsMap.getBoolean("system_ui_control_center_rounded_rect"),
+                        mPrefsMap.getBoolean("system_ui_control_center_rounded_rect") ||
+                            mPrefsMap.getBoolean("system_ui_control_center_custom_grid"),
                         CCGridForHyperOSKt::initCCGridForHyperOS
                     ),
                     Triple(
```

---

## 4. Implementation Code: Settings UI Layer

### 4.1 `system_ui_control_center_tiles.xml`
**File Target**: `library/common-ui/java/main/res/xml/system_ui_control_center_tiles.xml`

Insert the `<PreferenceCategory android:title="@string/system_ui_control_center_grid_layout_title">` before or inside `system_ui_control_center_tiles.xml`:

```xml
    <PreferenceCategory android:title="@string/system_ui_control_center_grid_layout_title">
        <SwitchPreference
            android:defaultValue="false"
            android:key="prefs_key_system_ui_control_center_custom_grid"
            android:title="@string/system_ui_control_center_custom_grid"
            android:summary="@string/system_ui_control_center_custom_grid_summary" />

        <SeekBarPreferenceCompat
            app:defaultValue="4"
            android:dependency="prefs_key_system_ui_control_center_custom_grid"
            android:key="prefs_key_system_ui_control_center_grid_columns"
            android:title="@string/system_control_center_qs_columns"
            app:defaultValueTitle="@string/array_default"
            app:format="@string/system_control_center_qs_columns_format"
            app:maxValue="7"
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

        <SeekBarPreferenceCompat
            app:defaultValue="0"
            android:dependency="prefs_key_system_ui_control_center_custom_grid"
            android:key="prefs_key_system_ui_control_center_grid_padding_v"
            android:title="@string/system_ui_control_center_grid_padding_v"
            app:defaultValueTitle="@string/array_default"
            app:format="%d dp"
            app:maxValue="40"
            app:minValue="0"
            app:showSeekBarValue="true"
            app:stepValue="2" />
    </PreferenceCategory>
```

### 4.2 `TileSettings.java`
**File Target**: `library/common-ui/java/main/src/com/sevtinge/hyperceiler/ui/hooker/systemui/TileSettings.java`

```java
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
package com.sevtinge.hyperceiler.ui.hooker.systemui;

import static com.sevtinge.hyperceiler.hook.utils.devicesdk.SystemSDKKt.getWhoAmI;
import static com.sevtinge.hyperceiler.hook.utils.shell.ShellUtils.rootExecCmd;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.sevtinge.hyperceiler.ui.R;
import com.sevtinge.hyperceiler.dashboard.DashboardFragment;
import com.sevtinge.hyperceiler.hook.utils.KillApp;
import com.sevtinge.hyperceiler.hook.utils.ThreadPoolManager;
import com.sevtinge.hyperceiler.hook.utils.devicesdk.TelephonyManager;
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefsUtils;

import fan.preference.DropDownPreference;
import fan.preference.SeekBarPreferenceCompat;

public class TileSettings extends DashboardFragment implements Preference.OnPreferenceChangeListener {
    SwitchPreference mTaplus;
    SwitchPreference mNewCCGridLabel;
    SwitchPreference mRoundedRect;
    SeekBarPreferenceCompat mRoundedRectRadius;
    SeekBarPreferenceCompat mSunshineModeHighBrightness;
    DropDownPreference mFiveG;
    DropDownPreference mSunshineMode;
    DropDownPreference mSunshineModeHigh;

    // R1: Grid & Padding preferences
    SwitchPreference mCustomGrid;
    SeekBarPreferenceCompat mGridColumns;
    SeekBarPreferenceCompat mGridColumnsHorizontal;
    SeekBarPreferenceCompat mTileMarginH;
    SeekBarPreferenceCompat mTileMarginV;
    SeekBarPreferenceCompat mGridPaddingH;
    SeekBarPreferenceCompat mGridPaddingV;

    Handler handler;

    int mMaxBrightness = 0;

    @Override
    public int getPreferenceScreenResId() {
        return R.xml.system_ui_control_center_tiles;
    }

    @Override
    public void initPrefs() {
        mFiveG = findPreference("prefs_key_system_control_center_5g_new_tile");
        mRoundedRect = findPreference("prefs_key_system_ui_control_center_rounded_rect");
        mRoundedRectRadius = findPreference("prefs_key_system_ui_control_center_rounded_rect_radius");
        mTaplus = findPreference("prefs_key_security_center_taplus");
        mNewCCGridLabel = findPreference("prefs_key_system_control_center_qs_tile_label");
        mSunshineMode = findPreference("prefs_key_system_control_center_sunshine_new_mode");
        mSunshineModeHigh = findPreference("prefs_key_system_control_center_sunshine_new_mode_high");
        mSunshineModeHighBrightness = findPreference("prefs_key_system_control_center_sunshine_mode_brightness");

        // R1: Grid & Padding bindings
        mCustomGrid = findPreference("prefs_key_system_ui_control_center_custom_grid");
        mGridColumns = findPreference("prefs_key_system_ui_control_center_grid_columns");
        mGridColumnsHorizontal = findPreference("prefs_key_system_ui_control_center_grid_columns_horizontal");
        mTileMarginH = findPreference("prefs_key_system_ui_control_center_tile_margin_h");
        mTileMarginV = findPreference("prefs_key_system_ui_control_center_tile_margin_v");
        mGridPaddingH = findPreference("prefs_key_system_ui_control_center_grid_padding_h");
        mGridPaddingV = findPreference("prefs_key_system_ui_control_center_grid_padding_v");

        handler = new Handler(Looper.getMainLooper());

        try {
            mMaxBrightness = Integer.parseInt(rootExecCmd("cat /sys/class/backlight/panel0-backlight/max_brightness"));
        } catch (Exception ignore) {}

        mTaplus.setOnPreferenceChangeListener(
                (preference, o) -> {
                    killTaplus();
                    return true;
                }
        );

        if (getWhoAmI().equals("root") && mMaxBrightness > 2048) {
            mSunshineModeHigh.setVisible(true);
            mSunshineMode.setVisible(false);
            mSunshineModeHigh.setOnPreferenceChangeListener(this);
            mSunshineModeHighBrightness.setMaxValue(mMaxBrightness);
        } else {
            mSunshineMode.setVisible(true);
            mSunshineModeHigh.setVisible(false);
            mSunshineModeHighBrightness.setVisible(false);
        }

        mRoundedRectRadius.setVisible(PrefsUtils.getSharedBoolPrefs(getContext(), "prefs_key_system_ui_control_center_rounded_rect", false));
        mNewCCGridLabel.setVisible(false);
        mFiveG.setVisible(TelephonyManager.getDefault().isFiveGCapable());
        mSunshineModeHighBrightness.setVisible(Integer.parseInt(PrefsUtils.mSharedPreferences.getString("prefs_key_system_control_center_sunshine_new_mode_high", "0")) == 3);

        mRoundedRect.setOnPreferenceChangeListener(this);

        if (mCustomGrid != null) {
            mCustomGrid.setOnPreferenceChangeListener(this);
            boolean isCustomGridEnabled = PrefsUtils.getSharedBoolPrefs(getContext(), "prefs_key_system_ui_control_center_custom_grid", false);
            setCanBeVisibleCustomGrid(isCustomGridEnabled);
        }
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object o) {
        if (preference == mRoundedRect) {
            setCanBeVisibleRoundedRect((Boolean) o);
        } else if (preference == mSunshineModeHigh) {
            setCanBeVisibleSunshineBrightness(Integer.parseInt((String) o));
        } else if (preference == mCustomGrid) {
            setCanBeVisibleCustomGrid((Boolean) o);
        }
        return true;
    }

    public void killTaplus() {
        ThreadPoolManager.getInstance().submit(() -> handler.post(() ->
                KillApp.killApps("com.miui.contentextension")));
    }

    private void setCanBeVisibleRoundedRect(boolean mode) {
        mRoundedRectRadius.setVisible(mode);
    }

    private void setCanBeVisibleSunshineBrightness(int mode) {
        mSunshineModeHighBrightness.setVisible(mode == 3);
    }

    private void setCanBeVisibleCustomGrid(boolean visible) {
        if (mGridColumns != null) mGridColumns.setVisible(visible);
        if (mGridColumnsHorizontal != null) mGridColumnsHorizontal.setVisible(visible);
        if (mTileMarginH != null) mTileMarginH.setVisible(visible);
        if (mTileMarginV != null) mTileMarginV.setVisible(visible);
        if (mGridPaddingH != null) mGridPaddingH.setVisible(visible);
        if (mGridPaddingV != null) mGridPaddingV.setVisible(visible);
    }
}
```

---

## 5. String Resources

### 5.1 English (`library/common-ui/java/main/res/values/strings_app.xml`)
```xml
    <!-- Control Center Grid & Spacing (R1) -->
    <string name="system_ui_control_center_grid_layout_title">Grid &amp; Spacing</string>
    <string name="system_ui_control_center_custom_grid">Customize grid layout</string>
    <string name="system_ui_control_center_custom_grid_summary">Customize columns, inter-tile margins, and edge padding for Control Center tiles</string>
    <string name="system_ui_control_center_tile_margin_h">Horizontal tile spacing</string>
    <string name="system_ui_control_center_tile_margin_v">Vertical tile spacing</string>
    <string name="system_ui_control_center_grid_padding_h">Container edge horizontal padding</string>
    <string name="system_ui_control_center_grid_padding_v">Container edge vertical padding</string>
```

### 5.2 Simplified Chinese (`library/common-ui/java/main/res/values-zh-rCN/strings_app.xml`)
```xml
    <!-- 控制中心网格与间距 (R1) -->
    <string name="system_ui_control_center_grid_layout_title">网格与间距</string>
    <string name="system_ui_control_center_custom_grid">自定义网格布局</string>
    <string name="system_ui_control_center_custom_grid_summary">自定义控制中心磁贴的列数、磁贴间距与容器边距</string>
    <string name="system_ui_control_center_tile_margin_h">磁贴水平间距</string>
    <string name="system_ui_control_center_tile_margin_v">磁贴垂直间距</string>
    <string name="system_ui_control_center_grid_padding_h">网格水平边距</string>
    <string name="system_ui_control_center_grid_padding_v">网格垂直边距</string>
```

---

## 6. Compatibility & Verification Points

1. **Orientation Dynamics**:
   - `updateResources()` detects portrait (`Configuration.ORIENTATION_PORTRAIT`) vs landscape (`ORIENTATION_LANDSCAPE`) and applies `colsPortrait` or `colsLandscape` before invoking `view.requestLayout()`.
2. **Measurement Pass Integrity**:
   - `onMeasure()` hook sets `mColumns`, `mCellMarginHorizontal`, `mCellMarginVertical`, and container edge padding *before* `super.onMeasure` executes, preventing layout jitter or broken wrapping without triggering recursive layout calls.
3. **ROM Resilience**:
   - All reflections and hooks use safe `runCatching` blocks and check alternate field names (`mCellMarginHorizontal`, `mTileMarginHorizontal`, `mTileMargin`) to gracefully handle variations across HyperOS 1.0 and HyperOS 2.0 builds.
