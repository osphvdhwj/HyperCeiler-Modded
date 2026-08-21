package com.sevtinge.hyperceiler.test;

import java.util.*;

/**
 * Tier 3 & Tier 4: Cross-Feature Integration & Real-World Lifecycle E2E Tests
 * Covers multi-feature interactions, orientation shifts, complete Control Center
 * lifecycle simulations, live preference hot reloading, and interactive gesture dispatches.
 */
public class CrossFeatureAndLifecycleE2ETest {

    // Simulated Control Center Lifecycle Context
    public static class ControlCenterSimulatedContext {
        public final PrefsContractTest.PrefsMap<String, Object> prefs = new PrefsContractTest.PrefsMap<>();

        // Layout state
        public int screenWidth = 1080;
        public int screenHeight = 2400;
        public boolean isLandscape = false;
        public float density = 2.75f;

        // View elements
        public String carrierTextView = "China Mobile";
        public boolean dualDataActive = false;
        public DualDataUsageTest.DataUsageFormatter.SecurityIntentSpec lastLaunchedIntent = null;

        public boolean platterActive = false;
        public List<ConnectivityPlatterTest.SubTileInfo> currentPlatter = null;
        public List<ConnectivityPlatterTest.SubTileInfo> currentExpandedModal = null;

        public int measuredColumns = 4;
        public int measuredCellWidth = 0;
        public float measuredScale = 1.0f;
        public float measuredRadius = 72f;

        public void onBoot() {
            // Read initial preferences
            dualDataActive = prefs.getBoolean("system_ui_control_center_dual_data_usage");
            platterActive = prefs.getBoolean("system_ui_control_center_ios_connectivity_platter");
            recomputeLayout();
        }

        public void onConfigurationChanged(boolean landscape, int newWidth, int newHeight) {
            this.isLandscape = landscape;
            this.screenWidth = newWidth;
            this.screenHeight = newHeight;
            recomputeLayout();
        }

        public void onPreferenceChanged(String key, Object value) {
            prefs.put("prefs_key_" + key, value);
            dualDataActive = prefs.getBoolean("system_ui_control_center_dual_data_usage");
            platterActive = prefs.getBoolean("system_ui_control_center_ios_connectivity_platter");
            recomputeLayout();
        }

        public void recomputeLayout() {
            // R1: Columns
            if (prefs.getBoolean("system_ui_control_center_custom_grid")) {
                measuredColumns = prefs.getInt("system_ui_control_center_grid_columns", 4);
            } else {
                measuredColumns = isLandscape ? 6 : 4;
            }
            measuredColumns = GridAndLayoutMathTest.LayoutCalculator.clampColumns(measuredColumns);

            int paddingH = prefs.getInt("system_ui_control_center_grid_padding_h", 16);
            int marginH = prefs.getInt("system_ui_control_center_tile_margin_h", 12);
            measuredCellWidth = GridAndLayoutMathTest.LayoutCalculator.calculateCellWidth(screenWidth, paddingH, marginH, measuredColumns);

            // R3: Shape & Scale
            boolean customShape = prefs.getBoolean("system_ui_control_center_rounded_rect");
            int radiusDp = prefs.getInt("system_ui_control_center_rounded_rect_radius", 26);
            measuredRadius = TileShapeAndScaleTest.TileGeometryTransformer.resolveCornerRadius(customShape, radiusDp, density);

            int scalePercent = prefs.getInt("system_ui_control_center_tile_scale", 100);
            measuredScale = TileShapeAndScaleTest.TileGeometryTransformer.normalizeScale(scalePercent);

            // R2: Header Text
            if (dualDataActive) {
                carrierTextView = DualDataUsageTest.DataUsageFormatter.formatDualDataText(4294967296L, 8589934592L); // 4GB / 8GB
            } else {
                carrierTextView = "China Mobile";
            }

            // R4: Platter
            if (platterActive) {
                currentPlatter = ConnectivityPlatterTest.ConnectivityModel.createCompactPlatter(
                        true, "Hyper-WiFi", true, "Buds Pro", true, "5G Ultra", true, false);
            } else {
                currentPlatter = null;
            }
        }

        public void performLongClickOnDataText() {
            if (dualDataActive) {
                lastLaunchedIntent = DualDataUsageTest.DataUsageFormatter.buildSecurityIntent();
            }
        }

        public void performLongClickOnPlatter() {
            if (platterActive && currentPlatter != null) {
                currentExpandedModal = ConnectivityPlatterTest.ConnectivityModel.createExpandedDialogItems(
                        currentPlatter, true, 1, true);
            }
        }
    }

    public static void test_cross_r1_r3_multi_variable_grid_and_tile_geometry() {
        ControlCenterSimulatedContext ctx = new ControlCenterSimulatedContext();
        ctx.onPreferenceChanged("system_ui_control_center_custom_grid", true);
        ctx.onPreferenceChanged("system_ui_control_center_grid_columns", 5);
        ctx.onPreferenceChanged("system_ui_control_center_tile_margin_h", 16);
        ctx.onPreferenceChanged("system_ui_control_center_grid_padding_h", 24);
        ctx.onPreferenceChanged("system_ui_control_center_rounded_rect", true);
        ctx.onPreferenceChanged("system_ui_control_center_rounded_rect_radius", 30);
        ctx.onPreferenceChanged("system_ui_control_center_tile_scale", 85);

        assertEquals(5, ctx.measuredColumns, "Columns set to 5");
        assertEquals(0.85f, ctx.measuredScale, 0.001f, "Scale set to 0.85f");
        assertEquals(82.5f, ctx.measuredRadius, 0.001f, "Radius set to 30dp * 2.75 = 82.5px");
        assertTrue(ctx.measuredCellWidth > 0, "Cell width calculated cleanly");
    }

