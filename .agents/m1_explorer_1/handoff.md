# Handoff Report — Milestone M1 (Requirement R1: Adjustable Grid & Padding)

**Author**: Explorer 1 (`m1_explorer_1`)  
**Target Milestone**: M1 (Requirement R1: Adjustable Grid & Padding)  
**Date**: 2026-08-21  
**Artifact Path**: `/data/data/com.termux/files/home/HyperCeilerModded/.agents/m1_explorer_1/analysis.md`

---

## 1. Observation

1. **Dynamic Plugin Architecture**:
   - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt` (lines 58-71, 116-175) captures the plugin classloader for `miui.systemui.controlcenter.MiuiControlCenter` and `miui.systemui.quicksettings.LocalMiuiQSTilePlugin`.
   - Lines 149-152 in `NewPluginHelperKt.kt` currently only load `CCGridForHyperOSKt` if `mPrefsMap.getBoolean("system_ui_control_center_rounded_rect")` is true.

2. **Existing Grid & Corner Radius Hooks**:
   - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/CCGridForHyperOSKt.kt` (lines 30-114) targets `miui.systemui.controlcenter.qs.tileview.QSTileItemIconView` for corner radius (`setDisabledBg`, `setEnabledBg`, `setCornerRadius`, `getCornerRadius`).
   - `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` is the container ViewGroup that calculates tile item widths, rows, columns (`mColumns`), horizontal margin (`mCellMarginHorizontal`), vertical margin (`mCellMarginVertical`), and padding (`setPaddingRelative`).

3. **Preference & Settings Structure**:
   - Preferences UI for Control Center tiles is located in `library/common-ui/java/main/res/xml/system_ui_control_center_tiles.xml` (lines 107-170).
   - Fragment controller is `library/common-ui/java/main/src/com/sevtinge/hyperceiler/ui/hooker/systemui/TileSettings.java` (lines 41-125), implementing `Preference.OnPreferenceChangeListener` and dynamic visibility toggling via `setVisible()`.
   - String localization files are in `library/common-ui/java/main/res/values/strings_app.xml` and `library/common-ui/java/main/res/values-zh-rCN/strings_app.xml`.

---

## 2. Logic Chain

1. **Plugin Classloader Hook Requirement**:
   - In HyperOS, the modern Control Center views reside inside `miui.systemui.plugin`. SystemUI's host classloader cannot resolve `miui.systemui.controlcenter.qs.tileview.QSTileListLayout`.
   - Therefore, `NewPluginHelperKt.kt` must trigger `CCGridForHyperOSKt.initCCGridForHyperOS` when either `system_ui_control_center_rounded_rect` OR `system_ui_control_center_custom_grid` is enabled.

2. **Grid Geometry & Layout Hooking**:
   - `QSTileListLayout.updateResources()` is called during initial view creation and whenever configuration changes (e.g. orientation rotation, density change). Overriding `mColumns`, `mCellMarginHorizontal`, `mCellMarginVertical`, and container edge padding inside `updateResources` (after) and calling `view.requestLayout()` ensures re-measurement.
   - `QSTileListLayout.onMeasure(int, int)` computes child widths using `(availableWidth - (mColumns - 1) * mCellMarginHorizontal) / mColumns`. Hooking `onMeasure` (before) ensures the custom column count, margins, and padding are applied right before calculation without triggering recursive layout calls.

3. **UI Preferences & Bindings**:
   - In `system_ui_control_center_tiles.xml`, a new `PreferenceCategory` containing master switch `prefs_key_system_ui_control_center_custom_grid`, columns seekbar (`prefs_key_system_ui_control_center_grid_columns`), landscape columns seekbar (`prefs_key_system_ui_control_center_grid_columns_horizontal`), horizontal margin seekbar (`prefs_key_system_ui_control_center_tile_margin_h`), vertical margin seekbar (`prefs_key_system_ui_control_center_tile_margin_v`), and edge padding seekbars (`prefs_key_system_ui_control_center_grid_padding_h`, `prefs_key_system_ui_control_center_grid_padding_v`) provides full user control.
   - `TileSettings.java` manages visibility dynamically on preference change, matching the existing `mRoundedRect` pattern.

---

## 3. Caveats

1. **Field Name Variations in Unofficial ROMs**:
   - Some custom ROMs or minor HyperOS builds may rename `mCellMarginHorizontal` to `mTileMarginHorizontal` or `mTileMargin`. The proposed implementation handles this with multiple fallback `runCatching` assignments.
2. **Infinite Layout Loop Prevention**:
   - `view.requestLayout()` must ONLY be called in `updateResources()` (after), never in `onMeasure()` (before), to prevent `StackOverflowError` / infinite layout loop cycles.

---

## 4. Conclusion

The design and code implementation for Milestone M1 (Requirement R1: Adjustable Grid & Padding) are completely specified, verified against HyperOS layout mechanics, and documented with ready-to-apply snippets in `.agents/m1_explorer_1/analysis.md`.

Target files for implementation:
1. `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/CCGridForHyperOSKt.kt`
2. `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt`
3. `library/common-ui/java/main/res/xml/system_ui_control_center_tiles.xml`
4. `library/common-ui/java/main/src/com/sevtinge/hyperceiler/ui/hooker/systemui/TileSettings.java`
5. `library/common-ui/java/main/res/values/strings_app.xml` & `values-zh-rCN/strings_app.xml`

---

## 5. Verification Method

1. **Static Analysis & Inspection**:
   - Inspect `.agents/m1_explorer_1/analysis.md` to confirm all imports, class names, method names, preference keys, and XML tags match HyperCeiler architecture.
2. **Build Verification**:
   - Run `./gradlew compileReleaseJavaWithJavac` and `./gradlew compileReleaseKotlin` or `./gradlew build` to confirm syntactic and build correctness upon implementation.
