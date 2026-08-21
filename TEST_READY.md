# TEST_READY — HyperCeiler End-to-End Test Suite

## 1. Test Runner Command

The automated End-to-End test harness can be executed directly from the project root:

```bash
./tools/run_e2e_tests.sh
```

---

## 2. Test Tier Breakdown & Execution Summary

| Tier | Category | Tests Defined | Tests Passed | Status |
|---|---|---|---|---|
| **Tier 1** | Feature Coverage (Unit & Functional Contracts) | 11 | 11 | **PASS** |
| **Tier 2** | Boundary, Corner & Adversarial Stress | 10 | 10 | **PASS** |
| **Tier 3** | Cross-Feature Integration & Layout Coexistence | 3 | 3 | **PASS** |
| **Tier 4** | Real-World Scenarios & Lifecycle Simulation | 4 | 4 | **PASS** |
| **Total** | **All Test Tiers** | **28** | **28** | **PASS (100%)** |

---

## 3. Detailed Test Inventory by Tier

### Tier 1: Feature Coverage
- `test_r1_grid_column_calculation (cols=4, cell_width=228px)`: Validates exact integer width calculations across 3, 4, 5, and 6 columns.
- `test_r1_grid_margin_and_padding_spacing`: Verifies non-overlapping rectangular bounds calculation for child tiles.
- `test_r1_preference_keys_and_defaults`: Verifies default fallbacks and mutations for R1 grid preference keys.
- `test_r2_dual_data_usage_formatting (5.00 GB Mobile | 10.50 GB WiFi)`: Validates `"Mobile: <size> | WiFi: <size>"` string generation across GB/MB/KB ranges.
- `test_r2_midnight_epoch_window_derivation`: Validates start-of-day (00:00:00.000) epoch timestamp calculation.
- `test_r2_security_activity_intent_construction`: Validates intent targeting `com.miui.securitycenter/com.miui.networkassistant.ui.activity.MainActivity` with `FLAG_ACTIVITY_NEW_TASK`.
- `test_r3_corner_radius_dp_to_px_conversion`: Validates dp-to-pixel conversion across 2.75x and 3.0x display densities.
- `test_r3_tile_scale_normalization (85% -> 0.85f)`: Validates percentage scale factor conversion to float scale factors.
- `test_r4_connectivity_platter_2x2_composition`: Validates 2x2 container composition containing WiFi, Bluetooth, Mobile Data, Airplane Mode.
- `test_r4_sub_tile_state_resolution (Active/Inactive/Unavailable)`: Validates state mapping (Active=2, Inactive=1/0, Unavailable=0) and live subtitle population.
- `test_r4_expand_dialog_row_population (6 items)`: Validates 6-item composition for modal dialog (WiFi, BT, Cell, Airplane, Hotspot, Mi Share).

### Tier 2: Boundary & Corner Cases
- `test_r1_grid_column_underflow_clamped (cols=0 -> clamped 3)`: Verifies columns underflow clamping to MIN 3.
- `test_r1_grid_column_overflow_clamped (cols=10 -> clamped 6)`: Verifies columns overflow clamping to MAX 6.
- `test_r1_excessive_padding_safe_fallback`: Verifies container padding exceeding screen width falls back safely without divide-by-zero.
- `test_r2_data_usage_zero_bytes_formatting ("Mobile: 0 B | WiFi: 0 B")`: Verifies zero and negative bytes formatting.
- `test_r2_data_usage_extreme_petabyte_formatting`: Verifies petabyte (PB) scale formatting.
- `test_r2_null_carrier_text_resilience`: Verifies null carrier, empty string, and unicode symbol robustness.
- `test_r3_tile_scale_boundary_clamping (scale=0% -> 50%, scale=300% -> 150%)`: Verifies scale clamping between 50% and 150%.
- `test_r3_negative_radius_clamping`: Verifies negative and extreme radius clamping.
- `test_r4_no_sim_state_graceful_handling`: Verifies no-SIM state correctly sets state UNAVAILABLE and subtitle "No SIM".
- `test_prefs_type_mismatch_exception_safety`: Verifies type coercion and ClassCast safety when corrupted or unexpected types are stored.

### Tier 3: Cross-Feature Integration
- `test_cross_r1_r3_multi_variable_grid_and_tile_geometry`: Verifies 5-column grid + 85% tile scale + 30dp corner radius layout balance.
- `test_cross_r2_r4_dual_data_and_platter_coexistence`: Verifies dual data header and 2x2 platter coexist without listener or state collisions.
- `test_cross_orientation_change_remeasure (Portrait 4-col -> Landscape 6-col)`: Verifies layout re-measure when switching between Portrait and Landscape orientations.

### Tier 4: Real-World Scenarios & Lifecycle Simulation
- `test_e2e_cold_boot_and_hook_registration_lifecycle`: Verifies complete module initialization from cold boot with default settings.
- `test_e2e_hot_preference_update_live_reload`: Verifies live preference changes immediately re-render views and recompute geometry.
- `test_e2e_user_gesture_long_press_data_usage_intent_dispatch`: Verifies simulated long-press on data usage text triggers SecurityCenter Intent broadcast.
- `test_e2e_user_gesture_platter_long_press_expand_modal`: Verifies simulated long-press on Platter view spawns expanded modal with 6 connectivity rows.

---

## 4. Feature Coverage Checklist

- [x] **R1: Adjustable Grid & Padding**
  - [x] Customizable columns (clamped 3..6, default 4)
  - [x] Horizontal & vertical inter-tile margins (0..30 dp)
  - [x] Horizontal edge padding (0..40 dp)
  - [x] SharedPreferences mapping with `prefs_key_` prefix
- [x] **R2: Dual Data Usage Panel**
  - [x] Start-of-day epoch window derivation
  - [x] Byte size formatting with dynamic units (B, KB, MB, GB, TB, PB)
  - [x] Header carrier text hook & replacement
  - [x] SecurityCenter long-press Intent creation & dispatch
- [x] **R3: Custom Shapes & Sizes**
  - [x] Dynamic corner radius float calculation based on density
  - [x] Scale factor normalization and boundary clamping (50%..150%)
  - [x] Preference toggle resolution
- [x] **R4: iOS-Style Connectivity Platter**
  - [x] 2x2 compact platter composition (WiFi, Bluetooth, Cellular, Airplane Mode)
  - [x] Sub-tile state resolver (Active, Inactive, Unavailable, Transient)
  - [x] Expanded modal dialog composition (6 items: WiFi, BT, Cell, Airplane, Hotspot, Mi Share)
- [x] **E2E Integration & Lifecycle**
  - [x] Cold boot initialization
  - [x] Live hot reloading on preference mutation
  - [x] Orientation change re-measure pass
  - [x] Interactive gesture simulation
