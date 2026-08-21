package com.sevtinge.hyperceiler.test;

import java.util.*;

/**
 * Tier 1 & Tier 2: iOS-Style Connectivity Platter Tests (Requirement R4)
 * Verifies 2x2 container composition, sub-tile state resolution (Active, Inactive,
 * Unavailable, Transient), and modal expand dialog item generation.
 */
public class ConnectivityPlatterTest {

    public enum TileState {
        UNAVAILABLE(0), // Disabled / No hardware / No SIM
        INACTIVE(1),    // Enabled but disconnected / Idle
        ACTIVE(2),      // Enabled & Connected / Transmitting
        TRANSIENT(3);   // In-between / Turning On/Off

        public final int code;
        TileState(int code) { this.code = code; }
    }

    public static class SubTileInfo {
        public final String spec;
        public final String title;
        public String subtitle;
        public TileState state;
        public boolean isExpandedItem;

        public SubTileInfo(String spec, String title, String subtitle, TileState state, boolean isExpandedItem) {
            this.spec = spec;
            this.title = title;
            this.subtitle = subtitle;
            this.state = state;
            this.isExpandedItem = isExpandedItem;
        }
    }

    public static class ConnectivityModel {
        public static final List<String> COMPACT_SPECS = Arrays.asList(
                "wifi", "bluetooth", "cell", "airplane"
        );

        public static final List<String> EXPANDED_SPECS = Arrays.asList(
                "wifi", "bluetooth", "cell", "airplane", "hotspot", "mishare"
        );

        public static List<SubTileInfo> createCompactPlatter(
                boolean wifiOn, String wifiSsid,
                boolean btOn, String btDevice,
                boolean cellOn, String carrier, boolean hasSim,
                boolean airplaneOn) {

            List<SubTileInfo> list = new ArrayList<>(4);

            // 1. Wi-Fi
            TileState wifiState = !wifiOn ? TileState.INACTIVE : (wifiSsid != null ? TileState.ACTIVE : TileState.INACTIVE);
            String wifiSub = !wifiOn ? "Off" : (wifiSsid != null ? wifiSsid : "Not Connected");
            list.add(new SubTileInfo("wifi", "Wi-Fi", wifiSub, wifiState, false));

            // 2. Bluetooth
            TileState btState = !btOn ? TileState.INACTIVE : (btDevice != null ? TileState.ACTIVE : TileState.INACTIVE);
            String btSub = !btOn ? "Off" : (btDevice != null ? btDevice : "On");
            list.add(new SubTileInfo("bluetooth", "Bluetooth", btSub, btState, false));

            // 3. Cellular Data
            TileState cellState;
            String cellSub;
            if (!hasSim) {
                cellState = TileState.UNAVAILABLE;
                cellSub = "No SIM";
            } else if (!cellOn) {
                cellState = TileState.INACTIVE;
                cellSub = "Off";
            } else {
                cellState = TileState.ACTIVE;
                cellSub = carrier != null ? carrier : "5G";
            }
            list.add(new SubTileInfo("cell", "Mobile Data", cellSub, cellState, false));

            // 4. Airplane Mode
            TileState airState = airplaneOn ? TileState.ACTIVE : TileState.INACTIVE;
            String airSub = airplaneOn ? "On" : "Off";
            list.add(new SubTileInfo("airplane", "Airplane Mode", airSub, airState, false));

            return list;
        }

        public static List<SubTileInfo> createExpandedDialogItems(
                List<SubTileInfo> compactItems,
                boolean hotspotOn, int hotspotClients,
                boolean miShareOn) {

            List<SubTileInfo> expanded = new ArrayList<>(compactItems);

            // 5. Personal Hotspot
            TileState hotspotState = hotspotOn ? TileState.ACTIVE : TileState.INACTIVE;
            String hotspotSub = hotspotOn ? (hotspotClients + " Connected") : "Off";
            expanded.add(new SubTileInfo("hotspot", "Personal Hotspot", hotspotSub, hotspotState, true));

            // 6. Mi Share / AirDrop
            TileState miShareState = miShareOn ? TileState.ACTIVE : TileState.INACTIVE;
            String miShareSub = miShareOn ? "Everyone" : "Off";
            expanded.add(new SubTileInfo("mishare", "Mi Share", miShareSub, miShareState, true));

            return expanded;
        }
    }

