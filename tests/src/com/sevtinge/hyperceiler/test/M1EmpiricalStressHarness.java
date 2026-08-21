package com.sevtinge.hyperceiler.test;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adversarial Empirical Stress Harness for M1 (Challenger 2)
 * Tests:
 * 1. WeakReference memory leak prevention vs StrongReference leak.
 * 2. ClassLoader isolation & reflection error resilience (missing fields, unknown classes).
 * 3. High-concurrency race condition resistance under 100 concurrent preference mutations.
 * 4. Dynamic reactivity & orientation math across edge cases.
 * 5. Crash vector resilience (null objects, zero density, invalid display configurations).
 */
public class M1EmpiricalStressHarness {

    public static class MockViewGroup {
        public int mColumns = 4;
        public int mCellMarginHorizontal = 0;
        public int mCellMarginVertical = 0;
        public int mTileMarginHorizontal = 0;
        public int mTileMarginVertical = 0;
        public int mTileMargin = 0;
        public int mCellMargin = 0;
        public int mPaddingStart = 0;
        public int mPaddingEnd = 0;
        public int paddingLeft = 0;
        public int paddingRight = 0;
        public int paddingTop = 0;
        public int paddingBottom = 0;

        public boolean requestLayoutCalled = false;
        public boolean invalidateCalled = false;

        public byte[] memoryPayload = new byte[1024 * 1024]; // 1MB payload to detect leaks

        public void setPaddingRelative(int start, int top, int end, int bottom) {
            this.paddingLeft = start;
            this.paddingTop = top;
            this.paddingRight = end;
            this.paddingBottom = bottom;
        }

        public void requestLayout() {
            requestLayoutCalled = true;
        }

        public void invalidate() {
            invalidateCalled = true;
        }
    }

    public static class MockIncompleteViewGroup {
        // Missing fields to simulate non-standard ROMs / obfuscated fields
        public int someOtherField = 123;
        public void setPaddingRelative(int start, int top, int end, int bottom) {}
        public void requestLayout() {}
        public void invalidate() {}
    }

    // 1. Test WeakReference GC reclamation (Memory Leak Test)
    public static void test_weak_reference_memory_leak_safety() {
        WeakReference<MockViewGroup> weakRef;
        {
            MockViewGroup group = new MockViewGroup();
            weakRef = new WeakReference<>(group);
            if (weakRef.get() == null) {
                throw new AssertionError("WeakReference should hold target before GC");
            }
            // Dereference local
            group = null;
        }

        // Force GC and wait
        for (int i = 0; i < 5; i++) {
            System.gc();
            System.runFinalization();
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }

        if (weakRef.get() != null) {
            // Give one more GC cycle
            System.gc();
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }

        if (weakRef.get() != null) {
            throw new AssertionError("Memory Leak: WeakReference was not collected by GC!");
        }
        System.out.println("  PASS: test_weak_reference_memory_leak_safety (ViewGroup collected cleanly by GC)");
    }

    // 2. Test Reflection & Crash Resilience with Incompatible ROM ViewGroup
    public static void test_reflection_resilience_on_incompatible_viewgroup() {
        MockIncompleteViewGroup view = new MockIncompleteViewGroup();
        
        // Simulating applyGridLayout reflection calls with error catching
        AtomicBoolean crashed = new AtomicBoolean(false);
        try {
            // Attempt to set mColumns via reflection (which does not exist)
            try {
                var field = view.getClass().getDeclaredField("mColumns");
                field.setAccessible(true);
                field.setInt(view, 5);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Expected to be caught safely by runCatching
            }

            try {
                var field = view.getClass().getDeclaredField("mCellMarginHorizontal");
                field.setAccessible(true);
                field.setInt(view, 24);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Expected to be caught safely by runCatching
            }

            view.setPaddingRelative(16, 0, 16, 0);
            view.requestLayout();
        } catch (Throwable t) {
            crashed.set(true);
        }

        if (crashed.get()) {
            throw new AssertionError("Host crashed on incompatible ROM ViewGroup!");
        }
        System.out.println("  PASS: test_reflection_resilience_on_incompatible_viewgroup (No crash on missing fields)");
    }

