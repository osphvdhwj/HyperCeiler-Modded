# HyperCeiler Control Center Hooks Survey Report
**Focus Requirements**: R1 (Adjustable Grid & Padding) & R3 (Custom Shapes & Sizes)  
**Author**: Explorer 2 (survey_explorer_2)  
**Date**: 2026-08-21  
**Project**: HyperCeilerModded (/data/data/com.termux/files/home/HyperCeilerModded)

---

## 1. Executive Summary

This report documents the architectural findings, target classes, hook points, methods, fields, and UI integration points required to implement:
- **Requirement R1 (Adjustable Grid & Padding)**: Dynamic QS grid columns (portrait & landscape), inter-tile padding (horizontal & vertical), and edge padding.
- **Requirement R3 (Custom Shapes & Sizes)**: Global adjustment of tile corner radius (shape) and dynamic scaling (size) for all small QS tiles.

In Xiaomi's HyperOS (and MIUI), Control Center and Quick Settings are loaded dynamically via plugins (`miui.systemui.plugin`). HyperCeiler leverages Xposed hook infrastructure (`NewPluginHelperKt`) to intercept the plugin's `ClassLoader` and inject hook loaders targeting `miui.systemui.controlcenter` and `com.android.systemui`.

---

## 2. Architecture of Control Center in HyperCeiler

### 2.1 Plugin Loader Mechanism (`NewPluginHelperKt.kt`)
HyperOS encapsulates the modern Control Center into dynamic plugin packages. SystemUI loads these plugins using `PluginInstance$PluginFactory`.

- **Hook Injection Point**: `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt`
  - Hooks `com.android.systemui.shared.plugins.PluginInstance$PluginFactory.createPluginContext()`
  - Identifies target plugin component names:
    - `miui.systemui.controlcenter.MiuiControlCenter`
    - `miui.systemui.quicksettings.LocalMiuiQSTilePlugin`
  - Obtains the plugin's `ClassLoader` (`factory.pluginCtxRef.get().classLoader`)
  - Dispatches to specialized hook loaders (e.g. `CCGridForHyperOSKt`, `CustomCardTiles`, `QSColor`, `QsTileSuperBlur`).

### 2.2 SystemUI Base Hooks (`SystemUiV.java` / `SystemUiU.java`)
- For classic/legacy SystemUI QS views outside the plugin context, hooks are initialized directly in `SystemUiV.java` (`initHook(new QSGrid(), ...)`).
- HyperCeiler uses `PrefsMap` (`PrefsUtils.mPrefsMap`) which automatically prepends `"prefs_key_"` to look up preferences saved in `hyperceiler_prefs.xml` and synced via `SharedPrefsProvider`.

---

## 3. Investigation of Requirement R1: Adjustable Grid & Padding

### 3.1 Target Classes & Hierarchy
1. **Modern HyperOS Control Center Plugin**:
   - `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` (Container ViewGroup for small QS tiles in the plugin)
   - `miui.systemui.controlcenter.panel.main.qs.QSGridController` / `QSItemViewHolder`
2. **Classic / AOSP-based MIUI SystemUI**:
   - `com.android.systemui.qs.MiuiTileLayout`
   - `com.android.systemui.qs.MiuiPagedTileLayout`
   - `com.android.systemui.qs.MiuiQuickQSPanel`

### 3.2 Key Methods & Fields for R1

