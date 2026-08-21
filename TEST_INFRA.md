# TEST_INFRA — Comprehensive End-to-End Test Architecture & Specification

## 1. Test Philosophy & Principles

The HyperCeiler Control Center testing framework is designed around five core engineering tenets:

1. **Deterministic Oracles & Observable Invariants**:
   Every test calculates expected mathematical, layout, formatting, or intent outputs from first-principles specifications defined in `PROJECT.md` and `ORIGINAL_REQUEST.md`. No test shall rely on unverified assumptions or fragile internal private states.

2. **Progressive Testability & Decoupled Verification**:
   Tests must be runnable across different stages of milestone implementation. Pure algorithmic models, preference translation logic, data formatting, and intent construction can be verified in isolation as well as in end-to-end integration flows.

3. **Adversarial & Boundary Rigor**:
   SystemUI and Control Center hooks execute in high-stress, real-time UI render loops. The test framework subject all inputs—preference bounds, screen densities, orientation shifts, multi-SIM data payloads, null contexts, and concurrent state transitions—to rigorous adversarial stress testing.

4. **Zero-Flakiness & Isolation**:
   Every test fixture sets up its own isolated memory state, mocking or simulating dependencies without cross-test leakage, and cleanly resets static registries.

5. **Multi-Environment Runner Support**:
   Tests can be executed via standard Gradle tasks (`./gradlew testDebugUnitTest` / `./gradlew check`) as well as via a dedicated, zero-dependency standalone CLI test runner harness (`tools/run_e2e_tests.sh`) optimized for fast developer feedback and CI execution in Termux/Linux environments.

---

## 2. Feature Inventory & Interface Contracts

| Requirement | Feature ID | Target Component / Hook Class | Preference Key(s) & Types | Contract / Observable Output |
|---|---|---|---|---|
| **R1: Adjustable Grid & Padding** | `R1-FEAT-01` (Columns) | `QSTileListLayout.updateResources()`, `MiuiTileLayout` | `system_ui_control_center_custom_grid` (Bool), `system_ui_control_center_grid_columns` (Int, 3..6, def 4) | Calculated column count clamped to [3..6]; Tile cell width dynamically derived: `(containerWidth - 2*paddingH - (cols-1)*marginH) / cols`. |
| | `R1-FEAT-02` (Tile Margin) | `QSTileListLayout.mCellMarginHorizontal`, `mCellMarginVertical` | `system_ui_control_center_tile_margin_h` (Int dp, 0..30), `system_ui_control_center_tile_margin_v` (Int dp, 0..30) | Horizontal and vertical inter-tile margins applied to child view measurement and row spacing. |
| | `R1-FEAT-03` (Edge Padding) | `QSTileListLayout.setPadding()` | `system_ui_control_center_grid_padding_h` (Int dp, 0..40) | Container left/right padding set dynamically on layout refresh. |
| | `R1-FEAT-04` (Preferences) | `TileSettings.java`, `system_ui_control_center_tiles.xml` | Prefix `prefs_key_` + keys above | Key resolution through `PrefsMap` with correct default fallbacks when keys are unconfigured. |
| **R2: Dual Data Usage Panel** | `R2-FEAT-01` (Query & Format) | `DualDataUsageHook.kt`, `NetworkStatsManager` | `system_ui_control_center_dual_data_usage` (Bool, def false) | Epoch calculation from start-of-day (00:00:00.000) to current time. Formatted output string matching `"Mobile: <size> \| WiFi: <size>"`. |
| | `R2-FEAT-02` (Header Hook) | `ControlCenterStatusBar`, `MiuiQSHeaderView` | `system_ui_control_center_dual_data_usage` | Carrier text replaced with dual data usage text when enabled; original carrier string restored when disabled. |
| | `R2-FEAT-03` (Security Intent) | `DualDataUsageHook.kt`, `View.OnLongClickListener` | Long-press on carrier/data text | Dispatches `Intent` with component `com.miui.securitycenter/com.miui.networkassistant.ui.activity.MainActivity`, action `miui.intent.action.NETWORKASSISTANT_MAIN`, and flags `FLAG_ACTIVITY_NEW_TASK`. |
| | `R2-FEAT-04` (Preferences) | `ControlCenterSettings.java`, `system_ui_control_center.xml` | `system_ui_control_center_dual_data_usage` | Preference toggle registered and saved to `hyperceiler_prefs.xml`. |
| **R3: Custom Shapes & Sizes** | `R3-FEAT-01` (Corner Radius) | `QSTileItemIconView.setCornerRadius()`, `GradientDrawable` | `system_ui_control_center_rounded_rect` (Bool), `system_ui_control_center_rounded_rect_radius` (Int dp, 0..50) | Injected corner radius applied to background drawables and `getCornerRadius()` hook return value. |
| | `R3-FEAT-02` (Tile Scale) | `QSTileItemIconView.setScaleX()`, `setScaleY()` | `system_ui_control_center_tile_scale` (Int %, 50..150, def 100) | `scaleX` and `scaleY` set to `scalePercent / 100.0f` on child icon views. |
| | `R3-FEAT-03` (Preferences) | `TileSettings.java`, `system_ui_control_center_tiles.xml` | Keys above | Shape toggle and sliders dynamically update runtime values. |
| **R4: iOS-Style Connectivity Platter** | `R4-FEAT-01` (2x2 Container) | `ConnectivityPlatterHook.kt`, `QSController`, `QSCardItemView` | `system_ui_control_center_ios_connectivity_platter` (Bool, def false) | Platter replaces separate WiFi/Cell big cards with composite 2x2 container holding WiFi, Bluetooth, Mobile Data, Airplane Mode. |
| | `R4-FEAT-02` (Sub-Tile States) | `ConnectivityPlatterView.SubTile` | State resolver logic | Correct resolution of Active (enabled/connected), Inactive (disabled), Unavailable (no SIM/no HW), and Transient (turning on/off) states. |
| | `R4-FEAT-03` (Expanded Modal) | `ConnectivityExpandDialog` | Long-press on Platter container | Spawns expanded modal dialog displaying 6 detailed connectivity rows (WiFi + SSID, Bluetooth + Device, Mobile Data + Roaming, Airplane Mode, Hotspot, NFC/AirDrop). |
| | `R4-FEAT-04` (Preferences) | `ControlCenterSettings.java`, `system_ui_control_center.xml` | `system_ui_control_center_ios_connectivity_platter` | Preference toggle registered and saved to `hyperceiler_prefs.xml`. |

