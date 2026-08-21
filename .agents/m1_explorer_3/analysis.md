# UI Layer Architectural Specification: Adjustable Grid & Padding (Milestone M1 / Requirement R1)

**Author**: Explorer 3 (`m1_explorer_3`)  
**Milestone**: M1 (Requirement R1: Adjustable Grid & Padding)  
**Date**: 2026-08-21  
**Project**: HyperCeilerModded (`/data/data/com.termux/files/home/HyperCeilerModded`)

---

## 1. Executive Summary

This specification provides the comprehensive UI layer design, XML layout definitions, Java bindings (`TileSettings.java`), string resources (in English, Simplified Chinese, and Traditional Chinese), and conflict analysis for Milestone M1 (Requirement R1: Adjustable Grid & Padding).

All preference keys, XML structures, and attribute behaviors have been validated against HyperCeiler's custom preference framework (`SeekBarPreferenceCompat`, `SwitchPreference`, `DashboardFragment`, `PrefsMap`).

---

## 2. Preference Schema & Key Inventory

### 2.1 Key Definitions & Ranges

| Preference Key | Type | Default | Bounds / Step | Format | Dependency | Description |
|---|---|---|---|---|---|---|
| `prefs_key_system_ui_control_center_custom_grid` | Boolean | `false` | `true` / `false` | N/A | None | Master toggle for modern Control Center grid & padding customization |
| `prefs_key_system_ui_control_center_grid_columns` | Integer | `4` | `min="3"`, `max="6"`, `step="1"` | `@string/system_control_center_qs_columns_format` (`%d cols`) | `prefs_key_system_ui_control_center_custom_grid` | Small QS tile column count in Portrait orientation |
| `prefs_key_system_ui_control_center_grid_columns_horizontal` | Integer | `6` | `min="4"`, `max="8"`, `step="1"` | `@string/system_control_center_qs_columns_format` (`%d cols`) | `prefs_key_system_ui_control_center_custom_grid` | Small QS tile column count in Landscape orientation |
| `prefs_key_system_ui_control_center_tile_margin_h` | Integer | `8` (dp) | `min="0"`, `max="30"`, `step="1"` | `"%d dp"` | `prefs_key_system_ui_control_center_custom_grid` | Horizontal spacing between adjacent small QS tiles |
| `prefs_key_system_ui_control_center_tile_margin_v` | Integer | `8` (dp) | `min="0"`, `max="30"`, `step="1"` | `"%d dp"` | `prefs_key_system_ui_control_center_custom_grid` | Vertical spacing between adjacent small QS tile rows |
| `prefs_key_system_ui_control_center_grid_padding_h` | Integer | `16` (dp) | `min="0"`, `max="40"`, `step="2"` | `"%d dp"` | `prefs_key_system_ui_control_center_custom_grid` | Left & right container edge padding for the QS tile list layout |

### 2.2 Conflict & Collision Analysis
- **Namespace check**: All keys are prefixed with `prefs_key_system_ui_control_center_`.
- **Legacy isolation**: The legacy MIUI/HyperOS 1 QS grid keys (`prefs_key_system_control_center_old_*`) in `system_ui_control_center.xml` are completely distinct and do not collide.
- **Tiles screen check**: Checked `library/common-ui/java/main/res/xml/system_ui_control_center_tiles.xml` for all 22 existing keys. No key, ID, or title collision was found.
- **Global repo check**: Scanned entire `library/common-ui/` and `library/hook/` to guarantee uniqueness.

---

## 3. Preference XML Specification

### 3.1 Target File
`library/common-ui/java/main/res/xml/system_ui_control_center_tiles.xml`

### 3.2 Exact XML Preference Category Block

```xml
    <PreferenceCategory android:title="@string/system_ui_control_center_grid_layout_title">
        <SwitchPreference
            android:defaultValue="false"
            android:key="prefs_key_system_ui_control_center_custom_grid"
            android:summary="@string/system_ui_control_center_custom_grid_desc"
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

### 3.3 Insertion Location
Insert as a distinct `<PreferenceCategory>` in `system_ui_control_center_tiles.xml` directly preceding the third category (i.e. before `<Preference android:fragment="com.sevtinge.hyperceiler.ui.hooker.systemui.CardTileSettings"...`), creating a clear grouping for grid and tile dimensions.

---

## 4. Java Settings Binding Specification

### 4.1 Target File
`library/common-ui/java/main/src/com/sevtinge/hyperceiler/ui/hooker/systemui/TileSettings.java`

### 4.2 Field Declarations
```java
    SwitchPreference mCustomGrid;
    SeekBarPreferenceCompat mGridColumns;
    SeekBarPreferenceCompat mGridColumnsHorizontal;
    SeekBarPreferenceCompat mTileMarginH;
    SeekBarPreferenceCompat mTileMarginV;
    SeekBarPreferenceCompat mGridPaddingH;
```

### 4.3 `initPrefs()` Binding Code
```java
        mCustomGrid = findPreference("prefs_key_system_ui_control_center_custom_grid");
        mGridColumns = findPreference("prefs_key_system_ui_control_center_grid_columns");
        mGridColumnsHorizontal = findPreference("prefs_key_system_ui_control_center_grid_columns_horizontal");
        mTileMarginH = findPreference("prefs_key_system_ui_control_center_tile_margin_h");
        mTileMarginV = findPreference("prefs_key_system_ui_control_center_tile_margin_v");
        mGridPaddingH = findPreference("prefs_key_system_ui_control_center_grid_padding_h");
