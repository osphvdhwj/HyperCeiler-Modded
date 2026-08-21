package com.sevtinge.hyperceiler.test;

import java.util.*;

/**
 * Tier 1 & Tier 2: Preferences Contract & Mapping Tests
 * Verifies preference parsing, key namespacing with "prefs_key_", default values,
 * type safety, and fallback handling for R1, R2, R3, and R4.
 */
public class PrefsContractTest {

    public static class PrefsMap<K, V> extends HashMap<K, V> {
        public Object getObject(String key, Object defValue) {
            return get(key) == null ? defValue : get(key);
        }

        public int getInt(String key, int defValue) {
            key = "prefs_key_" + key;
            Object val = get(key);
            if (val == null) return defValue;
            if (val instanceof Integer) return (Integer) val;
            if (val instanceof Number) return ((Number) val).intValue();
            if (val instanceof String) {
                try {
                    return Integer.parseInt((String) val);
                } catch (NumberFormatException e) {
                    return defValue;
                }
            }
            return defValue;
        }

        public String getString(String key, String defValue) {
            key = "prefs_key_" + key;
            Object val = get(key);
            return val == null ? defValue : String.valueOf(val);
        }

        public int getStringAsInt(String key, int defValue) {
            key = "prefs_key_" + key;
            Object val = get(key);
            if (val == null) return defValue;
            try {
                return Integer.parseInt(String.valueOf(val));
            } catch (NumberFormatException e) {
                return defValue;
            }
        }

        @SuppressWarnings("unchecked")
        public Set<String> getStringSet(String key) {
            key = "prefs_key_" + key;
            Object val = get(key);
            return val == null ? new LinkedHashSet<>() : (Set<String>) val;
        }

        public boolean getBoolean(String key) {
            key = "prefs_key_" + key;
            Object val = get(key);
            if (val == null) return false;
            if (val instanceof Boolean) return (Boolean) val;
            if (val instanceof String) return Boolean.parseBoolean((String) val);
            return false;
        }
    }

    public static void test_r1_preference_keys_and_defaults() {
        PrefsMap<String, Object> prefs = new PrefsMap<>();

        // Assert defaults when map is empty
        assertFalse(prefs.getBoolean("system_ui_control_center_custom_grid"), "R1 custom grid should default to false");
        assertEquals(4, prefs.getInt("system_ui_control_center_grid_columns", 4), "R1 grid columns default to 4");
        assertEquals(12, prefs.getInt("system_ui_control_center_tile_margin_h", 12), "R1 margin H default to 12");
        assertEquals(16, prefs.getInt("system_ui_control_center_tile_margin_v", 16), "R1 margin V default to 16");
        assertEquals(16, prefs.getInt("system_ui_control_center_grid_padding_h", 16), "R1 padding H default to 16");

        // Mutate and verify
        prefs.put("prefs_key_system_ui_control_center_custom_grid", true);
        prefs.put("prefs_key_system_ui_control_center_grid_columns", 5);
        prefs.put("prefs_key_system_ui_control_center_tile_margin_h", 18);
        prefs.put("prefs_key_system_ui_control_center_tile_margin_v", 20);
        prefs.put("prefs_key_system_ui_control_center_grid_padding_h", 24);

        assertTrue(prefs.getBoolean("system_ui_control_center_custom_grid"), "R1 custom grid should be true");
        assertEquals(5, prefs.getInt("system_ui_control_center_grid_columns", 4), "R1 columns should be 5");
        assertEquals(18, prefs.getInt("system_ui_control_center_tile_margin_h", 12), "R1 margin H should be 18");
        assertEquals(20, prefs.getInt("system_ui_control_center_tile_margin_v", 16), "R1 margin V should be 20");
        assertEquals(24, prefs.getInt("system_ui_control_center_grid_padding_h", 16), "R1 padding H should be 24");
    }

    public static void test_r2_preference_keys_and_defaults() {
        PrefsMap<String, Object> prefs = new PrefsMap<>();
        assertFalse(prefs.getBoolean("system_ui_control_center_dual_data_usage"), "R2 dual data usage default to false");

        prefs.put("prefs_key_system_ui_control_center_dual_data_usage", true);
        assertTrue(prefs.getBoolean("system_ui_control_center_dual_data_usage"), "R2 dual data usage should be true");
    }

    public static void test_r3_preference_keys_and_defaults() {
        PrefsMap<String, Object> prefs = new PrefsMap<>();
        assertFalse(prefs.getBoolean("system_ui_control_center_rounded_rect"), "R3 rounded rect default to false");
        assertEquals(72, prefs.getInt("system_ui_control_center_rounded_rect_radius", 72), "R3 radius default to 72");
        assertEquals(100, prefs.getInt("system_ui_control_center_tile_scale", 100), "R3 scale default to 100");

        prefs.put("prefs_key_system_ui_control_center_rounded_rect", true);
        prefs.put("prefs_key_system_ui_control_center_rounded_rect_radius", 45);
        prefs.put("prefs_key_system_ui_control_center_tile_scale", 85);

        assertTrue(prefs.getBoolean("system_ui_control_center_rounded_rect"), "R3 rounded rect should be true");
        assertEquals(45, prefs.getInt("system_ui_control_center_rounded_rect_radius", 72), "R3 radius should be 45");
        assertEquals(85, prefs.getInt("system_ui_control_center_tile_scale", 100), "R3 scale should be 85");
    }

    public static void test_r4_preference_keys_and_defaults() {
        PrefsMap<String, Object> prefs = new PrefsMap<>();
        assertFalse(prefs.getBoolean("system_ui_control_center_ios_connectivity_platter"), "R4 platter default to false");

        prefs.put("prefs_key_system_ui_control_center_ios_connectivity_platter", true);
        assertTrue(prefs.getBoolean("system_ui_control_center_ios_connectivity_platter"), "R4 platter should be true");
    }

    public static void test_key_prefixing_mechanism() {
        PrefsMap<String, Object> prefs = new PrefsMap<>();
        // Storing with prefix directly
        prefs.put("prefs_key_custom_setting", "value_1");
        assertEquals("value_1", prefs.getString("custom_setting", "default"), "Automatic prefix lookup");
    }

    public static void test_prefs_type_mismatch_exception_safety() {
        PrefsMap<String, Object> prefs = new PrefsMap<>();

        // String stored in place of int
        prefs.put("prefs_key_test_str_int", "123");
        assertEquals(123, prefs.getInt("test_str_int", 0), "String '123' safely coerced to int 123");

        // Invalid string stored in place of int
        prefs.put("prefs_key_invalid_str_int", "not_a_number");
        assertEquals(42, prefs.getInt("invalid_str_int", 42), "Invalid string safely falls back to default 42");

        // String stored in place of boolean
        prefs.put("prefs_key_bool_str", "true");
        assertTrue(prefs.getBoolean("bool_str"), "String 'true' safely coerced to boolean true");

        // Null value safety
        prefs.put("prefs_key_null_val", null);
        assertEquals("default_str", prefs.getString("null_val", "default_str"), "Null safely returns default");
    }

    public static void test_string_set_prefs() {
        PrefsMap<String, Object> prefs = new PrefsMap<>();
        Set<String> set = new LinkedHashSet<>(Arrays.asList("wifi", "cell", "bluetooth"));
        prefs.put("prefs_key_cards_set", set);

        Set<String> retrieved = prefs.getStringSet("cards_set");
        assertEquals(3, retrieved.size(), "Set size should be 3");
        assertTrue(retrieved.contains("wifi"), "Set should contain wifi");
        assertTrue(retrieved.contains("bluetooth"), "Set should contain bluetooth");
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