---

## 3. Test Tiers & Execution Matrix

```
+-------------------------------------------------------------------------------+
|                       HYPERCEILER E2E TEST TIERS MATRIX                       |
+-------------------------------------------------------------------------------+
|  Tier 1: Feature Coverage (Unit & Functional Contracts)                       |
|  - R1: Column computation, spacing calculations, padding application         |
|  - R2: Data usage formatting ("Mobile: X | WiFi: Y"), Intent construction    |
|  - R3: Corner radius float conversion, scale factor normalization            |
|  - R4: 2x2 platter item composition, sub-tile state resolution                |
+-------------------------------------------------------------------------------+
|  Tier 2: Boundary, Corner & Adversarial Stress                                |
|  - Out-of-bounds columns (<3, >6), negative padding, scale <= 0, scale > 200%|
|  - Zero data usage (0 B), extreme data usage (>10 TB), Petabyte overflow      |
|  - Null carrier text, empty strings, missing system services, no-SIM state    |
|  - Corrupted / unexpected SharedPreferences types (ClassCast safety)         |
+-------------------------------------------------------------------------------+
|  Tier 3: Cross-Feature Integration & Layout Coexistence                       |
|  - 5-column grid + 80% tile scale + 36dp corner radius layout balance         |
|  - Dual data usage panel coexistence with Connectivity Platter 2x2 container  |
|  - Orientation shifts (Portrait 4 cols -> Landscape 6 cols re-measure)       |
|  - HyperOS 1.0 vs HyperOS 2.0 ClassLoader & method hook coexistence          |
+-------------------------------------------------------------------------------+
|  Tier 4: Real-World Scenarios & End-to-End Control Center Lifecycle           |
|  - Cold boot: Read preferences -> initialize hooks -> default state           |
|  - Hot preference mutation: SharedPreferences update -> ContentObserver event |
|    -> PrefsMap cache refresh -> layout re-measure -> state re-render          |
|  - Full User Gesture Flow: Long-press data text -> Intent launch verification |
|  - Full User Gesture Flow: Tap sub-tile -> state toggle; Long-press platter  |
|    -> ExpandDialog construction & row population                              |
+-------------------------------------------------------------------------------+
```

### Detailed Tier Specifications

#### Tier 1: Feature Coverage
- **T1.1 (R1 Grid Math)**:
  - Input: Container width = 1080px, paddingH = 48px, marginH = 24px, columns = 4.
  - Expected: Total available width = `1080 - 2*48 = 984px`. Total margin gap = `(4-1)*24 = 72px`. Tile cell width = `(984 - 72) / 4 = 228px`.
  - Input: Columns = 3, columns = 5, columns = 6.
  - Expected: Exact integer cell width and non-overlapping child bounds `[left, right]`.
- **T1.2 (R2 Formatting & Time Range)**:
  - Input: Mobile bytes = `5,368,709,120` (5.00 GB), WiFi bytes = `11,274,289,152` (10.50 GB).
  - Expected string: `"Mobile: 5.00 GB | WiFi: 10.50 GB"`.
  - Input: Timestamp `2026-08-21T14:30:00.000Z`.
  - Expected query interval: `startOfDay = 2026-08-21T00:00:00.000Z` to `endTime = 2026-08-21T14:30:00.000Z`.
