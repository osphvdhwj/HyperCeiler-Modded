package com.sevtinge.hyperceiler.test;

import java.util.*;

/**
 * Tier 1, Tier 2, Tier 3: Grid & Layout Calculation Tests (Requirement R1)
 * Verifies dynamic column layout math, padding, margins, boundary clamping,
 * child view bounds calculation, and orientation changes.
 */
public class GridAndLayoutMathTest {

    public static class LayoutCalculator {
        public static final int MIN_COLUMNS = 3;
        public static final int MAX_COLUMNS = 6;
        public static final int DEFAULT_COLUMNS = 4;

        public static int clampColumns(int requestedColumns) {
            if (requestedColumns < MIN_COLUMNS) return MIN_COLUMNS;
            if (requestedColumns > MAX_COLUMNS) return MAX_COLUMNS;
            return requestedColumns;
        }

        public static int calculateCellWidth(int containerWidth, int paddingH, int marginH, int columns) {
            int clampedCols = clampColumns(columns);
            int totalPadding = 2 * Math.max(0, paddingH);
            int availableWidth = containerWidth - totalPadding;
            if (availableWidth <= 0) {
                return 1; // Safe fallback to avoid division by zero or negative size
            }
            int totalMargins = (clampedCols - 1) * Math.max(0, marginH);
            int contentWidth = availableWidth - totalMargins;
            if (contentWidth <= 0) {
                return 1;
            }
            return contentWidth / clampedCols;
        }

        public static class Rect {
            public final int left;
            public final int top;
            public final int right;
            public final int bottom;

            public Rect(int left, int top, int right, int bottom) {
                this.left = left;
                this.top = top;
                this.right = right;
                this.bottom = bottom;
            }

            public int width() { return right - left; }
            public int height() { return bottom - top; }

            public boolean intersects(Rect other) {
                return left < other.right && right > other.left && top < other.bottom && bottom > other.top;
            }
        }

        public static List<Rect> computeChildBounds(
                int containerWidth, int paddingTop, int paddingH,
                int marginH, int marginV, int cellHeight, int columns, int itemCount) {

            int clampedCols = clampColumns(columns);
            int cellWidth = calculateCellWidth(containerWidth, paddingH, marginH, clampedCols);
            List<Rect> bounds = new ArrayList<>(itemCount);

            for (int i = 0; i < itemCount; i++) {
                int col = i % clampedCols;
                int row = i / clampedCols;

                int left = paddingH + col * (cellWidth + marginH);
                int top = paddingTop + row * (cellHeight + marginV);
                int right = left + cellWidth;
                int bottom = top + cellHeight;

                bounds.add(new Rect(left, top, right, bottom));
            }
            return bounds;
        }
    }

    public static void test_r1_grid_column_calculation() {
        int containerWidth = 1080;
        int paddingH = 48;
        int marginH = 24;
        int cols = 4;

        // availableWidth = 1080 - 2*48 = 984
        // totalMargins = 3 * 24 = 72
        // cellWidth = (984 - 72) / 4 = 912 / 4 = 228
        int cellWidth = LayoutCalculator.calculateCellWidth(containerWidth, paddingH, marginH, cols);
        assertEquals(228, cellWidth, "Standard 4-col cell width calculation");

        // 3 cols: avail = 1000, margins = 2*20 = 40 -> 960 / 3 = 320
        assertEquals(320, LayoutCalculator.calculateCellWidth(1080, 40, 20, 3), "3 cols cell width");

        // 5 cols: avail = 1000, margins = 4*20 = 80 -> 920 / 5 = 184
        assertEquals(184, LayoutCalculator.calculateCellWidth(1080, 40, 20, 5), "5 cols cell width");

        // 6 cols: avail = 1000, margins = 5*20 = 100 -> 900 / 6 = 150
        assertEquals(150, LayoutCalculator.calculateCellWidth(1080, 40, 20, 6), "6 cols cell width");
    }

    public static void test_r1_grid_margin_and_padding_spacing() {
        int containerWidth = 1080;
        int paddingTop = 20;
        int paddingH = 40;
        int marginH = 16;
        int marginV = 16;
        int cellHeight = 120;
        int cols = 4;
        int itemCount = 12;

        List<LayoutCalculator.Rect> bounds = LayoutCalculator.computeChildBounds(
                containerWidth, paddingTop, paddingH, marginH, marginV, cellHeight, cols, itemCount);

        assertEquals(12, bounds.size(), "All 12 items have bounds");

        // Verify bounds are within container horizontal limits
        for (int i = 0; i < bounds.size(); i++) {
            LayoutCalculator.Rect rect = bounds.get(i);
            assertTrue(rect.left >= paddingH, "Item " + i + " left within padding");
            assertTrue(rect.right <= containerWidth - paddingH + 2, "Item " + i + " right within container");
            assertEquals(cellHeight, rect.height(), "Item " + i + " height matches cellHeight");
        }

        // Verify no items overlap
        for (int i = 0; i < bounds.size(); i++) {
            for (int j = i + 1; j < bounds.size(); j++) {
                assertFalse(bounds.get(i).intersects(bounds.get(j)),
                        "Item " + i + " should not intersect item " + j);
            }
        }
    }

    public static void test_r1_grid_column_underflow_clamped() {
        assertEquals(3, LayoutCalculator.clampColumns(0), "Underflow 0 clamped to MIN 3");
        assertEquals(3, LayoutCalculator.clampColumns(-5), "Underflow -5 clamped to MIN 3");
        assertEquals(3, LayoutCalculator.clampColumns(1), "Underflow 1 clamped to MIN 3");
        assertEquals(3, LayoutCalculator.clampColumns(2), "Underflow 2 clamped to MIN 3");
    }

    public static void test_r1_grid_column_overflow_clamped() {
        assertEquals(6, LayoutCalculator.clampColumns(7), "Overflow 7 clamped to MAX 6");
        assertEquals(6, LayoutCalculator.clampColumns(10), "Overflow 10 clamped to MAX 6");
        assertEquals(6, LayoutCalculator.clampColumns(100), "Overflow 100 clamped to MAX 6");
        assertEquals(6, LayoutCalculator.clampColumns(999), "Overflow 999 clamped to MAX 6");
    }

    public static void test_r1_excessive_padding_safe_fallback() {
        // Zero padding and zero margin
        int width1 = LayoutCalculator.calculateCellWidth(1000, 0, 0, 4);
        assertEquals(250, width1, "Zero padding and margin width");

        // Excessive padding that exceeds container width
        int width2 = LayoutCalculator.calculateCellWidth(200, 150, 10, 4);
        assertTrue(width2 >= 1, "Excessive padding should return safe positive width >= 1");

        // Negative margins and padding handled safely
        int width3 = LayoutCalculator.calculateCellWidth(1000, -20, -10, 4);
        assertEquals(250, width3, "Negative margin/padding clamped to 0");
    }

    public static void test_orientation_change_remeasure() {
        // Portrait: 1080px width, 4 columns
        int portraitCellWidth = LayoutCalculator.calculateCellWidth(1080, 40, 20, 4);
        assertEquals(235, portraitCellWidth, "Portrait 4-col cell width");

        // Landscape: 2400px width, 6 columns
        int landscapeCellWidth = LayoutCalculator.calculateCellWidth(2400, 60, 24, 6);
        // avail = 2400 - 120 = 2280; margins = 5 * 24 = 120; 2160 / 6 = 360
        assertEquals(360, landscapeCellWidth, "Landscape 6-col cell width");
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

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message + " - Expected false, got true");
        }
    }
}