| Target Class | Method / Field | Type | Function & Hook Technique |
|---|---|---|---|
| `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` | `mColumns` | `int` (Field) | Number of columns in the grid. Hook `updateResources()` or `onMeasure()` to assign user-configured columns based on orientation. |
| `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` | `updateResources()` | `void` (Method) | Invoked upon configuration/density/orientation change. Hook `after` to override `mColumns`, item margins, and padding. |
| `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` | `onMeasure(int, int)` | `void` (Method) | Computes child tile width based on total width / `mColumns`. Hook `before` or `after` to inject adjusted column count, tile padding, and container edge padding. |
| `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` | `mCellMarginHorizontal` / `mCellMarginVertical` / `mTileMargin` | `int` / `float` (Fields) | Inter-tile horizontal/vertical spacing. Hook to override with custom pixel values from preferences. |
| `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` | `setPadding(int, int, int, int)` | `void` (Method) | Edge margins / container padding of the tile grid. Hook `onMeasure` or `updateResources` to call `viewGroup.setPadding(edgePadding, top, edgePadding, bottom)`. |
| `com.android.systemui.qs.MiuiTileLayout` | `layoutTileRecords(int)` | `void` (Method) | Legacy MIUI tile layout. Hooked in `QSGrid.kt` via `setObjectField("mColumns", cols)`. |
| `com.android.systemui.qs.MiuiTileLayout` | `mMaxAllowedRows` | `int` (Field) | Max rows for portrait/landscape. Set in `updateResources()` afterHook. |

### 3.3 Hook Implementation Strategy for R1
1. **Define XSharedPreferences Keys**:
   - `system_ui_control_center_grid_enable` (Boolean): Master toggle for custom HyperOS grid layout.
   - `system_ui_control_center_grid_columns` (Int, default 4, range 3..7): Portrait columns.
   - `system_ui_control_center_grid_columns_horizontal` (Int, default 6, range 4..8): Landscape columns.
   - `system_ui_control_center_grid_tile_padding` (Int, dp, default standard, range 0..32): Inter-tile spacing.
   - `system_ui_control_center_grid_edge_padding` (Int, dp, default standard, range 0..32): Edge container padding.

2. **Hook Implementation in Plugin Context (`CCGridForHyperOSKt.kt` or new `CCGridLayoutHook.kt`)**:
   ```kotlin
   fun initCCGridLayout(classLoader: ClassLoader) {
       val isGridCustom = mPrefsMap.getBoolean("system_ui_control_center_grid_enable")
       if (!isGridCustom) return

       val colsPortrait = mPrefsMap.getInt("system_ui_control_center_grid_columns", 4)
       val colsLandscape = mPrefsMap.getInt("system_ui_control_center_grid_columns_horizontal", 6)
       val tilePaddingDp = mPrefsMap.getInt("system_ui_control_center_grid_tile_padding", -1)
       val edgePaddingDp = mPrefsMap.getInt("system_ui_control_center_grid_edge_padding", -1)

       XposedHelpers.findAndHookMethod(
           "miui.systemui.controlcenter.qs.tileview.QSTileListLayout",
           classLoader,
           "updateResources",
           object : XC_MethodHook() {
               override fun afterHookedMethod(param: MethodHookParam) {
                   val view = param.thisObject as ViewGroup
                   val isPortrait = view.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                   val cols = if (isPortrait) colsPortrait else colsLandscape
                   
                   // Set columns
                   runCatching { XposedHelpers.setIntField(view, "mColumns", cols) }
                   
                   // Set padding between tiles
                   if (tilePaddingDp >= 0) {
                       val paddingPx = TypedValue.applyDimension(
                           TypedValue.COMPLEX_UNIT_DIP, tilePaddingDp.toFloat(), view.resources.displayMetrics
                       ).toInt()
                       runCatching { XposedHelpers.setIntField(view, "mCellMarginHorizontal", paddingPx) }
                       runCatching { XposedHelpers.setIntField(view, "mCellMarginVertical", paddingPx) }
                   }

                   // Set edge padding
                   if (edgePaddingDp >= 0) {
                       val edgePx = TypedValue.applyDimension(
                           TypedValue.COMPLEX_UNIT_DIP, edgePaddingDp.toFloat(), view.resources.displayMetrics
                       ).toInt()
                       view.setPadding(edgePx, view.paddingTop, edgePx, view.paddingBottom)
                   }
                   view.requestLayout()
               }
           }
       )
   }
   ```

---

## 4. Investigation of Requirement R3: Custom Shapes & Sizes