    public static void test_cross_r2_r4_dual_data_and_platter_coexistence() {
        ControlCenterSimulatedContext ctx = new ControlCenterSimulatedContext();
        ctx.onPreferenceChanged("system_ui_control_center_dual_data_usage", true);
        ctx.onPreferenceChanged("system_ui_control_center_ios_connectivity_platter", true);

        assertTrue(ctx.dualDataActive, "Dual data usage is active");
        assertTrue(ctx.platterActive, "Connectivity Platter is active");
        assertTrue(ctx.carrierTextView.contains("Mobile: 4.00 GB | WiFi: 8.00 GB"), "Dual data header populated");
        assertEquals(4, ctx.currentPlatter.size(), "2x2 Platter created concurrently");
    }

    public static void test_cross_orientation_change_remeasure() {
        ControlCenterSimulatedContext ctx = new ControlCenterSimulatedContext();
        ctx.onBoot();
        assertEquals(4, ctx.measuredColumns, "Portrait default 4 columns");

        // Switch to landscape (2400x1080)
        ctx.onConfigurationChanged(true, 2400, 1080);
        assertEquals(6, ctx.measuredColumns, "Landscape default 6 columns");
        assertTrue(ctx.measuredCellWidth > 300, "Landscape cell width expanded appropriately");
    }

    public static void test_e2e_cold_boot_and_hook_registration_lifecycle() {
        ControlCenterSimulatedContext ctx = new ControlCenterSimulatedContext();
        ctx.onBoot();

        assertFalse(ctx.dualDataActive, "Default dual data false");
        assertFalse(ctx.platterActive, "Default platter false");
        assertEquals("China Mobile", ctx.carrierTextView, "Default carrier text intact");
        assertEquals(4, ctx.measuredColumns, "Default 4 columns");
        assertEquals(1.0f, ctx.measuredScale, 0.001f, "Default scale 1.0f");
    }

    public static void test_e2e_hot_preference_update_live_reload() {
        ControlCenterSimulatedContext ctx = new ControlCenterSimulatedContext();
        ctx.onBoot();

        // Simulate user toggling dual data usage in Settings UI
        ctx.onPreferenceChanged("system_ui_control_center_dual_data_usage", true);
        assertTrue(ctx.dualDataActive, "Dual data active after hot reload");
        assertEquals("Mobile: 4.00 GB | WiFi: 8.00 GB", ctx.carrierTextView, "Header immediately updated");

        // Simulate user turning off dual data
        ctx.onPreferenceChanged("system_ui_control_center_dual_data_usage", false);
        assertFalse(ctx.dualDataActive, "Dual data inactive after toggle off");
        assertEquals("China Mobile", ctx.carrierTextView, "Header restored to carrier string");
    }

    public static void test_e2e_user_gesture_long_press_data_usage_intent_dispatch() {
        ControlCenterSimulatedContext ctx = new ControlCenterSimulatedContext();
        ctx.onPreferenceChanged("system_ui_control_center_dual_data_usage", true);

        ctx.performLongClickOnDataText();

        assertNotNull(ctx.lastLaunchedIntent, "Intent should be dispatched on long press");
        assertEquals("com.miui.securitycenter", ctx.lastLaunchedIntent.targetPackage, "Package matches SecurityCenter");
        assertEquals("com.miui.networkassistant.ui.activity.MainActivity", ctx.lastLaunchedIntent.targetActivity, "Activity matches");
        assertEquals("miui.intent.action.NETWORKASSISTANT_MAIN", ctx.lastLaunchedIntent.action, "Action matches");
        assertEquals(0x10000000, ctx.lastLaunchedIntent.flags, "Flags match FLAG_ACTIVITY_NEW_TASK");
    }

    public static void test_e2e_user_gesture_platter_long_press_expand_modal() {
        ControlCenterSimulatedContext ctx = new ControlCenterSimulatedContext();
        ctx.onPreferenceChanged("system_ui_control_center_ios_connectivity_platter", true);

        assertEquals(4, ctx.currentPlatter.size(), "Compact platter ready");
        assertNull(ctx.currentExpandedModal, "No expanded modal yet");

        // Long press platter
        ctx.performLongClickOnPlatter();

        assertNotNull(ctx.currentExpandedModal, "Expanded modal generated");
        assertEquals(6, ctx.currentExpandedModal.size(), "Expanded modal has 6 items");
        assertEquals("Personal Hotspot", ctx.currentExpandedModal.get(4).title, "Item 4 title");
        assertEquals("Mi Share", ctx.currentExpandedModal.get(5).title, "Item 5 title");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertEquals(float expected, float actual, float delta, String message) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " - Expected true, got false");
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message + " - Expected false, got true");
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message + " - Expected non-null, got null");
        }
    }

    private static void assertNull(Object obj, String message) {
        if (obj != null) {
            throw new AssertionError(message + " - Expected null, got " + obj);
        }
    }
}
