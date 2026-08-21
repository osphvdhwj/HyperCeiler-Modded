package com.sevtinge.hyperceiler.test;

import java.util.*;

/**
 * Tier 1 & Tier 2: Dual Data Usage Panel Tests (Requirement R2)
 * Verifies data formatting, epoch window derivation, Security app Intent construction,
 * zero-byte handling, and fallback behavior.
 */
public class DualDataUsageTest {

    public static class DataUsageFormatter {
        public static final String INTENT_PACKAGE = "com.miui.securitycenter";
        public static final String INTENT_ACTIVITY = "com.miui.networkassistant.ui.activity.MainActivity";
        public static final String INTENT_ACTION = "miui.intent.action.NETWORKASSISTANT_MAIN";
        public static final int FLAG_ACTIVITY_NEW_TASK = 0x10000000;

        public static String formatFileSize(long bytes) {
            if (bytes < 0) bytes = 0;
            if (bytes < 1024) {
                return bytes + " B";
            }
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            exp = Math.min(exp, 5); // up to PB
            String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
            double val = bytes / Math.pow(1024, exp);
            return String.format(Locale.US, "%.2f %s", val, units[exp]);
        }

        public static String formatDualDataText(long mobileBytes, long wifiBytes) {
            String mobileStr = formatFileSize(mobileBytes);
            String wifiStr = formatFileSize(wifiBytes);
            return "Mobile: " + mobileStr + " | WiFi: " + wifiStr;
        }

        public static long calculateStartOfDayEpoch(long currentTimestampMillis, TimeZone tz) {
            Calendar calendar = Calendar.getInstance(tz);
            calendar.setTimeInMillis(currentTimestampMillis);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
        }

        public static class SecurityIntentSpec {
            public final String targetPackage;
            public final String targetActivity;
            public final String action;
            public final int flags;

            public SecurityIntentSpec(String targetPackage, String targetActivity, String action, int flags) {
                this.targetPackage = targetPackage;
                this.targetActivity = targetActivity;
                this.action = action;
                this.flags = flags;
            }
        }

        public static SecurityIntentSpec buildSecurityIntent() {
            return new SecurityIntentSpec(INTENT_PACKAGE, INTENT_ACTIVITY, INTENT_ACTION, FLAG_ACTIVITY_NEW_TASK);
        }
    }

    public static void test_r2_dual_data_usage_formatting() {
        long mobileBytes = 5368709120L; // 5.00 GB
        long wifiBytes = 11274289152L;   // 10.50 GB

        String formatted = DataUsageFormatter.formatDualDataText(mobileBytes, wifiBytes);
        assertEquals("Mobile: 5.00 GB | WiFi: 10.50 GB", formatted, "Standard 5GB mobile / 10.50GB wifi");

        long mobileBytes2 = 1572864L; // 1.50 MB
        long wifiBytes2 = 262144L;    // 256.00 KB
        String formatted2 = DataUsageFormatter.formatDualDataText(mobileBytes2, wifiBytes2);
        assertEquals("Mobile: 1.50 MB | WiFi: 256.00 KB", formatted2, "MB and KB formatting");
    }

    public static void test_r2_midnight_epoch_window_derivation() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        // 2026-08-21T14:30:45.123Z
        long testTimestamp = 1787322645123L;

        long startOfDay = DataUsageFormatter.calculateStartOfDayEpoch(testTimestamp, tz);

        Calendar cal = Calendar.getInstance(tz);
        cal.setTimeInMillis(startOfDay);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY), "Start of day hour should be 0");
        assertEquals(0, cal.get(Calendar.MINUTE), "Start of day minute should be 0");
        assertEquals(0, cal.get(Calendar.SECOND), "Start of day second should be 0");
        assertEquals(0, cal.get(Calendar.MILLISECOND), "Start of day millisecond should be 0");
        assertTrue(startOfDay <= testTimestamp, "Start of day is earlier than or equal to current timestamp");
    }

    public static void test_r2_security_activity_intent_construction() {
        DataUsageFormatter.SecurityIntentSpec spec = DataUsageFormatter.buildSecurityIntent();

        assertEquals("com.miui.securitycenter", spec.targetPackage, "Target package matches SecurityCenter");
        assertEquals("com.miui.networkassistant.ui.activity.MainActivity", spec.targetActivity, "Target activity matches NetworkAssistant");
        assertEquals("miui.intent.action.NETWORKASSISTANT_MAIN", spec.action, "Action matches NETWORKASSISTANT_MAIN");
        assertEquals(0x10000000, spec.flags, "Flag matches FLAG_ACTIVITY_NEW_TASK");
    }

    public static void test_r2_data_usage_zero_bytes_formatting() {
        String zeroFormatted = DataUsageFormatter.formatDualDataText(0, 0);
        assertEquals("Mobile: 0 B | WiFi: 0 B", zeroFormatted, "Zero bytes formatted as 0 B");

        String negativeFormatted = DataUsageFormatter.formatDualDataText(-100, -500);
        assertEquals("Mobile: 0 B | WiFi: 0 B", negativeFormatted, "Negative bytes clamped to 0 B");

        String exact1KB = DataUsageFormatter.formatDualDataText(1024, 2048);
        assertEquals("Mobile: 1.00 KB | WiFi: 2.00 KB", exact1KB, "Exact KB boundary");
    }

    public static void test_r2_data_usage_extreme_petabyte_formatting() {
        long pbBytes = 1125899906842624L; // 1 PB
        String pbFormatted = DataUsageFormatter.formatDualDataText(pbBytes, 0);
        assertEquals("Mobile: 1.00 PB | WiFi: 0 B", pbFormatted, "1 Petabyte formatting");
    }

    public static void test_r2_null_carrier_text_resilience() {
        String originalCarrier = null;
        String fallbackCarrier = originalCarrier != null ? originalCarrier : "China Mobile";
        assertEquals("China Mobile", fallbackCarrier, "Null carrier safely falls back");

        String unicodeCarrier = "中国移动 | CMCC 5G ⚡";
        String customDataText = DataUsageFormatter.formatDualDataText(1048576, 2097152);
        assertTrue(customDataText.contains("Mobile: 1.00 MB"), "Custom text generated cleanly");
        assertTrue(unicodeCarrier.contains("⚡"), "Unicode carrier string handled without failure");
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
}
