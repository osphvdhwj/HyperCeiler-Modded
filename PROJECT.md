# Project: iOS-Style Control Center for HyperOS in HyperCeiler

## Architecture
HyperCeiler is an Xposed module for HyperOS / MIUI. The Control Center modifications span two execution contexts:
1. **SystemUI Host Process (`com.android.systemui`)**:
   - Initialized via `XposedInit` -> `DataBase` -> `SystemUiV.java` (Android 15 / HyperOS 2) & `SystemUiU.java` (Android 14 / HyperOS 1).
   - Manages top status bar, carrier/flare views, data usage headers (`ControlCenterStatusBar`, `MiuiQSHeaderView`), and system-level IPC with `NetworkStatsManager`.
2. **SystemUI Dynamic Plugin (`miui.systemui.plugin`)**:
   - Loaded dynamically by SystemUI and intercepted via `NewPluginHelperKt.kt` (`PluginInstance$PluginFactory.createPluginContext()`).
   - Obtains the plugin `ClassLoader` for `miui.systemui.controlcenter.MiuiControlCenter`.
   - Manages tile layouts (`QSTileListLayout`), tile icons (`QSTileItemIconView`), card layouts (`QSCardItemView`), and card controllers (`QSController`, `QSCardsController`).
3. **Settings App UI & Preferences**:
   - Preferences UI defined in `library/common-ui/res/xml/` (`system_ui_control_center.xml`, `system_ui_control_center_tiles.xml`).
   - Settings fragments in `library/common-ui/src/.../ui/hooker/systemui/` (`ControlCenterSettings.java`, `TileSettings.java`).
   - SharedPreferences bridged to hooks via `SharedPrefsProvider` and queried with `PrefsMap.java` (keys prefixed with `prefs_key_`).

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | R1-Grid-Columns | Customizable grid columns (default 4, horizontal) for small QS tiles in Control Center | M1 | ORIGINAL_REQUEST §R1 |
| 2 | R1-Tile-Padding | Customizable horizontal and vertical spacing between small QS tiles | M1 | ORIGINAL_REQUEST §R1 |
| 3 | R1-Edge-Padding | Customizable container edge margins/padding for the tile list layout | M1 | ORIGINAL_REQUEST §R1 |
| 4 | R1-Settings-UI | XSharedPreferences toggles and UI sliders in HyperCeiler Tile Settings | M1 | ORIGINAL_REQUEST §R1 |
| 5 | R2-Dual-Data-Text | Formatted data usage text ("Mobile: X \| WiFi: Y") queried via NetworkStatsManager | M2 | ORIGINAL_REQUEST §R2 |
| 6 | R2-Header-Hook | Hook ControlCenterStatusBar and MiuiQSHeaderView to inject dual data usage | M2 | ORIGINAL_REQUEST §R2 |
| 7 | R2-Security-Intent | Long-press on data usage text launches Security app data stats activity | M2 | ORIGINAL_REQUEST §R2 |
| 8 | R2-Settings-UI | Preferences switch in HyperCeiler Control Center Settings | M2 | ORIGINAL_REQUEST §R2 |
| 9 | R3-Corner-Radius | Customizable corner radius (shape) for small QS tile icons | M3 | ORIGINAL_REQUEST §R3 |
| 10 | R3-Tile-Scale | Customizable scale factor (size) for small QS tile icons | M3 | ORIGINAL_REQUEST §R3 |
| 11 | R3-Settings-UI | Sliders in HyperCeiler Tile Settings for Shape (Radius) and Scale | M3 | ORIGINAL_REQUEST §R3 |
| 12 | R4-Connectivity-Platter | Predefined composite platter grouping WiFi, Bluetooth, Mobile Data | M4 | ORIGINAL_REQUEST §R4 |
| 13 | R4-Expandable-Folder | Expandable modal dialog / folder on long-press showing Airplane mode, Hotspot, and detailed status | M4 | ORIGINAL_REQUEST §R4 |
| 14 | R4-Settings-UI | Toggle switch in Control Center Settings to enable iOS Connectivity Platter | M4 | ORIGINAL_REQUEST §R4 |
| 15 | E2E-Verification | Full Gradle compilation, unit/integration verification, and hook sanity checks | M5 | Acceptance Criteria |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | M1: Adjustable Grid & Padding | Implement QSTileListLayout column/margin hooks + UI sliders | none | DONE |
| 2 | M2: Dual Data Usage Panel | Implement data usage query, header hooks, security intent + UI toggle | none | DONE |
| 3 | M3: Custom Shapes & Sizes | Implement QSTileItemIconView corner radius & scale hooks + UI sliders | M1 | DONE |
| 4 | M4: iOS-Style Connectivity Platter | Implement Connectivity Platter card, expansion popup, tile actions + UI toggle | M1, M3 | DONE |
| 5 | M5: E2E Integration & Build Verification | Full project build compilation, hook integrity check, and test validation | M1, M2, M3, M4 | DONE |

