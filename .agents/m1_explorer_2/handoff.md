# Handoff Report: Milestone M1 (Requirement R1) — QSTileListLayout vs MiuiTileLayout, Multi-Orientation Geometry & Dynamic Reactivity

**Author**: Explorer 2 (`m1_explorer_2`)  
**Milestone**: M1 (Requirement R1: Adjustable Grid & Padding)  
**Date**: 2026-08-21  
**Project Root**: `/data/data/com.termux/files/home/HyperCeilerModded`  
**Target Recipient**: Milestone 1 Implementer / Orchestrator  

---

## 1. Observation

1. **ClassLoader & Package Partitioning**:
   - `com.android.systemui.qs.MiuiTileLayout` exists in the host SystemUI package (`com.android.systemui`) and is hooked via `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/QSGrid.kt` (lines 30–70).
   - `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` exists only within the dynamic plugin `miui.systemui.plugin` loaded via `PluginInstance$PluginFactory.createPluginContext()`, intercepted in `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt` (lines 58–71, 116–175).
   - `CCGridForHyperOSKt.kt` in `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/CCGridForHyperOSKt.kt` currently targets `QSTileItemIconView` for corner radius hooks in the plugin ClassLoader.

2. **Orientation & Measurement Geometry in `QSTileListLayout`**:
   - In HyperOS Control Center plugin, `QSTileListLayout` manages tile layout via internal fields:
     - `mColumns` (`int`): column count
     - `mCellMarginHorizontal` (`int`): horizontal space between adjacent tile items
     - `mCellMarginVertical` (`int`): vertical space between adjacent tile rows
   - During `onMeasure(int widthMeasureSpec, int heightMeasureSpec)`, `QSTileListLayout` calculates tile width:
     $$\text{tileWidth} = \frac{\text{totalWidth} - \text{paddingLeft} - \text{paddingRight} - (mColumns - 1) \times mCellMarginHorizontal}{mColumns}$$
   - During screen rotation, `updateResources()` is called, reading `resources.configuration.orientation` (`Configuration.ORIENTATION_PORTRAIT` vs `Configuration.ORIENTATION_LANDSCAPE`).

3. **Preferences & Real-Time Reactivity Framework**:
   - `PrefsMap` (`library/hook/src/main/java/com/sevtinge/hyperceiler/hook/utils/prefs/PrefsMap.java`) prepends `"prefs_key_"` to all preference keys queried.
   - `SharedPrefsProvider` (`library/hook/src/main/java/com/sevtinge/hyperceiler/hook/provider/SharedPrefsProvider.java`) broadcasts preference change URIs via `ContentResolver.notifyChange()`.
   - `PrefsChangeObserver` (`library/hook/src/main/java/com/sevtinge/hyperceiler/hook/utils/prefs/PrefsChangeObserver.java`) listens to ContentResolver notifications in the target process and immediately updates `mPrefsMap` when `autoApplyChange = true`.

---

## 2. Logic Chain

1. **Isolation of Legacy vs Modern QS**:
   - Because `MiuiTileLayout` (host SystemUI) and `QSTileListLayout` (plugin) reside in different packages, ClassLoaders, and UI layers, M1 grid hooks targeting `QSTileListLayout` in the plugin ClassLoader will not interfere with legacy `QSGrid` hooks on `MiuiTileLayout`.
2. **Multi-Form-Factor & Orientation Robustness**:
   - Hooking `updateResources()` in `QSTileListLayout` and dynamically injecting `mColumns` based on `Configuration.ORIENTATION_PORTRAIT` vs `Configuration.ORIENTATION_LANDSCAPE` ensures immediate adaptation during screen rotation.
   - Foldable unfolded screens and tablets (`sw600dp+`) can be configured with 5–6 portrait columns and 6–8 landscape columns, preventing tile distortion across diverse aspect ratios.
3. **Zero-Restart Preference Reactivity**:
   - Registering a `PrefsChangeObserver` within the layout's context and retaining a `WeakReference<ViewGroup>` to the active `QSTileListLayout` allows `onChange()` events to trigger `view.post { applyGridGeometry(view); view.requestLayout(); view.invalidate() }`.
   - This achieves instantaneous visual feedback without requiring `com.android.systemui` restart.

---

## 3. Caveats

1. **Foldable Dynamic Posture Switching**: When a foldable transitions from folded (cover screen) to unfolded (inner screen), `onConfigurationChanged` and `updateResources` are called by the system. The hook relies on `view.resources.displayMetrics` and `view.resources.configuration.orientation` to calculate DP dimensions.
2. **Label Visibility with High Column Counts**: If 6 columns are selected on narrow phone screens (e.g. 360dp width), tile labels under icons may truncate if tile text is too long; this is standard behavior and tile scale / margin sliders allow users to balance spacing.
3. **Plugin Re-instantiation**: If SystemUI unloads and reloads the plugin, `NewPluginHelperKt` will re-invoke `initCCGridForHyperOS(classLoader)`, and a new `WeakReference` will be established automatically.

---

## 4. Conclusion & Concrete Recommendations

### 4.1 Preference Key Schema
- **Master Switch**: `prefs_key_system_ui_control_center_custom_grid` (Boolean, default `false`)
- **Portrait Columns**: `prefs_key_system_ui_control_center_grid_columns` (Int, default `4`, min `3`, max `6`, step `1`)
- **Landscape Columns**: `prefs_key_system_ui_control_center_grid_columns_horizontal` (Int, default `6`, min `4`, max `8`, step `1`)
- **Horizontal Tile Margin**: `prefs_key_system_ui_control_center_tile_margin_h` (Int dp, default `8`, min `0`, max `30`, step `1`)
- **Vertical Tile Margin**: `prefs_key_system_ui_control_center_tile_margin_v` (Int dp, default `8`, min `0`, max `30`, step `1`)
- **Grid Edge Padding**: `prefs_key_system_ui_control_center_grid_padding_h` (Int dp, default `16`, min `0`, max `40`, step `2`)

### 4.2 Hook Placement
- Implement grid geometry logic and dynamic observer inside `CCGridForHyperOSKt.kt`.
- Dispatch via `NewPluginHelperKt.kt` under both `miui.systemui.controlcenter.MiuiControlCenter` and `miui.systemui.quicksettings.LocalMiuiQSTilePlugin`.

---

## 5. Verification Method

1. **Static / Build Verification**:
   - Run:
     ```bash
     proot -b /data/data/com.termux/files/usr/glibc/lib:/lib \
           -b /data/data/com.termux/files/usr/tmp:/tmp \
           env JAVA_HOME=/data/data/com.termux/files/home/jdk-17.0.12+7 \
               PATH=/data/data/com.termux/files/home/jdk-17.0.12+7/bin:/data/data/com.termux/files/usr/bin \
           ./gradlew assembleDebug
     ```
   - Exit code must be 0 (BUILD SUCCESSFUL).

2. **Functional Verification**:
   - Open HyperCeiler Settings -> Notification & Control Center -> QS Tile Settings.
   - Adjust column counts, tile spacing, and edge padding sliders.
   - Verify `PrefsChangeObserver` triggers layout updates dynamically without requiring SystemUI restart.
