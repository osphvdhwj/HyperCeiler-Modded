## 2026-08-21T13:56:45Z
You are Worker 1 for Milestone M4 (Requirement R4: iOS-Style Connectivity Platter).
Your Working Directory for metadata: /data/data/com.termux/files/home/HyperCeilerModded/.agents/m4_worker_1/
Project root: /data/data/com.termux/files/home/HyperCeilerModded
Read /data/data/com.termux/files/home/HyperCeilerModded/.agents/ORIGINAL_REQUEST.md, /data/data/com.termux/files/home/HyperCeilerModded/PROJECT.md, and /data/data/com.termux/files/home/HyperCeilerModded/.agents/survey_explorer_3/survey_report.md.

Task:
1. Implement `ConnectivityPlatterHook.kt` containing:
   - `ConnectivityPlatterView`: 2x2 compact ViewGroup platter grouping WiFi, Bluetooth, Mobile Data, Airplane Mode.
   - `ConnectivityExpandDialog`: Expandable modal dialog / folder triggered on long-press displaying 6 connectivity options (WiFi + SSID, Bluetooth + Connected Device, Cellular Data, Airplane Mode, Personal Hotspot, MiShare/NFC) with click actions to toggle and long-click to open system settings subpages.
   - `ConnectivityHostHelper`: Helper to query active states, resolve subtitles, and trigger tile actions.
   - Hook in `QSController` / `QSCardItemView` in plugin ClassLoader.
2. Register `ConnectivityPlatterHook` in `NewPluginHelperKt.kt` under the plugin loader list for `MiuiControlCenter` and `LocalMiuiQSTilePlugin` when `system_ui_control_center_ios_connectivity_platter` (or `system_ui_control_center_connectivity_platter`) is enabled.
3. Add `SwitchPreference` (`prefs_key_system_ui_control_center_ios_connectivity_platter`) under a Connectivity Folder category in `system_ui_control_center.xml` and bind in `ControlCenterSettings.java`.
4. Add localized strings in `strings_app.xml` and `values-zh-rCN/strings_app.xml`.
5. Verify with `./tools/run_e2e_tests.sh` and compile with:
   `proot -b /data/data/com.termux/files/usr/glibc/lib:/lib -b /data/data/com.termux/files/usr/tmp:/tmp env JAVA_HOME=/data/data/com.termux/files/home/jdk-17.0.12+7 PATH=/data/data/com.termux/files/home/jdk-17.0.12+7/bin:/data/data/com.termux/files/usr/bin ./gradlew -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp -Dorg.gradle.jvmargs="-Xmx3072m -Dfile.encoding=UTF-8 -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp" assembleDebug`
6. Save handoff report in `/data/data/com.termux/files/home/HyperCeilerModded/.agents/m4_worker_1/handoff.md` and send a message.
