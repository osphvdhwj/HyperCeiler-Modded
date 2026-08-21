## 2026-08-21T13:48:00Z
You are Worker 1 for Milestone M2 (Requirement R2: Dual Data Usage Panel).
Your Working Directory for metadata: /data/data/com.termux/files/home/HyperCeilerModded/.agents/m2_worker_1/
Project root: /data/data/com.termux/files/home/HyperCeilerModded
Read /data/data/com.termux/files/home/HyperCeilerModded/.agents/ORIGINAL_REQUEST.md, /data/data/com.termux/files/home/HyperCeilerModded/PROJECT.md, and /data/data/com.termux/files/home/HyperCeilerModded/.agents/survey_explorer_3/survey_report.md.

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_preview_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

File Ownership for M2:
- `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/controlcenter/DualDataUsageHook.kt`
- `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/SystemUiV.java`
- `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/SystemUiU.java`
- `library/common-ui/java/main/res/xml/system_ui_control_center.xml`
- `library/common-ui/java/main/src/com/sevtinge/hyperceiler/ui/hooker/systemui/ControlCenterSettings.java`
- `library/common-ui/java/main/res/values/strings_app.xml`
- `library/common-ui/java/main/res/values-zh-rCN/strings_app.xml`

Task:
1. Create `DualDataUsageHook.kt` implementing the hooks for `ControlCenterStatusBar` and `MiuiQSHeaderView`.
   - Read preference `system_ui_control_center_dual_data_usage`.
   - Calculate start-of-day Mobile & WiFi data usage via `NetworkStatsManager` with `TrafficStats` fallback.
   - Format data text as `"Mobile: %s | WiFi: %s"`.
   - Inject text into carrier/data TextView.
   - Set `OnLongClickListener` to launch SecurityCenter NetworkAssistant activity (`com.miui.securitycenter/com.miui.networkassistant.ui.activity.MainActivity`, action `miui.intent.action.NETWORKASSISTANT_MAIN`, flag `FLAG_ACTIVITY_NEW_TASK`).
2. Register `DualDataUsageHook` in `SystemUiV.java` and `SystemUiU.java` under `initHook()`.
3. Add `SwitchPreference` (`prefs_key_system_ui_control_center_dual_data_usage`) in `system_ui_control_center.xml` and bind in `ControlCenterSettings.java`.
4. Add localized strings in `strings_app.xml` and `values-zh-rCN/strings_app.xml`.
5. Verify with `./tools/run_e2e_tests.sh` and compile with:
   `proot -b /data/data/com.termux/files/usr/glibc/lib:/lib -b /data/data/com.termux/files/usr/tmp:/tmp env JAVA_HOME=/data/data/com.termux/files/home/jdk-17.0.12+7 PATH=/data/data/com.termux/files/home/jdk-17.0.12+7/bin:/data/data/com.termux/files/usr/bin ./gradlew -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp -Dorg.gradle.jvmargs="-Xmx3072m -Dfile.encoding=UTF-8 -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp" assembleDebug`
6. Save handoff report in `/data/data/com.termux/files/home/HyperCeilerModded/.agents/m2_worker_1/handoff.md` and send a message.