## Interface Contracts

### M1 ↔ M3 (Grid & Tile Layouts)
- M1 hooks `QSTileListLayout` for container geometry (columns, margins, padding).
- M3 hooks `QSTileItemIconView` / `QSTileItemView` for individual child item geometry (radius, scale).
- Preferences keys:
  - `system_ui_control_center_custom_grid` (Boolean)
  - `system_ui_control_center_grid_columns` (Int, default 4, range 3..6)
  - `system_ui_control_center_tile_margin_h` (Int, dp, range 0..30)
  - `system_ui_control_center_tile_margin_v` (Int, dp, range 0..30)
  - `system_ui_control_center_grid_padding_h` (Int, dp, range 0..40)
  - `system_ui_control_center_tile_radius` (Int, dp, range 0..50)
  - `system_ui_control_center_tile_scale` (Int, percentage 50..150, default 100)

### M2 (Data Usage IPC & Security Intent)
- Hook target: `com.android.systemui.controlcenter.phone.widget.ControlCenterStatusBar` and `com.android.systemui.qs.MiuiQSHeaderView`.
- Intent target: Component `com.miui.securitycenter/com.miui.networkassistant.ui.activity.MainActivity`, Action `miui.intent.action.NETWORKASSISTANT_MAIN` with `Intent.FLAG_ACTIVITY_NEW_TASK`.
- Preference key: `system_ui_control_center_dual_data_usage` (Boolean).

### M4 (Connectivity Platter & Card Interception)
- Hook target: `miui.systemui.controlcenter.qs.QSController` & `miui.systemui.controlcenter.qs.tileview.QSCardItemView`.
- Platter container replaces or prepends standard WiFi/Cell card tiles with custom 2x2 platter view.
- Long-press spawns `ConnectivityExpandDialog` containing expanded connectivity options (WiFi, Mobile Data, Bluetooth, Airplane Mode, Personal Hotspot, NFC/VPN).
- Preference key: `system_ui_control_center_ios_connectivity_platter` (Boolean).

## Code Layout
- `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/`:
  - `CCGridForHyperOSKt.kt`: Layout & grid hooks (M1 & M3)
  - `DualDataUsageHook.kt`: Dual data usage calculation, header hook, and intent dispatch (M2)
  - `ConnectivityPlatterHook.kt`: iOS Connectivity platter and expandable folder (M4)
- `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt`:
  - Plugin classloader hook dispatch registry
- `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/SystemUiV.java` & `SystemUiU.java`:
  - SystemUI host hook registrations
- `library/common-ui/java/main/res/xml/`:
  - `system_ui_control_center.xml`: Control Center general settings (M2, M4)
  - `system_ui_control_center_tiles.xml`: Tile specific settings (M1, M3)
- `library/common-ui/java/main/res/values/strings_app.xml`:
  - String resources for all new preferences
- `library/common-ui/java/main/src/com/sevtinge/hyperceiler/ui/hooker/systemui/`:
  - `ControlCenterSettings.java`: Settings binding for M2, M4
  - `TileSettings.java`: Settings binding for M1, M3