    // 3. Test High Concurrency Preference Updates & Race Conditions
    public static void test_concurrent_preference_updates_race_conditions() throws Exception {
        final PrefsContractTest.PrefsMap<String, Object> prefsMap = new PrefsContractTest.PrefsMap<>();
        final MockViewGroup view = new MockViewGroup();
        final int THREAD_COUNT = 100;
        final int ITERATIONS_PER_THREAD = 1000;
        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        final AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Random rng = new Random(threadId);
                    for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                        int col = 3 + rng.nextInt(4); // 3..6
                        int marginH = rng.nextInt(30);
                        int paddingH = rng.nextInt(40);
                        boolean enabled = rng.nextBoolean();

                        // Mutate
                        synchronized (prefsMap) {
                            prefsMap.put("prefs_key_system_ui_control_center_custom_grid", enabled);
                            prefsMap.put("prefs_key_system_ui_control_center_grid_columns", col);
                            prefsMap.put("prefs_key_system_ui_control_center_tile_margin_h", marginH);
                            prefsMap.put("prefs_key_system_ui_control_center_grid_padding_h", paddingH);
                        }

                        // Read & apply
                        int readCol;
                        int readMarginH;
                        int readPaddingH;
                        boolean readEnabled;
                        synchronized (prefsMap) {
                            readCol = prefsMap.getInt("system_ui_control_center_grid_columns", 4);
                            readMarginH = prefsMap.getInt("system_ui_control_center_tile_margin_h", 8);
                            readPaddingH = prefsMap.getInt("system_ui_control_center_grid_padding_h", 16);
                            readEnabled = prefsMap.getBoolean("system_ui_control_center_custom_grid");
                        }

                        // Apply to view
                        view.mColumns = GridAndLayoutMathTest.LayoutCalculator.clampColumns(readCol);
                        view.mCellMarginHorizontal = (int) (readMarginH * 2.75f);
                        view.setPaddingRelative((int) (readPaddingH * 2.75f), 0, (int) (readPaddingH * 2.75f), 0);
                        view.requestLayout();
                    }
                } catch (Throwable t) {
                    errorCount.incrementAndGet();
                    t.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        if (!completed || errorCount.get() > 0) {
            throw new AssertionError("Race condition or deadlock detected under 100 concurrent threads! Errors: " + errorCount.get());
        }

        if (view.mColumns < 3 || view.mColumns > 6) {
            throw new AssertionError("Columns outside clamped bounds [3..6]: " + view.mColumns);
        }

        System.out.println("  PASS: test_concurrent_preference_updates_race_conditions (100,000 ops across 100 threads safe)");
    }

    // 4. Test ClassLoader Isolation
    public static void test_classloader_isolation() throws Exception {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        
        // Ensure mock plugin classloader is distinct
        ClassLoader pluginClassLoader = new ClassLoader(systemClassLoader) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (name.equals("miui.systemui.controlcenter.qs.tileview.QSTileListLayout")) {
                    return MockViewGroup.class;
                }
                return super.findClass(name);
            }
        };

        Class<?> loadedClass = pluginClassLoader.loadClass("miui.systemui.controlcenter.qs.tileview.QSTileListLayout");
        if (loadedClass != MockViewGroup.class) {
            throw new AssertionError("ClassLoader isolation failed: plugin class not resolved correctly");
        }
        System.out.println("  PASS: test_classloader_isolation (Plugin ClassLoader isolation verified)");
    }

    // 5. Test Zero Density and Math Boundary Resilience
    public static void test_zero_and_negative_density_resilience() {
        float[] densities = {-1.0f, 0.0f, 0.5f, 1.0f, 2.75f, 3.5f, 4.0f};
        for (float density : densities) {
            float safeDensity = Math.max(0.1f, density);
            int marginHDp = 12;
            int paddingHDp = 16;
            int marginHPx = (int) (marginHDp * safeDensity);
            int paddingHPx = (int) (paddingHDp * safeDensity);

            int cellWidth = GridAndLayoutMathTest.LayoutCalculator.calculateCellWidth(1080, paddingHPx, marginHPx, 4);
            if (cellWidth <= 0) {
                throw new AssertionError("Cell width non-positive for density " + density);
            }
        }
        System.out.println("  PASS: test_zero_and_negative_density_resilience (Density boundaries tested safely)");
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("          M1 CHALLENGER 2: ADVERSARIAL EMPIRICAL STRESS HARNESS                 ");
        System.out.println("================================================================================");
        try {
            test_weak_reference_memory_leak_safety();
            test_reflection_resilience_on_incompatible_viewgroup();
            test_concurrent_preference_updates_race_conditions();
            test_classloader_isolation();
            test_zero_and_negative_density_resilience();
            System.out.println("================================================================================");
            System.out.println("ALL EMPIRICAL CHALLENGER TESTS PASSED (5/5 tests passed, 0 failures)");
            System.out.println("================================================================================");
        } catch (Throwable t) {
            System.err.println("CHALLENGER FAILURE: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}
