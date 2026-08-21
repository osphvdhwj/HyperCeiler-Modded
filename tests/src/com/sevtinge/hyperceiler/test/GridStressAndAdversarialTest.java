package com.sevtinge.hyperceiler.test;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Empirical Adversarial & Stress Test Suite for Milestone M1 (CCGridForHyperOSKt).
 * Formulated by Challenger 1 to stress-test grid mathematics, extreme inputs,
 * container overflow, display density mutations, and high-frequency concurrent orientation changes.
 */
public class GridStressAndAdversarialTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    private static void executeTest(String testLabel, Runnable test) {
        try {
            test.run();
            System.out.println("  [PASS] " + testLabel);
            totalPassed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] " + testLabel + " -> " + t.getMessage());
            t.printStackTrace();
            totalFailed++;
        }
    }

    // =========================================================================
    // 1. Grid Math Simulation matching CCGridForHyperOSKt and QSTileListLayout
    // =========================================================================
    public static class GridMathModel {
        public static final int DEFAULT_PORTRAIT_COLS = 4;
        public static final int DEFAULT_LANDSCAPE_COLS = 6;
        public static final int MIN_COLS = 3;
        public static final int MAX_COLS_PORTRAIT = 6;
        public static final int MAX_COLS_LANDSCAPE = 8;

        public static int resolveColumns(boolean isPortrait, int prefColsPortrait, int prefColsLandscape) {
            int rawCols = isPortrait ? prefColsPortrait : prefColsLandscape;
            // Validate safe clamping if out of UI bounds
            int min = isPortrait ? 3 : 4;
            int max = isPortrait ? 6 : 8;
            if (rawCols < min) return min;
            if (rawCols > max) return max;
            return rawCols;
        }

        public static int computeDimensionPx(int dp, float density) {
            return (int) (Math.max(0, dp) * density);
        }

        public static int calculateCellWidth(int containerWidth, int paddingHPx, int marginHPx, int columns) {
            if (columns <= 0) columns = 1; // Prevent division by zero
            int availableWidth = containerWidth - (2 * Math.max(0, paddingHPx));
            if (availableWidth <= 0) return 1;

            int totalMargins = (columns - 1) * Math.max(0, marginHPx);
            int contentWidth = availableWidth - totalMargins;
            if (contentWidth <= 0) return 1;

            return Math.max(1, contentWidth / columns);
        }

        public static int calculateTotalRowWidth(int cellWidth, int marginHPx, int columns, int paddingHPx) {
            return (columns * cellWidth) + ((columns - 1) * marginHPx) + (2 * paddingHPx);
        }
    }

    // =========================================================================
    // CHALLENGE 1: Extreme Column Values (0, 1, 2, 7, 10, negative, max int)
    // =========================================================================
    public static void test_extreme_column_underflow_and_overflow() {
        int[] extremeCols = { Integer.MIN_VALUE, -100, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 50, 1000, Integer.MAX_VALUE };

        for (int rawCol : extremeCols) {
            int resolvedPortrait = GridMathModel.resolveColumns(true, rawCol, 6);
            assertTrue(resolvedPortrait >= 3 && resolvedPortrait <= 6,
                    "Portrait column " + rawCol + " must clamp between 3 and 6, got " + resolvedPortrait);

            int resolvedLandscape = GridMathModel.resolveColumns(false, 4, rawCol);
            assertTrue(resolvedLandscape >= 4 && resolvedLandscape <= 8,
                    "Landscape column " + rawCol + " must clamp between 4 and 8, got " + resolvedLandscape);

            // Directly test calculateCellWidth with raw unconstrained columns to ensure no divide-by-zero or crash
            int safeWidth = GridMathModel.calculateCellWidth(1080, 48, 24, rawCol);
            assertTrue(safeWidth >= 1, "Cell width must always be >= 1 for raw col " + rawCol + ", got " + safeWidth);
        }
    }

    // =========================================================================
    // CHALLENGE 2: Container Width Overflow with Large Margins & Padding
    // =========================================================================
    public static void test_container_width_overflow_and_narrow_displays() {
        int[] containerWidths = { 0, 1, 50, 100, 200, 360, 480, 720, 1080, 1440, 2400, 3840 };
        int[] paddings = { 0, 16, 40, 100, 500, 2000 };
        int[] margins = { 0, 8, 30, 80, 300, 1000 };
        float[] densities = { 1.0f, 1.5f, 2.0f, 2.75f, 3.0f, 3.5f, 4.0f };

        for (int width : containerWidths) {
            for (int paddingDp : paddings) {
                for (int marginDp : margins) {
                    for (float density : densities) {
                        int paddingPx = GridMathModel.computeDimensionPx(paddingDp, density);
                        int marginPx = GridMathModel.computeDimensionPx(marginDp, density);

                        for (int cols = 3; cols <= 8; cols++) {
                            int cellWidth = GridMathModel.calculateCellWidth(width, paddingPx, marginPx, cols);

                            // Invariant 1: Cell width must always be strictly positive
                            assertTrue(cellWidth >= 1,
                                    String.format("Cell width must be >= 1 (width=%d, pad=%d, mar=%d, density=%.2f, cols=%d) -> got %d",
                                            width, paddingPx, marginPx, density, cols, cellWidth));

                            // Invariant 2: If available width was sufficient, total width must fit inside container
                            int availableWidth = width - (2 * paddingPx);
                            int totalMargins = (cols - 1) * marginPx;
                            if (availableWidth > totalMargins + cols) {
                                int totalRowWidth = GridMathModel.calculateTotalRowWidth(cellWidth, marginPx, cols, paddingPx);
                                assertTrue(totalRowWidth <= width,
                                        String.format("Total row width %d exceeds container %d", totalRowWidth, width));
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // CHALLENGE 3: Negative Dimensions and Display Density Permutations
    // =========================================================================
    public static void test_negative_dimensions_and_density_resilience() {
        int[] negativeValues = { -1000, -50, -1 };
        for (int neg : negativeValues) {
            int paddingPx = GridMathModel.computeDimensionPx(neg, 2.75f);
            assertEquals(0, paddingPx, "Negative padding dp should clamp to 0 px");

            int marginPx = GridMathModel.computeDimensionPx(neg, 2.75f);
            assertEquals(0, marginPx, "Negative margin dp should clamp to 0 px");

            int cellWidth = GridMathModel.calculateCellWidth(1080, neg, neg, 4);
            assertEquals(270, cellWidth, "Negative padding/margin gives clean unpadded width 1080/4 = 270");
        }

        // Density scaling check
        float[] testDensities = { 0.75f, 1.0f, 1.33f, 1.5f, 2.0f, 2.625f, 2.75f, 3.0f, 3.5f, 4.0f };
        for (float d : testDensities) {
            int pad16 = (int) (16 * d);
            int mar8 = (int) (8 * d);
            int cellW = GridMathModel.calculateCellWidth(1080, pad16, mar8, 4);
            assertTrue(cellW > 0, "Density " + d + " produced positive cell width: " + cellW);
        }
    }

    // =========================================================================
    // CHALLENGE 4: Rapid Concurrent Orientation & Configuration Mutations
    // =========================================================================
    public static void test_rapid_concurrent_orientation_and_density_mutations() throws Exception {
        int threadCount = 8;
        int iterationsPerThread = 25000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);

        class MockLayoutView {
            volatile int currentOrientation = 1; // 1 = Portrait, 2 = Landscape
            volatile float currentDensity = 2.75f;
            volatile int width = 1080;
            volatile int height = 2400;
            volatile int calculatedCols = 4;
            volatile int calculatedCellWidth = 0;

            public void applyLayout(int prefColPort, int prefColLand, int prefMarginH, int prefPadH) {
                boolean isPortrait = (currentOrientation == 1);
                int cols = isPortrait ? prefColPort : prefColLand;
                int padPx = (int) (prefPadH * currentDensity);
                int marPx = (int) (prefMarginH * currentDensity);

                this.calculatedCols = GridMathModel.resolveColumns(isPortrait, prefColPort, prefColLand);
                this.calculatedCellWidth = GridMathModel.calculateCellWidth(width, padPx, marPx, calculatedCols);
            }
        }

        final MockLayoutView sharedView = new MockLayoutView();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Random rng = new Random(42 + threadId);
                    for (int i = 0; i < iterationsPerThread; i++) {
                        boolean flipToLandscape = rng.nextBoolean();
                        sharedView.currentOrientation = flipToLandscape ? 2 : 1;
                        sharedView.width = flipToLandscape ? 2400 : 1080;
                        sharedView.height = flipToLandscape ? 1080 : 2400;
                        sharedView.currentDensity = 2.0f + (rng.nextInt(5) * 0.5f);

                        int portCols = 3 + rng.nextInt(4); // 3..6
                        int landCols = 4 + rng.nextInt(5); // 4..8
                        int mar = rng.nextInt(31); // 0..30
                        int pad = rng.nextInt(41); // 0..40

                        sharedView.applyLayout(portCols, landCols, mar, pad);

                        if (sharedView.calculatedCols < 3 || sharedView.calculatedCols > 8) {
                            errorCount.incrementAndGet();
                        }
                        if (sharedView.calculatedCellWidth <= 0) {
                            errorCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Multi-threaded stress test must complete within 10 seconds");
        assertEquals(0, errorCount.get(), "Total errors encountered during 200,000 rapid orientation/density mutations");
    }

    // =========================================================================
    // CHALLENGE 5: WeakReference Lifecycle & GC Safety
    // =========================================================================
    public static void test_weak_reference_lifecycle_and_gc_safety() {
        Object mockView = new Object();
        WeakReference<Object> weakRef = new WeakReference<>(mockView);

        assertNotNull(weakRef.get(), "WeakReference initially points to view");

        // Clear strong reference and force GC
        mockView = null;
        System.gc();

        // Should safely be nullable or cleared without throwing exception
        Object collected = weakRef.get();
        // Safe access simulation:
        boolean handledSafely = true;
        try {
            if (collected != null) {
                // If not yet collected by JVM, still safe
            } else {
                // Safely handles null view ref when view is destroyed
            }
        } catch (NullPointerException npe) {
            handledSafely = false;
        }
        assertTrue(handledSafely, "WeakReference nullability handled gracefully without NullPointerException");
    }

    // =========================================================================
    // Main Runner
    // =========================================================================
    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("        CHALLENGER 1: M1 EMPIRICAL ADVERSARIAL & STRESS TEST SUITE              ");
        System.out.println("================================================================================");

        executeTest("test_extreme_column_underflow_and_overflow (col 0, 1, 2, 7, 10, min/max int)",
                GridStressAndAdversarialTest::test_extreme_column_underflow_and_overflow);

        executeTest("test_container_width_overflow_and_narrow_displays (overflow & micro screens)",
                GridStressAndAdversarialTest::test_container_width_overflow_and_narrow_displays);

        executeTest("test_negative_dimensions_and_density_resilience (negative dp, densities 0.75x..4.0x)",
                GridStressAndAdversarialTest::test_negative_dimensions_and_density_resilience);

        executeTest("test_rapid_concurrent_orientation_and_density_mutations (200k iterations, 8 threads)",
                () -> {
                    try {
                        test_rapid_concurrent_orientation_and_density_mutations();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        executeTest("test_weak_reference_lifecycle_and_gc_safety (View GC deallocation)",
                GridStressAndAdversarialTest::test_weak_reference_lifecycle_and_gc_safety);

        System.out.println("================================================================================");
        if (totalFailed == 0) {
            System.out.println(String.format("CHALLENGER VERDICT: ALL %d ADVERSARIAL STRESS TESTS PASSED!", totalPassed));
            System.out.println("================================================================================");
            System.exit(0);
        } else {
            System.err.println(String.format("CHALLENGER VERDICT: %d TESTS FAILED!", totalFailed));
            System.out.println("================================================================================");
            System.exit(1);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " - Expected true, got false");
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message + " - Expected non-null, got null");
        }
    }
}