### 4.1 Target Classes & Hierarchy
1. **Small QS Tile View & Icon View**:
   - `miui.systemui.controlcenter.qs.tileview.QSTileItemIconView`: Renders the tile icon background and shape (contains gradient backgrounds, corner radius, state drawables).
   - `miui.systemui.controlcenter.qs.tileview.QSTileItemView`: The outer tile item layout containing the icon view and label container.
2. **Card (Big) Tile View**:
   - `miui.systemui.controlcenter.qs.tileview.QSCardItemView` & `QSCardItemIconView`.

### 4.2 Baseline Analysis of Existing Code
HyperCeiler already has basic corner radius hooks in `CCGridForHyperOSKt.kt`:
```kotlin
object CCGridForHyperOSKt {
    private val radius by lazy {
        PrefsUtils.mPrefsMap.getInt("system_ui_control_center_rounded_rect_radius", 72).toFloat()
    }
    // Hooks QSTileItemIconView:
    // - setDisabledBg(Drawable) -> if (drawable is GradientDrawable) drawable.cornerRadius = radius
    // - setEnabledBg(Drawable) -> if (drawable is GradientDrawable) drawable.cornerRadius = radius
    // - setCornerRadius(Float) -> param.args[0] = radius
    // - getCornerRadius() -> return radius
}
```
And `QSColor.java`:
- Hooks `QSTileItemIconView.getActiveBackgroundDrawable`, `getDisabledBackgroundDrawable`, `getBackgroundDrawable`.
- Hooks `QSCardItemView.setCornerRadius(float)`.

### 4.3 Key Methods & Fields for R3 (Corner Radius + Tile Scale)

| Target Class | Method | Hook Type | Purpose |
|---|---|---|---|
| `miui.systemui.controlcenter.qs.tileview.QSTileItemIconView` | `setCornerRadius(float)` | `beforeHookedMethod` | Injects custom corner radius float parameter. |
| `miui.systemui.controlcenter.qs.tileview.QSTileItemIconView` | `getCornerRadius()` | `replaceHookedMethod` | Returns custom corner radius float (used in HyperOS 2.0 layout calculations). |
| `miui.systemui.controlcenter.qs.tileview.QSTileItemIconView` | `setEnabledBg(Drawable)` / `setDisabledBg(Drawable)` | `beforeHookedMethod` | Ensures `GradientDrawable.cornerRadius = radius` when backgrounds are assigned dynamically. |
| `miui.systemui.controlcenter.qs.tileview.QSTileItemIconView` | `getActiveBackgroundDrawable(QSTile$State)` / `getDisabledBackgroundDrawable(...)` / `getBackgroundDrawable(...)` | `afterHookedMethod` | Overrides `drawable.cornerRadius` if drawable is `GradientDrawable`. |
| `miui.systemui.controlcenter.qs.tileview.QSTileItemIconView` | `updateIcon(...)` or `onLayout(...)` / constructor | `afterHookedMethod` | Applies dynamic scaling: `(view as View).scaleX = scale; (view as View).scaleY = scale`. |
| `miui.systemui.controlcenter.qs.tileview.QSTileItemView` | `onMeasure(int, int)` / `onLayout(...)` | `afterHookedMethod` | Adjusts child scaling or layout bounds if label spacing compensation is needed. |

### 4.4 Hook Implementation Strategy for R3
1. **Define XSharedPreferences Keys**:
   - `system_ui_control_center_rounded_rect` (Boolean): Enable custom shape / corner radius.
   - `system_ui_control_center_rounded_rect_radius` (Int, default 72, range 0..99): Corner radius value (0 = square, 72 = squircle, 99 = circular).
   - `system_ui_control_center_tile_scale` (Int, percentage, default 100, range 50..150): Scale factor (100% = 1.0f).