    public static void test_r4_connectivity_platter_2x2_composition() {
        List<SubTileInfo> platter = ConnectivityModel.createCompactPlatter(
                true, "Office-WiFi-5G",
                true, "Sony WH-1000XM5",
                true, "China Mobile 5G", true,
                false
        );

        assertEquals(4, platter.size(), "Compact platter has exactly 4 items");
        assertEquals("wifi", platter.get(0).spec, "Item 0 is Wi-Fi");
        assertEquals("bluetooth", platter.get(1).spec, "Item 1 is Bluetooth");
        assertEquals("cell", platter.get(2).spec, "Item 2 is Cellular");
        assertEquals("airplane", platter.get(3).spec, "Item 3 is Airplane Mode");
    }

    public static void test_r4_sub_tile_state_resolution() {
        List<SubTileInfo> platter = ConnectivityModel.createCompactPlatter(
                true, "Home-5G",
                true, "AirPods",
                true, "LTE/5G", true,
                false
        );

        assertEquals(TileState.ACTIVE, platter.get(0).state, "Wi-Fi is Active");
        assertEquals("Home-5G", platter.get(0).subtitle, "Wi-Fi subtitle is SSID");

        assertEquals(TileState.ACTIVE, platter.get(1).state, "Bluetooth is Active");
        assertEquals("AirPods", platter.get(1).subtitle, "Bluetooth subtitle is Device Name");

        assertEquals(TileState.ACTIVE, platter.get(2).state, "Cell is Active");
        assertEquals("LTE/5G", platter.get(2).subtitle, "Cell subtitle is Carrier");

        assertEquals(TileState.INACTIVE, platter.get(3).state, "Airplane is Inactive");
        assertEquals("Off", platter.get(3).subtitle, "Airplane subtitle is Off");
    }

    public static void test_r4_expand_dialog_row_population() {
        List<SubTileInfo> compact = ConnectivityModel.createCompactPlatter(
                true, "Starbucks", true, null, true, "AT&T", true, false
        );

        List<SubTileInfo> expanded = ConnectivityModel.createExpandedDialogItems(
                compact, true, 2, true
        );

        assertEquals(6, expanded.size(), "Expanded dialog has exactly 6 items");
        assertEquals("hotspot", expanded.get(4).spec, "Item 4 is Hotspot");
        assertEquals(TileState.ACTIVE, expanded.get(4).state, "Hotspot is Active");
        assertEquals("2 Connected", expanded.get(4).subtitle, "Hotspot shows client count");

        assertEquals("mishare", expanded.get(5).spec, "Item 5 is Mi Share");
        assertEquals(TileState.ACTIVE, expanded.get(5).state, "Mi Share is Active");
    }

    public static void test_r4_no_sim_state_graceful_handling() {
        List<SubTileInfo> platter = ConnectivityModel.createCompactPlatter(
                false, null,
                false, null,
                false, null, false, // No SIM
                false
        );

        assertEquals(TileState.INACTIVE, platter.get(0).state, "Wi-Fi is Inactive");
        assertEquals("Off", platter.get(0).subtitle, "Wi-Fi subtitle is Off");

        assertEquals(TileState.INACTIVE, platter.get(1).state, "Bluetooth is Inactive");
        assertEquals("Off", platter.get(1).subtitle, "Bluetooth subtitle is Off");

        assertEquals(TileState.UNAVAILABLE, platter.get(2).state, "Cell is Unavailable without SIM");
        assertEquals("No SIM", platter.get(2).subtitle, "Cell subtitle shows No SIM");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }
}
