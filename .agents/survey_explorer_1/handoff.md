# Handoff Report — Explorer 1 (survey_explorer_1)

**Working Directory**: `/data/data/com.termux/files/home/HyperCeilerModded/.agents/survey_explorer_1/`  
**Date**: 2026-08-21T12:56:00Z  
**Type**: Hard Handoff (Survey Phase Complete)  

---

## 1. Observation

1. **Gradle Build Configuration & Modules**:
   - `settings.gradle.kts`: Includes `:app`, `:library:common-ui`, `:library:hook`, `:library:processor`, `:library:hidden-api`.
   - `gradle/libs.versions.toml`: Configured with AGP `8.11.1`, Kotlin `2.2.0`, Java toolchain `21`, Xposed API `82`, EzXHelper `3.0.0`, DexKit `2.0.6`, HookTool `v.2.1.4`, Miuix `1.0.13.0`.
   - Build Execution Command:
     ```bash
     proot -b /data/data/com.termux/files/usr/glibc/lib:/lib \
           -b /data/data/com.termux/files/usr/tmp:/tmp \
           env JAVA_HOME=/data/data/com.termux/files/home/jdk-17.0.12+7 \
               PATH=/data/data/com.termux/files/home/jdk-17.0.12+7/bin:/data/data/com.termux/files/usr/bin \
           ./gradlew -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp \
                     -Dorg.gradle.jvmargs="-Xmx3072m -Dfile.encoding=UTF-8 -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp" \
                     assembleDebug
     ```
   - Result: `BUILD SUCCESSFUL in 47s`, 74 actionable tasks executed.

2. **Annotation Processor & Hook Registry**:
   - `library/processor/src/main/java/com/hchen/database/HookBaseProcessor.java`: Scans classes annotated with `@HookBase(targetPackage, targetSdk, targetOSVersion, isPad)` and writes `com.sevtinge.hyperceiler.module.base.DataBase`.
   - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/XposedInit.java` (lines 173-205): Matches package name and SDK version from `DataBase.get()` to load the matching `BaseModule` (e.g. `SystemUiV` for SDK 35, `SystemUiU` for SDK 34).

3. **Preferences & IPC**:
   - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/utils/prefs/PrefsMap.java` (lines 31-55): `getInt()`, `getBoolean()`, `getString()` automatically prepend `"prefs_key_"`.
   - Preferences storage path: `/data/user_de/0/com.harry.hyperhand/shared_prefs/hyperceiler_prefs.xml` exposed via `SharedPrefsProvider.java`.

4. **Dynamic SystemUI Plugin Hooks**:
   - `library/hook/src/main/java/com/sevtinge/hyperceiler/hook/module/hook/systemui/plugin/NewPluginHelperKt.kt` (lines 57-71, 116-175): Intercepts `PluginInstance$PluginFactory.createPluginContext()` and obtains the plugin `ClassLoader` for `miui.systemui.controlcenter.MiuiControlCenter` and `miui.systemui.quicksettings.LocalMiuiQSTilePlugin`.
   - Plugin contains:
     - `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` (Grid container for R1)
     - `miui.systemui.controlcenter.qs.tileview.QSTileItemIconView` (Tile shape & scale for R3)
     - `miui.systemui.controlcenter.qs.QSController` & `QSCardsController` (Card tiles & platter for R4)

5. **Host SystemUI Hooks**:
   - `ControlCenterStatusBar` and `MiuiQSHeaderView` in `com.android.systemui` host process handle carrier/data usage text (for R2).
   - `android.app.usage.NetworkStatsManager.querySummaryForDevice` aggregates today's mobile and Wi-Fi traffic.
   - Long press launches `com.miui.securitycenter/com.miui.networkassistant.ui.activity.MainActivity` (`miui.intent.action.NETWORKASSISTANT_MAIN`).

---

## 2. Logic Chain

1. From (1), the repository compiles cleanly and can be continuously verified using the tested `assembleDebug` command under proot with JDK 17.
2. From (2), any new hook class targeting SystemUI can either be registered as a standalone hook in `SystemUiV.java` / `SystemUiU.java` (for host SystemUI classes) or dispatched via `NewPluginHelperKt.kt` (for plugin classes).
3. From (3), UI sliders and switches defined in `system_ui_control_center.xml` or `system_ui_control_center_tiles.xml` with key `prefs_key_<name>` are immediately queryable in hook code as `mPrefsMap.getInt("<name>")` or `mPrefsMap.getBoolean("<name>")`.
4. From (4) and (5), Requirements R1, R3, and R4 belong to the plugin context (`miui.systemui.plugin`) and should be hooked via `NewPluginHelperKt.kt`, while Requirement R2 belongs to the host SystemUI process (`com.android.systemui`) and should be hooked via `SystemUiV.java` and `SystemUiU.java`.

---

## 3. Caveats

1. In Termux, executing `./gradlew` directly fails due to missing `/tmp` and glibc interpreter linkage; running via `proot -b /data/data/com.termux/files/usr/glibc/lib:/lib -b /data/data/com.termux/files/usr/tmp:/tmp ...` is required.
2. The exact method names in `QSTileListLayout` and `QSTileItemIconView` must be guarded with `runCatching` / `loadClassOrNull` to guarantee graceful fallback across HyperOS 1.0 vs HyperOS 2.0.
3. NetworkStatsManager queries should be executed on worker threads or debounced to avoid blocking main thread UI layout passes.

---

## 4. Conclusion

The repository architecture, build system, hook registration lifecycle, preferences IPC, and Control Center plugin dispatch mechanism have been thoroughly investigated and mapped. The survey findings provide all necessary class paths, method hooks, preference keys, and code implementations needed to execute the implementation phase for R1, R2, R3, and R4.

---

## 5. Verification Method

1. Inspect survey report:
   `view_file /data/data/com.termux/files/home/HyperCeilerModded/.agents/survey_explorer_1/survey_report.md`
2. Test Gradle build compilation:
   ```bash
   proot -b /data/data/com.termux/files/usr/glibc/lib:/lib \
         -b /data/data/com.termux/files/usr/tmp:/tmp \
         env JAVA_HOME=/data/data/com.termux/files/home/jdk-17.0.12+7 \
             PATH=/data/data/com.termux/files/home/jdk-17.0.12+7/bin:/data/data/com.termux/files/usr/bin \
         ./gradlew -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp \
                   -Dorg.gradle.jvmargs="-Xmx3072m -Dfile.encoding=UTF-8 -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp" \
                   assembleDebug
   ```
3. Verify output APK generation:
   Check that `/data/data/com.termux/files/home/HyperCeilerModded/app/build/outputs/apk/debug/` contains `HyperHand_*_debug.apk`.