2. **Hook Implementation in `CCGridForHyperOSKt.kt`**:
   - Corner radius: Combine all existing `QSTileItemIconView` hooks (`setCornerRadius`, `getCornerRadius`, `setEnabledBg`, `setDisabledBg`, `getActiveBackgroundDrawable`, `getDisabledBackgroundDrawable`).
   - Scale factor:
     ```kotlin
     val scalePercent = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_tile_scale", 100)
     val scale = scalePercent / 100.0f
     
     if (scalePercent != 100) {
         XposedHelpers.findAndHookMethod(
             "miui.systemui.controlcenter.qs.tileview.QSTileItemIconView",
             classLoader,
             "updateIcon",
             "com.android.systemui.plugins.qs.QSTile\$State",
             Boolean::class.javaPrimitiveType,
             Boolean::class.javaPrimitiveType,
             object : XC_MethodHook() {
                 override fun afterHookedMethod(param: MethodHookParam) {
                     val view = param.thisObject as View
                     view.scaleX = scale
                     view.scaleY = scale
                     view.pivotX = view.width / 2f
                     view.pivotY = view.height / 2f
                 }
             }
         )
     }
     ```

---

## 5. UI Integration Points in HyperCeiler

### 5.1 Menu & Fragment Hierarchy
1. **Main Control Center Menu**:
   - **Fragment**: `com.sevtinge.hyperceiler.ui.hooker.systemui.ControlCenterSettings`
   - **XML**: `library/common-ui/java/main/res/xml/system_ui_control_center.xml`
   - **Entry in UI**: Homepage -> SystemUI (Notification & Control Center).
2. **Tile Customization Sub-Menu**:
   - **Fragment**: `com.sevtinge.hyperceiler.ui.hooker.systemui.TileSettings`
   - **XML**: `library/common-ui/java/main/res/xml/system_ui_control_center_tiles.xml`
   - **Entry in UI**: Control Center Settings -> "QS tile" (`prefs_key_system_ui_control_center_tile_settings`).

### 5.2 Settings Placement Plan

#### Place A: In `system_ui_control_center_tiles.xml` (Tile Settings)
Add preferences under a new or existing category:

```xml
<PreferenceCategory android:title="@string/system_ui_control_center_tile_shape_size_title">
    <!-- Corner Radius Shape -->
    <SwitchPreference
        android:defaultValue="false"
        android:key="prefs_key_system_ui_control_center_rounded_rect"
        android:title="@string/system_ui_control_center_rounded_rect" />

    <SeekBarPreferenceCompat
        app:defaultValue="72"
        android:dependency="prefs_key_system_ui_control_center_rounded_rect"
        android:key="prefs_key_system_ui_control_center_rounded_rect_radius"
        android:title="@string/system_ui_control_center_rounded_rect_radius"
        app:defaultValueTitle="@string/array_default"
        app:format="%d dp"
        app:maxValue="99"
        app:minValue="0"
        app:showSeekBarValue="true"
        app:stepValue="1" />

    <!-- Tile Scale (Size) -->
    <SeekBarPreferenceCompat
        app:defaultValue="100"
        android:key="prefs_key_system_ui_control_center_tile_scale"
        android:title="@string/system_ui_control_center_tile_scale"
        app:defaultValueTitle="@string/array_default"
        app:format="%d%%"
        app:maxValue="150"
        app:minValue="50"
        app:showSeekBarValue="true"
        app:stepValue="5" />
</PreferenceCategory>
```

#### Place B: In `system_ui_control_center.xml` or `system_ui_control_center_tiles.xml` (Grid & Padding)
Add preferences for the modern Control Center grid:

