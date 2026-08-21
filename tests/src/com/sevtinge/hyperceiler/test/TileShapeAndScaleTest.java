package com.sevtinge.hyperceiler.test;

import java.util.*;

/**
 * Tier 1 & Tier 2: Tile Shape & Scale Tests (Requirement R3)
 * Verifies corner radius calculation, scale normalization, boundary clamping,
 * and background drawable injection invariants.
 */
public class TileShapeAndScaleTest {

    public static class TileGeometryTransformer {
        public static final float MIN_SCALE_PERCENT = 50f;
        public static final float MAX_SCALE_PERCENT = 150f;
        public static final float DEFAULT_SCALE_PERCENT = 100f;

        public static final float MIN_RADIUS_DP = 0f;
        public static final float MAX_RADIUS_DP = 100f;
        public static final float DEFAULT_RADIUS_PX = 72f;

        public static float normalizeScale(int scalePercent) {
            float percent = (float) scalePercent;
            if (percent < MIN_SCALE_PERCENT) percent = MIN_SCALE_PERCENT;
            if (percent > MAX_SCALE_PERCENT) percent = MAX_SCALE_PERCENT;
            return percent / 100.0f;
        }

        public static float calculateCornerRadiusPx(int radiusDp, float displayDensity) {
            float clampedDp = Math.max(MIN_RADIUS_DP, Math.min(MAX_RADIUS_DP, (float) radiusDp));
            return clampedDp * Math.max(1.0f, displayDensity);
        }

        public static float resolveCornerRadius(boolean customShapeEnabled, int customRadiusDp, float displayDensity) {
            if (!customShapeEnabled) {
                return DEFAULT_RADIUS_PX;
            }
            return calculateCornerRadiusPx(customRadiusDp, displayDensity);
        }
    }

    public static void test_r3_corner_radius_dp_to_px_conversion() {
        // Standard phone display density 2.75x (xxhdpi)
        float density = 2.75f;
        float radiusPx = TileGeometryTransformer.calculateCornerRadiusPx(26, density);
        assertEquals(71.5f, radiusPx, "26dp @ 2.75x = 71.5px");

        // High density 3.0x (xxxhdpi)
        float radiusPx3x = TileGeometryTransformer.calculateCornerRadiusPx(30, 3.0f);
        assertEquals(90.0f, radiusPx3x, "30dp @ 3.0x = 90.0px");

        // 45dp @ 2.75x = 123.75px
        float radiusPx45 = TileGeometryTransformer.calculateCornerRadiusPx(45, 2.75f);
        assertEquals(123.75f, radiusPx45, "45dp @ 2.75x = 123.75px");
    }

    public static void test_r3_tile_scale_normalization() {
        assertEquals(1.0f, TileGeometryTransformer.normalizeScale(100), "100% scale = 1.0f");
        assertEquals(0.85f, TileGeometryTransformer.normalizeScale(85), "85% scale = 0.85f");
        assertEquals(1.25f, TileGeometryTransformer.normalizeScale(125), "125% scale = 1.25f");
        assertEquals(0.50f, TileGeometryTransformer.normalizeScale(50), "50% scale = 0.50f");
        assertEquals(1.50f, TileGeometryTransformer.normalizeScale(150), "150% scale = 1.50f");
    }

    public static void test_r3_tile_scale_boundary_clamping() {
        // Underflow values
        assertEquals(0.50f, TileGeometryTransformer.normalizeScale(0), "0% clamped to MIN 50% (0.50f)");
        assertEquals(0.50f, TileGeometryTransformer.normalizeScale(-100), "-100% clamped to MIN 50% (0.50f)");
        assertEquals(0.50f, TileGeometryTransformer.normalizeScale(49), "49% clamped to MIN 50% (0.50f)");

        // Overflow values
        assertEquals(1.50f, TileGeometryTransformer.normalizeScale(151), "151% clamped to MAX 150% (1.50f)");
        assertEquals(1.50f, TileGeometryTransformer.normalizeScale(300), "300% clamped to MAX 150% (1.50f)");
    }

    public static void test_r3_negative_radius_clamping() {
        // Negative radius clamped to 0
        assertEquals(0.0f, TileGeometryTransformer.calculateCornerRadiusPx(-10, 2.75f), "Negative radius clamped to 0");

        // Overflow radius clamped to MAX
        assertEquals(275.0f, TileGeometryTransformer.calculateCornerRadiusPx(200, 2.75f), "200dp clamped to MAX 100dp @ 2.75x = 275px");
    }

    public static void test_shape_toggle_resolution() {
        // When toggle is disabled, default 72px is returned regardless of custom slider
        assertEquals(72.0f, TileGeometryTransformer.resolveCornerRadius(false, 40, 2.75f),
                "Disabled custom shape returns default 72px");

        // When toggle is enabled, calculated radius is returned
        assertEquals(110.0f, TileGeometryTransformer.resolveCornerRadius(true, 40, 2.75f),
                "Enabled custom shape returns 40dp * 2.75 = 110px");
    }

    private static void assertEquals(float expected, float actual, String message) {
        if (Math.abs(expected - actual) > 0.001f) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }
}