- **T1.3 (R2 Security Intent Target)**:
  - Expected Package: `com.miui.securitycenter`.
  - Expected Class: `com.miui.networkassistant.ui.activity.MainActivity`.
  - Expected Action: `miui.intent.action.NETWORKASSISTANT_MAIN`.
  - Expected Flags: `Intent.FLAG_ACTIVITY_NEW_TASK` (`0x10000000`).
- **T1.4 (R3 Radius & Scale)**:
  - Input: `system_ui_control_center_rounded_rect_radius` = 45 dp, density = 2.75.
  - Expected radius px: `45 * 2.75 = 123.75f`.
  - Input: `system_ui_control_center_tile_scale` = 85%.
  - Expected scaleX / scaleY: `0.85f`.
- **T1.5 (R4 Platter Item Mapping & States)**:
  - Expected composite specs: `["wifi", "bluetooth", "cell", "airplane"]`.
  - Expected sub-tile states:
    - Wi-Fi enabled & connected to "Home-5G" -> Active (`state = 2`), Subtitle = "Home-5G".
    - Bluetooth enabled & disconnected -> Inactive / Ready (`state = 1`), Subtitle = "On".
    - Mobile Data enabled -> Active (`state = 2`), Subtitle = "LTE/5G".
    - Airplane mode disabled -> Inactive (`state = 0`), Subtitle = "Off".

#### Tier 2: Boundary, Corner & Adversarial Stress
- **T2.1 (Clamping Grid Columns)**:
  - Input: `grid_columns = 0`, `-5`, `1`, `10`, `999`.
  - Expected: Automatically clamped within safe range `[3..6]`. Layout does not divide-by-zero or crash.
- **T2.2 (Extreme Padding & Margin Boundaries)**:
  - Input: `grid_padding_h = 500px` on a 400px container width (`2*padding > width`).
  - Expected: Safe fallback cell width (minimum >= 1px), no negative layout widths or IllegalArgumentException.
- **T2.3 (Extreme Scale Factors)**:
  - Input: `tile_scale = 0%`, `-50%`, `300%`.
  - Expected: Clamped safely to `[50%..150%]` (i.e., `0.50f..1.50f`).
- **T2.4 (Data Usage Size Extreme Values & Zero Handling)**:
  - Input: `mobile = 0, wifi = 0` -> `"Mobile: 0 B | WiFi: 0 B"`.
  - Input: `mobile = 1024` -> `"Mobile: 1.00 KB | WiFi: 0 B"`.
  - Input: `mobile = 1,125,899,906,842,624` (1 PB) -> `"Mobile: 1.00 PB | WiFi: 0 B"`.
- **T2.5 (Null & Special Character Inputs in Preferences & Carrier Strings)**:
  - Input: Carrier text = `null`, `""`, `"<unknown>", "中国移动 | CMCC 5G ⚡"`.
  - Expected: No NullPointerException; clean string replacement.
- **T2.6 (Corrupted Preference Type Handling)**:
  - Input: String stored where Integer expected in SharedPreferences.
  - Expected: Safe default returned via fallback handler without crashing the SystemUI process.

#### Tier 3: Cross-Feature Integration
- **T3.1 (R1 Grid + R3 Scale & Radius Multi-Variable Grid)**:
  - Grid: 5 columns, horizontal margin = 12dp, vertical margin = 16dp, edge padding = 16dp.
  - Tile: scale = 90%, corner radius = 28dp.
  - Verification: Child tile icons receive exact scale transformation and corner radius while maintaining exact grid positioning without overlap.
- **T3.2 (R2 Dual Data Usage + R4 Platter Coexistence)**:
  - Header: Dual data text active and updated.
  - QS Layout: Connectivity Platter 2x2 active in card container.
  - Verification: No listener or view-hierarchy interference between header status bar and QS controller cards.
- **T3.3 (Dynamic Configuration Changes - Orientation Shift)**:
  - Simulated switch from Portrait (`width = 1080px, cols = 4`) to Landscape (`width = 2400px, cols = 6`).
  - Verification: Re-measure pass triggers column update, cell width recalculation, and proper bounds recalculation.

#### Tier 4: Real-World Scenarios & Lifecycle Simulation
- **T4.1 (Complete Control Center Boot Lifecycle)**:
  - Step 1: `XposedInit` / `DataBase` loads module for `com.android.systemui`.
  - Step 2: SystemUI initializes `ControlCenterStatusBar` and `MiuiQSHeaderView`.
  - Step 3: Dynamic plugin loader intercepts `miui.systemui.controlcenter`.
  - Step 4: Hooks register across `QSTileListLayout`, `QSTileItemIconView`, `QSController`.
  - Step 5: Initial views inflate with preferences applied.
