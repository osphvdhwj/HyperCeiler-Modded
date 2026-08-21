# Handoff Report: Survey Explorer 3 (R2 & R4)

**Role**: Explorer 3 (`survey_explorer_3`)  
**Task**: Survey and Technical Architecture Design for Requirement R2 (Dual Data Usage Panel) and Requirement R4 (iOS-Style Connectivity Platter)  
**Status**: Complete (Hard Handoff)  
**Date**: 2026-08-21  

---

## 1. Observation

1. **SystemUI Plugin Architecture & ClassLoader Dispatching**:
   - In `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt` (lines 58-71, 116-175):
     ```kotlin
     loadClass("com.android.systemui.shared.plugins.PluginInstance\$PluginFactory")
         .methodFinder().filterByName("createPluginContext")
         .first().createAfterHook { it ->
     ```
     Loads `LocalMiuiQSTilePlugin` and `MiuiControlCenter` plugins and passes `classLoader` to registered feature loaders like `CustomCardTiles`, `CCGridForHyperOSKt`, `QSColor`, `QsTileSuperBlur`.
2. **Control Center Card Styles & Interception**:
   - In `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/CustomCardTiles.java` (lines 51-64):
     `findAndHookMethod("miui.systemui.controlcenter.qs.QSController", classLoader, "getCardStyleTileSpecs", ...)`
     `findAndHookConstructor("miui.systemui.controlcenter.qs.tileview.QSCardItemView", classLoader, Context.class, AttributeSet.class, ...)`
3. **Header, Carrier Text, and Status Bar Hooks**:
   - In `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/HideDelimiter.java` (lines 270-299):
     `findAndHookMethodSilently("com.android.systemui.controlcenter.phone.widget.ControlCenterStatusBar", "onFinishInflate", ...)`
     `findAndHookMethodSilently("com.android.systemui.qs.MiuiQSHeaderView", "updateCarrierVisibility", ...)`
     Uses `carrierText` / `mCarrierText` of type `TextView`.
4. **Network Speed & Traffic Queries**:
   - In `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/statusbar/network/NewNetworkSpeed.kt` (lines 80-97):
     Uses `android.net.TrafficStats` and `ConnectivityManager` to inspect active transports (`TRANSPORT_WIFI`, `TRANSPORT_CELLULAR`).
5. **SecurityCenter Integration**:
   - In `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/securitycenter/app/AppRestrict.java` (lines 55-63):
     `com.miui.networkassistant` is embedded in `com.miui.securitycenter`. Main entry component: `com.miui.securitycenter/com.miui.networkassistant.ui.activity.MainActivity` with intent action `miui.intent.action.NETWORKASSISTANT_MAIN`.
6. **UI Preferences Layout**:
   - `library/common-ui/java/main/res/xml/system_ui_control_center.xml` and `library/common-ui/java/main/res/xml/system_ui_control_center_tiles.xml` provide the XML screens managed by `ControlCenterSettings.java` and `TileSettings.java`.

---

## 2. Logic Chain

1. **R2 Dual Data Usage Logic**:
   - *Premise (from Obs 3 & 4)*: SystemUI hosts `ControlCenterStatusBar` and `MiuiQSHeaderView`, each managing a header `TextView` for carrier/flare data. SystemUI runs with UID 1000 and can access `android.app.usage.NetworkStatsManager` directly.
   - *Step 1*: Hooking `ControlCenterStatusBar.onFinishInflate` and `updateFlaresInfo` / `MiuiQSHeaderView.onFinishInflate` exposes the carrier/data `TextView`.
   - *Step 2*: Querying `NetworkStatsManager.querySummaryForDevice` for `TYPE_MOBILE` and `TYPE_WIFI` from today's midnight (`00:00:00`) yields exact mobile and Wi-Fi data byte totals.
   - *Step 3*: Formatting with `Formatter.formatShortFileSize(context, bytes)` produces the target string `"Mobile: " + mobileStr + " | WiFi: " + wifiStr`.
   - *Step 4 (from Obs 5)*: Setting `view.setOnLongClickListener` on this `TextView` dispatches an `Intent` with component `com.miui.securitycenter/com.miui.networkassistant.ui.activity.MainActivity` (action `miui.intent.action.NETWORKASSISTANT_MAIN`) dismissing keyguard/panel, satisfying R2.

2. **R4 iOS-Style Connectivity Platter Logic**:
   - *Premise (from Obs 1 & 2)*: Modern Control Center cards in HyperOS are created via `QSController` and `QSCardsController` inside `miui.systemui.plugin`.
   - *Step 1*: In `QSController.getCardStyleTileSpecs`, replace the separate `"wifi"` and `"cell"` specs with a composite `"connectivity_folder"` spec or insert a composite platter item.
   - *Step 2*: In `QSCardsController` / `QSCardItemView`, construct `ConnectivityPlatterView` containing a 2x2 grid with 4 mini toggle buttons (Wi-Fi, Bluetooth, Cellular, Airplane Mode).
   - *Step 3*: Tapping each button delegates directly to `QSTile.click(expandable)` or system toggle actions.
   - *Step 4*: Long-pressing `ConnectivityPlatterView` instantiates `ConnectivityExpandDialog` (a modal frosted-glass overlay displaying 6 full-detail connectivity rows with active SSID, Bluetooth device names, and settings redirection).

---

## 3. Caveats

1. **Multi-SIM Data Accounting**: `NetworkStatsManager.querySummaryForDevice(TYPE_MOBILE, null, ...)` aggregates all SIM cards when `subscriberId` is `null`. If per-SIM separation is desired in future iterations, subscriber IDs must be fetched via `SubscriptionManager`.
2. **Asynchronous Querying**: NetworkStats queries should be asynchronously cached or executed off the main UI thread to prevent any UI stutter when opening Control Center.
3. **Plugin vs Host View Types**: Custom platter views injected inside the plugin must use standard Android widgets (`LinearLayout`, `GridLayout`, `ImageView`, `TextView`) rather than custom HyperCeiler classes, avoiding classloader boundary issues.

---

## 4. Conclusion

Requirements R2 and R4 are technically straightforward and fully compatible with HyperCeiler's architecture:
- **R2 (Dual Data Usage Panel)** can be implemented via `DualDataUsageHook.kt` hooked into `SystemUiU.java`/`SystemUiV.java`, querying `NetworkStatsManager` and launching SecurityCenter's `MainActivity`.
- **R4 (iOS-Style Connectivity Platter)** can be implemented via `ConnectivityPlatterHook.kt` hooked in `NewPluginHelperKt.kt` targeting `miui.systemui.plugin`, rendering a 2x2 `ConnectivityPlatterView` that expands into a 6-item `ConnectivityExpandDialog` upon long-press.

---

## 5. Verification Method

To verify the architecture and findings independently:
1. Inspect the survey report at:
   `/data/data/com.termux/files/home/HyperCeilerModded/.agents/survey_explorer_3/survey_report.md`
2. Verify existing hook targets:
   - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt` (lines 116-175)
   - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/CustomCardTiles.java`
   - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/HideDelimiter.java`
3. Verify test compilation task via Gradle:
   `./gradlew assembleDebug` (or `./gradlew :library:hook:assembleDebug`)