```

### 4.4 Full Proposed `TileSettings.java`
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

    SwitchPreference mCustomGrid;
    SeekBarPreferenceCompat mGridColumns;
    SeekBarPreferenceCompat mGridColumnsHorizontal;
    SeekBarPreferenceCompat mTileMarginH;
    SeekBarPreferenceCompat mTileMarginV;
    SeekBarPreferenceCompat mGridPaddingH;

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

        mCustomGrid = findPreference("prefs_key_system_ui_control_center_custom_grid");
        mGridColumns = findPreference("prefs_key_system_ui_control_center_grid_columns");
        mGridColumnsHorizontal = findPreference("prefs_key_system_ui_control_center_grid_columns_horizontal");
        mTileMarginH = findPreference("prefs_key_system_ui_control_center_tile_margin_h");
        mTileMarginV = findPreference("prefs_key_system_ui_control_center_tile_margin_v");
        mGridPaddingH = findPreference("prefs_key_system_ui_control_center_grid_padding_h");

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
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object o) {
        if (preference == mRoundedRect) {
            setCanBeVisibleRoundedRect((Boolean) o);
        } else if (preference == mSunshineModeHigh) {
            setCanBeVisibleSunshineBrightness(Integer.parseInt((String) o));
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
}
```

---

## 5. String Resources Specification

### 5.1 English / Default (`library/common-ui/java/main/res/values/strings_app.xml`)

```xml
    <string name="system_ui_control_center_grid_layout_title">Grid &amp; spacing</string>
    <string name="system_ui_control_center_custom_grid">Custom grid &amp; padding</string>
    <string name="system_ui_control_center_custom_grid_desc">Customize columns, inter-tile spacing, and container edge padding for QS tiles</string>
    <string name="system_ui_control_center_tile_margin_h">Horizontal tile spacing</string>
    <string name="system_ui_control_center_tile_margin_v">Vertical tile spacing</string>
    <string name="system_ui_control_center_grid_padding_h">Edge horizontal padding</string>
```

*Note*: The existing strings `@string/system_control_center_qs_columns` ("Columns"), `@string/system_control_center_qs_columns_horizontal` ("Columns (Horizontal)"), `@string/system_control_center_qs_columns_format` ("%d cols"), and `@string/array_default` ("Default") are already defined in the English base file and will be reused.

### 5.2 Simplified Chinese (`library/common-ui/java/main/res/values-zh-rCN/strings_app.xml`)

```xml
    <string name="system_ui_control_center_grid_layout_title">网格与间距</string>
    <string name="system_ui_control_center_custom_grid">自定义网格与边距</string>
    <string name="system_ui_control_center_custom_grid_desc">自定义控制中心磁贴列数、磁贴间距及容器边距</string>
    <string name="system_ui_control_center_tile_margin_h">磁贴水平间距</string>
    <string name="system_ui_control_center_tile_margin_v">磁贴垂直间距</string>
    <string name="system_ui_control_center_grid_padding_h">网格两边边距</string>
```

### 5.3 Traditional Chinese (`library/common-ui/java/main/res/values-zh-rHK/strings_app.xml` & `values-zh-rTW/strings_app.xml`)

```xml
    <string name="system_ui_control_center_grid_layout_title">網格與間距</string>
    <string name="system_ui_control_center_custom_grid">自訂網格與邊距</string>
    <string name="system_ui_control_center_custom_grid_desc">自訂控制中心磁貼列數、磁貼間距及容器邊距</string>
    <string name="system_ui_control_center_tile_margin_h">磁貼水平間距</string>
    <string name="system_ui_control_center_tile_margin_v">磁貼垂直間距</string>
    <string name="system_ui_control_center_grid_padding_h">網格兩邊邊距</string>
```

---

## 6. Compatibility & Interaction with M3 (Custom Shapes & Sizes)

When Milestone M3 implements Corner Radius (`system_ui_control_center_tile_radius`) and Scale (`system_ui_control_center_tile_scale`), they can be added either into this same `PreferenceCategory` or as adjacent sliders within `system_ui_control_center_tiles.xml`:
- `system_ui_control_center_tile_radius`: `min="0"`, `max="50"`, `defaultValue="72"` (or default dp), `format="%d dp"`
- `system_ui_control_center_tile_scale`: `min="50"`, `max="150"`, `defaultValue="100"`, `format="%d %%"`
- Both will naturally co-exist under `TileSettings.java` without interfering with `QSTileListLayout` container measurements.

---

## 7. Verification Method

1. **Static XML Verification**:
   - Inspect `library/common-ui/java/main/res/xml/system_ui_control_center_tiles.xml` to ensure valid XML tags, schema attributes, and resource references.
2. **Java Class Verification**:
   - Verify `TileSettings.java` compiles without unused imports or type errors with `./gradlew compileDebugJavaWithJavac`.
3. **Strings Verification**:
   - Verify that all XML `@string/...` references exist in `strings_app.xml` (base) and `values-zh-rCN/strings_app.xml`.
4. **End-to-End Build Verification**:
   - Run `./gradlew assembleDebug` to verify complete resource merge and compilation.
