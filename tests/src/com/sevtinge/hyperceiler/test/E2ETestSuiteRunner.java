package com.sevtinge.hyperceiler.test;

/**
 * Main Test Runner Entrypoint for HyperCeiler E2E Test Suite.
 * Executes all 28 tests across 4 tiers specified in TEST_INFRA.md and prints reports.
 */
public class E2ETestSuiteRunner {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    private static void executeTest(String testLabel, Runnable test) {
        try {
            test.run();
            System.out.println("  PASS: " + testLabel);
            totalPassed++;
        } catch (Throwable t) {
            System.err.println("  FAIL: " + testLabel + " -> " + t.getMessage());
            t.printStackTrace();
            totalFailed++;
        }
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        System.out.println("================================================================================");
        System.out.println("                    HYPERCEILER E2E TEST SUITE EXECUTION                        ");
        System.out.println("================================================================================");

        // TIER 1: FEATURE COVERAGE
        System.out.println("[TIER 1: FEATURE COVERAGE]");
        executeTest("test_r1_grid_column_calculation (cols=4, cell_width=228px)",
                GridAndLayoutMathTest::test_r1_grid_column_calculation);
        executeTest("test_r1_grid_margin_and_padding_spacing",
                GridAndLayoutMathTest::test_r1_grid_margin_and_padding_spacing);
        executeTest("test_r1_preference_keys_and_defaults",
                PrefsContractTest::test_r1_preference_keys_and_defaults);
        executeTest("test_r2_dual_data_usage_formatting (5.00 GB Mobile | 10.50 GB WiFi)",
                DualDataUsageTest::test_r2_dual_data_usage_formatting);
        executeTest("test_r2_midnight_epoch_window_derivation",
                DualDataUsageTest::test_r2_midnight_epoch_window_derivation);
        executeTest("test_r2_security_activity_intent_construction",
                DualDataUsageTest::test_r2_security_activity_intent_construction);
        executeTest("test_r3_corner_radius_dp_to_px_conversion",
                TileShapeAndScaleTest::test_r3_corner_radius_dp_to_px_conversion);
        executeTest("test_r3_tile_scale_normalization (85% -> 0.85f)",
                TileShapeAndScaleTest::test_r3_tile_scale_normalization);
        executeTest("test_r4_connectivity_platter_2x2_composition",
                ConnectivityPlatterTest::test_r4_connectivity_platter_2x2_composition);
        executeTest("test_r4_sub_tile_state_resolution (Active/Inactive/Unavailable)",
                ConnectivityPlatterTest::test_r4_sub_tile_state_resolution);
        executeTest("test_r4_expand_dialog_row_population (6 items)",
                ConnectivityPlatterTest::test_r4_expand_dialog_row_population);