- **T4.2 (Hot Preference Update & Live Reload)**:
  - Step 1: User changes `grid_columns` from 4 to 5 in HyperCeiler Settings.
  - Step 2: `SharedPreferences.OnSharedPreferenceChangeListener` notifies URI `content://com.harry.hyperhand.sharedprefs/...`.
  - Step 3: `PrefsMap` refreshes cached value.
  - Step 4: Control Center triggers `requestLayout()` and recalculates layout with 5 columns.
- **T4.3 (Interactive Event Handling - Long-Press Dual Data)**:
  - Step 1: Carrier text view receives simulated `performLongClick()`.
  - Step 2: Long-click listener fires, builds Security Intent.
  - Step 3: Intent is intercepted and validated against expected action and component.
- **T4.4 (Interactive Event Handling - Platter Expansion Modal)**:
  - Step 1: Platter container receives simulated long-press.
  - Step 2: `ConnectivityExpandDialog` is instantiated.
  - Step 3: Modal dialog contains all 6 connectivity rows (WiFi, Bluetooth, Cellular, Airplane, Hotspot, MiShare/NFC) with live state bindings.

---

## 4. Test Runner Commands & Automated Harness

### Running via Standalone Automated E2E Runner (Fast, Zero Dependency)
```bash
./tools/run_e2e_tests.sh
```

### Running via Gradle Tasks
```bash
proot -b /data/data/com.termux/files/usr/glibc/lib:/lib \
      -b /data/data/com.termux/files/usr/tmp:/tmp \
      env JAVA_HOME=/data/data/com.termux/files/home/jdk-17.0.12+7 \
          PATH=/data/data/com.termux/files/home/jdk-17.0.12+7/bin:/data/data/com.termux/files/usr/bin \
      ./gradlew -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp \
                -Dorg.gradle.jvmargs="-Xmx3072m -Dfile.encoding=UTF-8 -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp" \
                test
```

---

## 5. Expected Test Outputs & Verification Invariants

```
================================================================================
                    HYPERCEILER E2E TEST SUITE EXECUTION
================================================================================
[TIER 1: FEATURE COVERAGE]
  PASS: test_r1_grid_column_calculation (cols=4, cell_width=228px)
  PASS: test_r1_grid_margin_and_padding_spacing
  PASS: test_r1_preference_keys_and_defaults
  PASS: test_r2_dual_data_usage_formatting (5.00 GB Mobile | 10.50 GB WiFi)
  PASS: test_r2_midnight_epoch_window_derivation
  PASS: test_r2_security_activity_intent_construction
  PASS: test_r3_corner_radius_dp_to_px_conversion
  PASS: test_r3_tile_scale_normalization (85% -> 0.85f)
  PASS: test_r4_connectivity_platter_2x2_composition
  PASS: test_r4_sub_tile_state_resolution (Active/Inactive/Unavailable)
  PASS: test_r4_expand_dialog_row_population (6 items)

[TIER 2: BOUNDARY & CORNER CASES]
  PASS: test_r1_grid_column_underflow_clamped (cols=0 -> clamped 3)
  PASS: test_r1_grid_column_overflow_clamped (cols=10 -> clamped 6)
  PASS: test_r1_excessive_padding_safe_fallback
  PASS: test_r2_data_usage_zero_bytes_formatting ("Mobile: 0 B | WiFi: 0 B")
  PASS: test_r2_data_usage_extreme_petabyte_formatting
  PASS: test_r2_null_carrier_text_resilience
  PASS: test_r3_tile_scale_boundary_clamping (scale=0% -> 50%, scale=300% -> 150%)
  PASS: test_r3_negative_radius_clamping
  PASS: test_r4_no_sim_state_graceful_handling
  PASS: test_prefs_type_mismatch_exception_safety

[TIER 3: CROSS-FEATURE INTEGRATION]
  PASS: test_cross_r1_r3_multi_variable_grid_and_tile_geometry
  PASS: test_cross_r2_r4_dual_data_and_platter_coexistence
  PASS: test_cross_orientation_change_remeasure (Portrait 4-col -> Landscape 6-col)

[TIER 4: REAL-WORLD APPLICATION & LIFECYCLE]
  PASS: test_e2e_cold_boot_and_hook_registration_lifecycle
  PASS: test_e2e_hot_preference_update_live_reload
  PASS: test_e2e_user_gesture_long_press_data_usage_intent_dispatch
  PASS: test_e2e_user_gesture_platter_long_press_expand_modal

================================================================================
ALL TEST TIERS PASSED (28/28 tests passed, 0 failures, 0 skipped)
================================================================================
```