```xml
<PreferenceCategory android:title="@string/system_ui_control_center_grid_layout_title">
    <SwitchPreference
        android:defaultValue="false"
        android:key="prefs_key_system_ui_control_center_grid_enable"
        android:title="@string/system_ui_control_center_grid_enable" />

    <SeekBarPreferenceCompat
        app:defaultValue="4"
        android:dependency="prefs_key_system_ui_control_center_grid_enable"
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
        android:dependency="prefs_key_system_ui_control_center_grid_enable"
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
        android:dependency="prefs_key_system_ui_control_center_grid_enable"
        android:key="prefs_key_system_ui_control_center_grid_tile_padding"
        android:title="@string/system_ui_control_center_tile_padding"
        app:defaultValueTitle="@string/array_default"
        app:format="%d dp"
        app:maxValue="32"
        app:minValue="0"
        app:showSeekBarValue="true"
        app:stepValue="1" />

    <SeekBarPreferenceCompat
        app:defaultValue="16"
        android:dependency="prefs_key_system_ui_control_center_grid_enable"
        android:key="prefs_key_system_ui_control_center_grid_edge_padding"
        android:title="@string/system_ui_control_center_edge_padding"
        app:defaultValueTitle="@string/array_default"
        app:format="%d dp"
        app:maxValue="40"
        app:minValue="0"
        app:showSeekBarValue="true"
        app:stepValue="2" />
</PreferenceCategory>
```

### 5.3 Required String Resources to Add
In `library/common-ui/java/main/res/values/strings_app.xml` and `values-zh-rCN/strings_app.xml`:
- `system_ui_control_center_grid_layout_title`: "Grid & Spacing" / "网格与间距"
- `system_ui_control_center_grid_enable`: "Enable Custom Grid Layout" / "自定义网格布局"
- `system_ui_control_center_tile_padding`: "Tile Spacing" / "磁贴间距"
- `system_ui_control_center_edge_padding`: "Edge Padding" / "边缘边距"
- `system_ui_control_center_tile_shape_size_title`: "Tile Shape & Size" / "磁贴形状与大小"
- `system_ui_control_center_tile_scale`: "Tile Scale" / "磁贴缩放大小"

---

## 6. Compatibility & Technical Edge Cases

1. **HyperOS 1.0 vs HyperOS 2.0**:
   - In HyperOS 1.0, `setCornerRadius(float)` is widely called during tile initialization.
   - In HyperOS 2.0, `getCornerRadius()` is queried by the layout manager to measure and clip drawables.
   - Both methods must be hooked simultaneously (as established in `CCGridForHyperOSKt.kt`) to ensure flawless cross-version support.
2. **Orientation Handling**:
   - `QSTileListLayout` and `MiuiTileLayout` are recreated or undergo `updateResources()` / `onConfigurationChanged()` on screen rotation.
   - Reading `context.resources.configuration.orientation` in the `updateResources` hook ensures immediate adaptation between portrait and landscape column counts.
3. **Plugin Context & Timing**:
   - Because `miui.systemui.controlcenter` is a loaded plugin, all hooks targeting `QSTileListLayout` and `QSTileItemIconView` must be loaded inside `NewPluginHelperKt.onPluginLoaded()` using `factory.pluginCtxRef.get().classLoader`, rather than the base SystemUI classloader.
4. **Drawable State Preservation**:
   - QS tiles frequently transition states (Active = 2, Inactive = 1, Unavailable = 0).
   - Hooking both `setEnabledBg`, `setDisabledBg`, and `getActiveBackgroundDrawable` / `getDisabledBackgroundDrawable` ensures the custom corner radius is preserved throughout dynamic tile clicks and state changes.

---

## 7. Summary of Proposed Source Modifications for Implementation Phase

1. **Hook Layer**:
   - Update `CCGridForHyperOSKt.kt` (or create `CCGridMod.kt`) to incorporate:
     - `QSTileListLayout` column count, tile margin, and edge padding hooks.
     - `QSTileItemIconView` scale (`scaleX`, `scaleY`) and extended corner radius hooks.
   - Register loader in `NewPluginHelperKt.kt` under `miui.systemui.controlcenter.MiuiControlCenter` and `LocalMiuiQSTilePlugin`.
2. **UI Layer**:
   - Update `system_ui_control_center_tiles.xml` with grid columns, tile padding, edge padding, and tile scale preferences.
   - Update `TileSettings.java` with bindings and visibility dependencies.
   - Add localized string keys to `strings_app.xml` and `values-zh-rCN/strings_app.xml`.
