# Handoff Report — Survey Explorer 2 (Control Center Hooks: R1 & R3)

## 1. Observation
- **Plugin Loader & Entry Point**:
  - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt` (lines 58-71, 116-175): Hooks `com.android.systemui.shared.plugins.PluginInstance$PluginFactory.createPluginContext` to intercept `miui.systemui.controlcenter.MiuiControlCenter` and `miui.systemui.quicksettings.LocalMiuiQSTilePlugin`, extracting the plugin's `ClassLoader`.
- **Existing Shape & Corner Radius Hooks**:
  - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/CCGridForHyperOSKt.kt` (lines 30-114): Contains `CCGridForHyperOSKt.initCCGridForHyperOS(classLoader)` which hooks `miui.systemui.controlcenter.qs.tileview.QSTileItemIconView` methods:
    - `setDisabledBg(Drawable)`
    - `setEnabledBg(Drawable)`
    - `setCornerRadius(Float)`
    - `getCornerRadius()`
    - (Commented out): `getActiveBackgroundDrawable`, `getDisabledBackgroundDrawable`, `getBackgroundDrawable`.
  - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/QSColor.java` (lines 43-81, 98-135): Hooks `QSTileItemIconView` and `QSCardItemView.setCornerRadius(float)`.
  - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/QsTileSuperBlur.java` (lines 69-122): Hooks `miui.systemui.controlcenter.qs.tileview.QSTileItemView`.
- **Existing Classic Grid Hooks**:
  - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/QSGrid.kt` (lines 29-72): Hooks `com.android.systemui.qs.MiuiTileLayout` to alter `mColumns` and `mMaxAllowedRows`.
  - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/QQSGrid.kt` (lines 29-47): Hooks `com.android.systemui.qs.MiuiQuickQSPanel.setMaxTiles`.
- **UI Settings & Preferences**:
  - `library/common-ui/java/main/res/xml/system_ui_control_center.xml` (lines 117-145, 147-227): Controls main Control Center settings; links to `TileSettings` via `prefs_key_system_ui_control_center_tile_settings`.
  - `library/common-ui/java/main/res/xml/system_ui_control_center_tiles.xml` (lines 113-129): Contains `prefs_key_system_ui_control_center_rounded_rect` and `prefs_key_system_ui_control_center_rounded_rect_radius`.
  - `library/common-ui/java/main/src/com/sevtinge/hyperceiler/ui/hooker/systemui/TileSettings.java` (lines 41-125): Binds UI preferences and sets visibility for `mRoundedRectRadius`.
  - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/utils/prefs/PrefsMap.java` (lines 25-58): Implements automatic `"prefs_key_"` prefix resolution for all hook preference lookups.

## 2. Logic Chain
1. In HyperOS, Control Center is modularized into `miui.systemui.plugin`. Therefore, any hooks modifying modern Control Center layouts (such as `QSTileListLayout` and `QSTileItemIconView`) must be injected into the plugin `ClassLoader` provided by `NewPluginHelperKt`.
2. For Requirement R1 (Adjustable Grid & Padding):
   - Modern HyperOS Control Center arranges small QS tiles via `miui.systemui.controlcenter.qs.tileview.QSTileListLayout`.
   - Modifying `mColumns` in `updateResources()`/`onMeasure()` allows dynamic column counts (portrait vs landscape).
   - Modifying `mCellMarginHorizontal`/`mCellMarginVertical` adjusts inter-tile spacing.
   - Calling `setPadding(...)` on `QSTileListLayout` adjusts container edge padding.
3. For Requirement R3 (Custom Shapes & Sizes):
   - `miui.systemui.controlcenter.qs.tileview.QSTileItemIconView` renders the small QS tile shapes and icons.
   - Adjusting `setCornerRadius`, `getCornerRadius`, `setDisabledBg`, `setEnabledBg`, and background gradient drawables dynamically controls the tile corner radius (shape).
   - Hooking `updateIcon(...)` or `onLayout` on `QSTileItemIconView` to apply `view.setScaleX(scale)` and `view.setScaleY(scale)` dynamically adjusts tile scale (size).
4. For UI Integration:
   - `TileSettings.java` and `system_ui_control_center_tiles.xml` are the dedicated location in HyperCeiler for QS tile customization (already housing tile shape, colors, and blur).
   - Adding `SeekBarPreferenceCompat` elements for columns, tile padding, edge padding, and tile scale here follows the exact UI hierarchy and architectural pattern of HyperCeiler.

## 3. Caveats
- Android resource dimensions in `QSTileListLayout` may be cached across configuration changes; invoking `view.requestLayout()` after setting padding and columns ensures proper re-measurement.
- In HyperOS 2.0, certain VoWiFi or Card tile layouts may interact with the small tile grid; keeping VoWiFi hooks in `CustomCardTiles` compatible ensures no collision.
- No source code files outside of `.agents/` were modified during this investigation (strictly read-only).

## 4. Conclusion
- The target hook architecture for R1 and R3 is well-defined and integrates directly into `NewPluginHelperKt` and `CCGridForHyperOSKt.kt`.
- Exact target classes:
  - R1: `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` (plugin ClassLoader) and `com.android.systemui.qs.MiuiTileLayout` (SystemUI ClassLoader).
  - R3: `miui.systemui.controlcenter.qs.tileview.QSTileItemIconView` and `miui.systemui.controlcenter.qs.tileview.QSTileItemView`.
- UI preference additions belong in `system_ui_control_center_tiles.xml` and `TileSettings.java`, backed by `SeekBarPreferenceCompat` and string resources in `strings_app.xml`.

## 5. Verification Method
- **File & Class Verification**:
  - `view_file` on `/data/data/com.termux/files/home/HyperCeilerModded/library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt` lines 116-175.
  - `view_file` on `/data/data/com.termux/files/home/HyperCeilerModded/library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/CCGridForHyperOSKt.kt`.
  - `view_file` on `/data/data/com.termux/files/home/HyperCeilerModded/library/common-ui/java/main/res/xml/system_ui_control_center_tiles.xml`.
  - `view_file` on `/data/data/com.termux/files/home/HyperCeilerModded/library/common-ui/java/main/src/com/sevtinge/hyperceiler/ui/hooker/systemui/TileSettings.java`.
- **Report Verification**:
  - Inspect `/data/data/com.termux/files/home/HyperCeilerModded/.agents/survey_explorer_2/survey_report.md` for full implementation specs.
