## 2026-08-21T16:57:01Z

You are Worker 2 for Milestone M4 (Requirement R4: iOS-Style Connectivity Platter).
Your Working Directory for metadata: /data/data/com.termux/files/home/HyperCeilerModded/.agents/m4_worker_2/
Project root: /data/data/com.termux/files/home/HyperCeilerModded
Read /data/data/com.termux/files/home/HyperCeilerModded/.agents/ORIGINAL_REQUEST.md, /data/data/com.termux/files/home/HyperCeilerModded/PROJECT.md, and /data/data/com.termux/files/home/HyperCeilerModded/.agents/survey_explorer_3/survey_report.md.

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_preview_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

File Ownership for M4:
- `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/ConnectivityPlatterHook.kt`
- `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt`
- `library/common-ui/java/main/res/xml/system_ui_control_center.xml`
- `library/common-ui/java/main/src/com/sevtinge/hyperceiler/ui/hooker/systemui/ControlCenterSettings.java`
- `library/common-ui/java/main/res/values/strings_app.xml`
- `library/common-ui/java/main/res/values-zh-rCN/strings_app.xml`

Task:
1. Implement `ConnectivityPlatterHook.kt` in `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/ConnectivityPlatterHook.kt`:
   - `ConnectivityHostHelper`: Queries states for Wi-Fi, Bluetooth, Mobile Data, Airplane Mode, Personal Hotspot, MiShare/NFC, toggles states via standard system/reflection calls, and opens settings intents.
   - `ConnectivityPlatterView`: 2x2 compact ViewGroup platter grouping WiFi, Bluetooth, Mobile Data, Airplane Mode with rounded acrylic background and long-press dialog trigger.
   - `ConnectivityExpandDialog`: Modal frosted-glass dialog showing 6 full-detail connectivity rows (WiFi + SSID, Bluetooth + Device, Cellular Data + Carrier, Airplane Mode, Hotspot, MiShare) with toggle clicks & long-press settings launches.
   - `ConnectivityPlatterHook`: Hooks `QSController.getCardStyleTileSpecs` and `QSCardItemView` in plugin ClassLoader to insert/manage the platter.
2. Register `ConnectivityPlatterHook` in `NewPluginHelperKt.kt` under the plugin loader list for `MiuiControlCenter` and `LocalMiuiQSTilePlugin` when `system_ui_control_center_ios_connectivity_platter` is enabled.
3. Add `SwitchPreference` (`prefs_key_system_ui_control_center_ios_connectivity_platter`) under a Connectivity Folder category in `system_ui_control_center.xml` and bind in `ControlCenterSettings.java`.
4. Add localized strings in `strings_app.xml` and `values-zh-rCN/strings_app.xml`.
5. Verify with `./tools/run_e2e_tests.sh` and compile with:
   `proot -b /data/data/com.termux/files/usr/glibc/lib:/lib -b /data/data/com.termux/files/usr/tmp:/tmp env JAVA_HOME=/data/data/com.termux/files/home/jdk-17.0.12+7 PATH=/data/data/com.termux/files/home/jdk-17.0.12+7/bin:/data/data/com.termux/files/usr/bin ./gradlew -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp -Dorg.gradle.jvmargs="-Xmx3072m -Dfile.encoding=UTF-8 -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp" assembleDebug`
6. Save handoff report in `/data/data/com.termux/files/home/HyperCeilerModded/.agents/m4_worker_2/handoff.md` and send a message.