        // TIER 2: BOUNDARY & CORNER CASES
        System.out.println("\n[TIER 2: BOUNDARY & CORNER CASES]");
        executeTest("test_r1_grid_column_underflow_clamped (cols=0 -> clamped 3)",
                GridAndLayoutMathTest::test_r1_grid_column_underflow_clamped);
        executeTest("test_r1_grid_column_overflow_clamped (cols=10 -> clamped 6)",
                GridAndLayoutMathTest::test_r1_grid_column_overflow_clamped);
        executeTest("test_r1_excessive_padding_safe_fallback",
                GridAndLayoutMathTest::test_r1_excessive_padding_safe_fallback);
        executeTest("test_extreme_column_underflow_and_overflow (col 0, 1, 2, 7, 10, min/max int)",
                GridStressAndAdversarialTest::test_extreme_column_underflow_and_overflow);
        executeTest("test_container_width_overflow_and_narrow_displays (overflow & micro screens)",
                GridStressAndAdversarialTest::test_container_width_overflow_and_narrow_displays);
        executeTest("test_negative_dimensions_and_density_resilience (negative dp, densities 0.75x..4.0x)",
                GridStressAndAdversarialTest::test_negative_dimensions_and_density_resilience);
        executeTest("test_rapid_concurrent_orientation_and_density_mutations (200k iterations, 8 threads)",
                () -> {
                    try {
                        GridStressAndAdversarialTest.test_rapid_concurrent_orientation_and_density_mutations();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        executeTest("test_weak_reference_lifecycle_and_gc_safety (View GC deallocation)",
                GridStressAndAdversarialTest::test_weak_reference_lifecycle_and_gc_safety);
        executeTest("test_r2_data_usage_zero_bytes_formatting (\"Mobile: 0 B | WiFi: 0 B\")",
                DualDataUsageTest::test_r2_data_usage_zero_bytes_formatting);
        executeTest("test_r2_data_usage_extreme_petabyte_formatting",
                DualDataUsageTest::test_r2_data_usage_extreme_petabyte_formatting);
        executeTest("test_r2_null_carrier_text_resilience",
                DualDataUsageTest::test_r2_null_carrier_text_resilience);
        executeTest("test_r3_tile_scale_boundary_clamping (scale=0% -> 50%, scale=300% -> 150%)",
                TileShapeAndScaleTest::test_r3_tile_scale_boundary_clamping);
        executeTest("test_r3_negative_radius_clamping",
                TileShapeAndScaleTest::test_r3_negative_radius_clamping);
        executeTest("test_r4_no_sim_state_graceful_handling",
                ConnectivityPlatterTest::test_r4_no_sim_state_graceful_handling);
        executeTest("test_prefs_type_mismatch_exception_safety",
                PrefsContractTest::test_prefs_type_mismatch_exception_safety);

        // TIER 3: CROSS-FEATURE INTEGRATION
        System.out.println("\n[TIER 3: CROSS-FEATURE INTEGRATION]");
        executeTest("test_cross_r1_r3_multi_variable_grid_and_tile_geometry",
                CrossFeatureAndLifecycleE2ETest::test_cross_r1_r3_multi_variable_grid_and_tile_geometry);
        executeTest("test_cross_r2_r4_dual_data_and_platter_coexistence",
                CrossFeatureAndLifecycleE2ETest::test_cross_r2_r4_dual_data_and_platter_coexistence);
        executeTest("test_cross_orientation_change_remeasure (Portrait 4-col -> Landscape 6-col)",
                CrossFeatureAndLifecycleE2ETest::test_cross_orientation_change_remeasure);

        // TIER 4: REAL-WORLD APPLICATION & LIFECYCLE
        System.out.println("\n[TIER 4: REAL-WORLD APPLICATION & LIFECYCLE]");
        executeTest("test_e2e_cold_boot_and_hook_registration_lifecycle",
                CrossFeatureAndLifecycleE2ETest::test_e2e_cold_boot_and_hook_registration_lifecycle);
        executeTest("test_e2e_hot_preference_update_live_reload",
                CrossFeatureAndLifecycleE2ETest::test_e2e_hot_preference_update_live_reload);
        executeTest("test_e2e_user_gesture_long_press_data_usage_intent_dispatch",
                CrossFeatureAndLifecycleE2ETest::test_e2e_user_gesture_long_press_data_usage_intent_dispatch);
        executeTest("test_e2e_user_gesture_platter_long_press_expand_modal",
                CrossFeatureAndLifecycleE2ETest::test_e2e_user_gesture_platter_long_press_expand_modal);

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("\n================================================================================");
        if (totalFailed == 0) {
            System.out.println(String.format("ALL TEST TIERS PASSED (%d/%d tests passed, 0 failures, 0 skipped in %d ms)",
                    totalPassed, (totalPassed + totalFailed), duration));
            System.out.println("================================================================================");
            System.exit(0);
        } else {
            System.err.println(String.format("TEST SUITE FAILED (%d failed, %d passed in %d ms)",
                    totalFailed, totalPassed, duration));
            System.out.println("================================================================================");
            System.exit(1);
        }
    }
}
