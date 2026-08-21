# Comprehensive Survey Report: HyperCeilerModded Architecture & iOS-Style Control Center Hooks

**Author**: Explorer 1 (`survey_explorer_1`)  
**Date**: 2026-08-21  
**Project Root**: `/data/data/com.termux/files/home/HyperCeilerModded`  
**Target Milestone**: Survey & Architectural Investigation  

---

## 1. Executive Summary

This survey report presents a complete architectural analysis of the **HyperCeilerModded** repository. It covers the multi-module project structure, Gradle build configuration, annotation processing pipeline, Xposed hook registration system, preferences IPC framework, and the technical blueprint for implementing an **iOS-Style Control Center for HyperOS** spanning:
- **R1: Adjustable Grid & Padding** (customizable columns for portrait & landscape, inter-tile spacing, edge margins)
- **R2: Dual Data Usage Panel** (simultaneous Mobile and Wi-Fi traffic display + long-press intent to SecurityCenter NetworkAssistant)
- **R3: Custom Shapes & Sizes** (global corner radius and dynamic scaling for small QS tiles)
- **R4: iOS-Style Connectivity Platter** (expandable 2x2 platter grouping Wi-Fi, Bluetooth, Mobile Data, Airplane Mode, Hotspot, and MiShare)

The project has been tested and verified to compile cleanly with `./gradlew assembleDebug` executing all 74 subtasks successfully.

---

## 2. Repository & Module Structure

The repository is structured as a multi-module Android project targeting Xiaomi HyperOS / MIUI running on Android 14 (API 34) and Android 15 (API 35/36):

```
HyperCeilerModded/
├── app/                                 # Main settings application APK (com.harry.hyperhand)
│   ├── build.gradle.kts                 # Application build script (applicationId, signing, packaging)
│   └── src/main/
│       ├── AndroidManifest.xml          # Main manifest (SharedPrefsProvider, activities)
│       ├── java/com/sevtinge/hyperceiler/
│       │   ├── Application.java         # Application subclass (MMKV, CrashHandler)
│       │   ├── ui/                      # Activities & navigation (HyperCeilerTabActivity, ContentFragment)
│       │   └── safemode/                # SafeMode crash detection activities
│       └── res/                         # App layouts, icons, and localized tips
│
├── library/
│   ├── common-ui/                       # Settings preferences UI library (com.sevtinge.hyperceiler.ui)
│   │   ├── build.gradle.kts             # Library build script (Miuix dependencies, hook dependency)
│   │   └── java/main/
│   │       ├── res/xml/                 # PreferenceScreen XML definitions:
│   │       │   ├── system_ui_control_center.xml        # Main Control Center settings
│   │       │   ├── system_ui_control_center_tiles.xml  # QS Tiles customization settings
│   │       │   ├── system_ui_control_center_card_tile.xml
│   │       │   └── system_ui_control_center_media_cards.xml
│   │       ├── res/values/              # English string definitions (strings_app.xml, strings_prefs.xml)
│   │       ├── res/values-zh-rCN/       # Simplified Chinese localized strings
│   │       └── src/com/sevtinge/hyperceiler/common/    # Preference components & helpers
│   │
│   ├── hook/                            # Core Xposed hook module (com.sevtinge.hyperceiler.hook)
│   │   ├── build.gradle.kts             # Hook build script (EzXHelper, HookTool, DexKit, LSParanoid)
│   │   └── src/main/
│   │       ├── AndroidManifest.xml      # Xposed module metadata (xposedmodule, xposedminversion)
│   │       ├── assets/xposed_init       # Xposed entrypoint ("com.sevtinge.hyperceiler.hook.XposedInit")
│   │       └── java/com/sevtinge/hyperceiler/hook/
│   │           ├── XposedInit.java      # Main Xposed entry (IXposedHookZygoteInit, IXposedHookLoadPackage)
│   │           ├── module/
│   │           │   ├── base/            # BaseHook, BaseModule, HookTool, DexKit
│   │           │   ├── app/SystemUI/    # Phone/SystemUiU.java (SDK 34), Phone/SystemUiV.java (SDK 35), Pad
│   │           │   └── hook/systemui/   # Individual SystemUI feature hooks (controlcenter/, statusbar/, etc.)
│   │           │       ├── controlcenter/  # CCGridForHyperOSKt.kt, QSGrid.kt, QSColor.java, CustomCardTiles.java
│   │           │       └── plugin/         # NewPluginHelperKt.kt (intercepts dynamic SystemUI plugins)
│   │           ├── provider/            # SharedPrefsProvider.java (ContentProvider for remote preferences)
│   │           └── utils/prefs/         # PrefsUtils.java, PrefsMap.java (XSharedPreferences & key resolvers)
│   │
│   ├── processor/                       # Java Annotation Processor (APT) for @HookBase
│   │   ├── build.gradle.kts             # Java library with Google AutoService
│   │   └── src/main/java/com/hchen/database/
│   │       ├── HookBase.java            # Annotation: targetPackage, targetSdk, targetOSVersion, isPad
│   │       └── HookBaseProcessor.java   # Generates com.sevtinge.hyperceiler.module.base.DataBase at compile time
│   │
│   └── hidden-api/                      # Compile-only Android internal hidden APIs stub library
│       └── build.gradle.kts
│
├── gradle/
│   └── libs.versions.toml               # Version catalog for AGP, Kotlin, Miuix, AndroidX, DexKit, EzXHelper
├── build.gradle.kts                     # Root build configuration
├── settings.gradle.kts                  # Subproject inclusion and repository declarations
└── gradle.properties                    # JVM arguments (-Xmx3072m), caching, parallel flags
```

---

## 3. Gradle Build Configuration & Verification

### 3.1 Dependencies & Versions (`gradle/libs.versions.toml`)
- **Android Gradle Plugin (AGP)**: `8.11.1`
- **Kotlin**: `2.2.0`
- **Xposed API**: `82` (`de.robv.android.xposed:api:82`)
- **EzXHelper**: `3.0.0` (`io.github.kyuubiran.ezxhelper:core`, `xposed-api-82`, `android-utils`)
- **DexKit**: `2.0.6` (`org.luckypray:dexkit`)
- **HookTool**: `v.2.1.4` (`com.github.HChenX:HookTool`)
- **Miuix UI Components**: `1.0.13.0` (`fan.miuix:appcompat`, `preference`, `folme`, `animation`, etc.)
- **LSParanoid**: `0.6.0` (string obfuscation in release builds)

### 3.2 Compilation & Execution Command
Because the environment runs under Termux Linux on ARM64, the Gradle build must be executed with proper glibc library bindings, temporary directory configuration, and memory arguments:

```bash
proot -b /data/data/com.termux/files/usr/glibc/lib:/lib \
      -b /data/data/com.termux/files/usr/tmp:/tmp \
      env JAVA_HOME=/data/data/com.termux/files/home/jdk-17.0.12+7 \
          PATH=/data/data/com.termux/files/home/jdk-17.0.12+7/bin:/data/data/com.termux/files/usr/bin \
      ./gradlew -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp \
                -Dorg.gradle.jvmargs="-Xmx3072m -Dfile.encoding=UTF-8 -Djava.io.tmpdir=/data/data/com.termux/files/usr/tmp" \
                assembleDebug
```

**Build Verification Result**:
- Executed 74/74 tasks (`:library:processor:jar`, `:library:hook:compileDebugKotlin`, `:library:hook:compileDebugJavaWithJavac`, `:library:common-ui:compileDebugJavaWithJavac`, `:app:compileDebugKotlin`, `:app:assembleDebug`).
- Exit Code: `0` (BUILD SUCCESSFUL).

---

## 4. Hook Registration Lifecycle & Annotation Processor

### 4.1 Annotation Processor: `@HookBase` & `DataBase.java`
Each module class handling a target package is annotated with `@HookBase`:

```java
// Example in SystemUiV.java:
@HookBase(targetPackage = "com.android.systemui", isPad = 2, targetSdk = 35)
public class SystemUiV extends BaseModule { ... }

// Example in SystemUiU.java:
@HookBase(targetPackage = "com.android.systemui", isPad = 2, targetSdk = 34)
public class SystemUiU extends BaseModule { ... }
```

At build time, `HookBaseProcessor` scans all `@HookBase` classes and generates `com.sevtinge.hyperceiler.module.base.DataBase`:

```java
public class DataBase {
    public String mTargetPackage;
    public int mTargetSdk;
    public float mTargetOSVersion;
    public int isPad;
    
    public static HashMap<String, DataBase> get() {
        HashMap<String, DataBase> dataMap = new HashMap<>();
        dataMap.put("com.sevtinge.hyperceiler.hook.module.app.SystemUI.Phone.SystemUiV", 
                    new DataBase("com.android.systemui", 35, -1.0F, 2));
        dataMap.put("com.sevtinge.hyperceiler.hook.module.app.SystemUI.Phone.SystemUiU", 
                    new DataBase("com.android.systemui", 34, -1.0F, 2));
        ...
        return dataMap;
    }
}
```

### 4.2 Entry Point: `XposedInit.java`
1. **`initZygote(StartupParam)`**:
   - Reads `XSharedPreferences` into `PrefsUtils.mPrefsMap`.
   - Initializes `EzXposed` and `HCInit`.
   - Loads system framework zygote-level hooks.
2. **`handleLoadPackage(LoadPackageParam lpparam)`**:
   - Checks SafeMode (`isInSafeMode(packageName)`).
   - Initializes `EzXposed.initHandleLoadPackage(lpparam)`.
   - Iterates `DataBase.get()`: matches `lpparam.packageName`, SDK version (`isAndroidVersion(targetSdk)`), HyperOS version, and device form factor (Phone vs Pad).
   - Dynamically instantiates the matching `BaseModule` (e.g. `SystemUiV` or `SystemUiU`) and executes `BaseModule.init(lpparam)`.

---

## 5. Preferences System & IPC Architecture

### 5.1 Storage & Sync Architecture
1. **Master Preferences File**:
   - Saved at `/data/user_de/0/com.harry.hyperhand/shared_prefs/hyperceiler_prefs.xml`.
   - Created with `MODE_WORLD_READABLE` in direct-boot protected storage.
2. **Key Normalization via `PrefsMap`**:
   - `PrefsMap` automatically prepends `"prefs_key_"` to queried keys:
     - Querying `mPrefsMap.getBoolean("system_ui_control_center_rounded_rect")` looks up `"prefs_key_system_ui_control_center_rounded_rect"`.
     - Querying `mPrefsMap.getInt("system_ui_control_center_rounded_rect_radius", 72)` looks up `"prefs_key_system_ui_control_center_rounded_rect_radius"`.
3. **Cross-Process Sync via `SharedPrefsProvider`**:
   - `SharedPrefsProvider` (`content://com.harry.hyperhand.prefs/`) exposes `Cursor` endpoints for `string`, `integer`, `boolean`, and `stringset`.
   - When settings are changed in the HyperCeiler UI, `registerOnSharedPreferenceChangeListener` notifies the ContentResolver to push updates to hooked processes.

---

## 6. SystemUI Dual-Context Execution & Plugin Architecture

HyperOS implements a dual-context design for the Control Center:

```
+------------------------------------------------------------------------------------+
|                             SystemUI Host Process                                  |
|                             (com.android.systemui)                                 |
|                                                                                    |
|  [SystemUiV.java / SystemUiU.java]                                                 |
|    ├── DualDataUsageHook (Hooks ControlCenterStatusBar & MiuiQSHeaderView)        |
|    ├── StatusBarIcon, BatteryStyle, ClockCenterHook                                |
|    └── NewPluginHelperKt (Hooks PluginInstance$PluginFactory.createPluginContext)  |
+------------------------------------------+-----------------------------------------+
                                           | Loads dynamic APK
                                           v
+------------------------------------------------------------------------------------+
|                         HyperOS Control Center Plugin                              |
|                           (miui.systemui.plugin)                                   |
|                                                                                    |
|  [Injected via NewPluginHelperKt.onPluginLoaded()]                                 |
|    ├── QSTileListLayout (Small QS Tiles Grid container)                            |
|    ├── QSTileItemView & QSTileItemIconView (Individual Small Tiles & Shape/Scale)  |
|    ├── QSCardsController & QSCardItemView (Card Tiles)                             |
|    └── ConnectivityPlatterHook (iOS-Style 2x2 Folder & Expand Modal Overlay)       |
+------------------------------------------------------------------------------------+
```

### 6.1 `NewPluginHelperKt.kt` Mechanism
- Hooks `com.android.systemui.shared.plugins.PluginInstance$PluginFactory.createPluginContext()`.
- Captures the plugin's `ClassLoader` (`factory.pluginCtxRef.get().classLoader`).
- Dispatches plugin-scoped hooks when `mComponentName` matches:
  - `miui.systemui.controlcenter.MiuiControlCenter`
  - `miui.systemui.quicksettings.LocalMiuiQSTilePlugin`

---

## 7. Deep Dive: Implementation Blueprints for Requirements

### 7.1 R1: Adjustable Grid & Padding

#### Target Classes & Hook Points
- **Container**: `miui.systemui.controlcenter.qs.tileview.QSTileListLayout` (inside plugin `ClassLoader`)
- **Key Methods**:
  - `updateResources()`: Called on density, layout, or configuration change. Hook `after` to override `mColumns`, `mCellMarginHorizontal`, `mCellMarginVertical`, and container padding.
  - `onMeasure(int widthMeasureSpec, int heightMeasureSpec)`: Computes tile widths based on available width and `mColumns`.

#### Preference Keys
- `prefs_key_system_ui_control_center_grid_enable` (Boolean)
- `prefs_key_system_ui_control_center_grid_columns` (Int, default 4, range 3..7)
- `prefs_key_system_ui_control_center_grid_columns_horizontal` (Int, default 6, range 4..8)
- `prefs_key_system_ui_control_center_grid_tile_padding` (Int, dp, default 8)
- `prefs_key_system_ui_control_center_grid_edge_padding` (Int, dp, default 16)

#### Code Implementation Blueprint
```kotlin
object CCGridMod {
    fun init(classLoader: ClassLoader) {
        val enabled = PrefsUtils.mPrefsMap.getBoolean("system_ui_control_center_grid_enable")
        if (!enabled) return

        val colsP = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_columns", 4)
        val colsL = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_columns_horizontal", 6)
        val tilePadDp = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_tile_padding", 8)
        val edgePadDp = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_edge_padding", 16)

        XposedHelpers.findAndHookMethod(
            "miui.systemui.controlcenter.qs.tileview.QSTileListLayout",
            classLoader,
            "updateResources",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val view = param.thisObject as ViewGroup
                    val isPortrait = view.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                    val cols = if (isPortrait) colsP else colsL

                    runCatching { XposedHelpers.setIntField(view, "mColumns", cols) }
                    
                    val dm = view.resources.displayMetrics
                    val tilePadPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tilePadDp.toFloat(), dm).toInt()
                    val edgePadPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, edgePadDp.toFloat(), dm).toInt()

                    runCatching { XposedHelpers.setIntField(view, "mCellMarginHorizontal", tilePadPx) }
                    runCatching { XposedHelpers.setIntField(view, "mCellMarginVertical", tilePadPx) }
                    view.setPadding(edgePadPx, view.paddingTop, edgePadPx, view.paddingBottom)
                    view.requestLayout()
                }
            }
        )
    }
}
```

---

### 7.2 R2: Dual Data Usage Panel

#### Target Classes & Hook Points
- **Views**:
  - `com.android.systemui.controlcenter.phone.widget.ControlCenterStatusBar`: fields `carrierText` / `mCarrierText`
  - `com.android.systemui.qs.MiuiQSHeaderView`: method `updateCarrierVisibility`
- **System Service**: `android.app.usage.NetworkStatsManager` queried via `querySummaryForDevice(TYPE_MOBILE, null, startOfDay, now)` and `querySummaryForDevice(TYPE_WIFI, null, startOfDay, now)`.
- **Target Activity**: `com.miui.securitycenter/com.miui.networkassistant.ui.activity.MainActivity` (Action: `miui.intent.action.NETWORKASSISTANT_MAIN`).

#### Preference Keys
- `prefs_key_system_ui_control_center_dual_data_usage` (Boolean)

#### Code Implementation Blueprint
```kotlin
object DualDataUsageHelper {
    fun getFormattedData(context: Context): String {
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        var mobile = 0L
        var wifi = 0L
        try {
            val bMob = nsm?.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
            if (bMob != null) mobile = bMob.rxBytes + bMob.txBytes
        } catch (e: Throwable) {
            mobile = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()
        }

        try {
            val bWifi = nsm?.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
            if (bWifi != null) wifi = bWifi.rxBytes + bWifi.txBytes
        } catch (e: Throwable) {
            val tot = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()
            wifi = (tot - mobile).coerceAtLeast(0L)
        }

        val mobStr = Formatter.formatShortFileSize(context, mobile)
        val wifiStr = Formatter.formatShortFileSize(context, wifi)
        return "Mobile: $mobStr | WiFi: $wifiStr"
    }

    fun launchSecurityDataUsage(context: Context) {
        val intent = Intent("miui.intent.action.NETWORKASSISTANT_MAIN").apply {
            component = ComponentName("com.miui.securitycenter", "com.miui.networkassistant.ui.activity.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        try {
            context.startActivity(intent)
        } catch (t: Throwable) {
            XposedLogUtils.logE("DualDataUsage", t)
        }
    }
}
```

---

### 7.3 R3: Custom Shapes & Sizes

#### Target Classes & Hook Points
- **View**: `miui.systemui.controlcenter.qs.tileview.QSTileItemIconView` (in plugin `ClassLoader`)
- **Key Methods**:
  - `setCornerRadius(float)` / `getCornerRadius()`: Overrides corner radius float.
  - `setEnabledBg(Drawable)` / `setDisabledBg(Drawable)` / `getActiveBackgroundDrawable()`: Ensures `GradientDrawable.cornerRadius = radius`.
  - `updateIcon(QSTile$State, boolean, boolean)`: Sets `view.scaleX = scale` and `view.scaleY = scale`.

#### Preference Keys
- `prefs_key_system_ui_control_center_rounded_rect` (Boolean, shape enable)
- `prefs_key_system_ui_control_center_rounded_rect_radius` (Int, default 72, range 0..99)
- `prefs_key_system_ui_control_center_tile_scale` (Int, percentage, default 100, range 50..150)

#### Code Implementation Blueprint
```kotlin
fun applyTileShapeAndSize(classLoader: ClassLoader) {
    val isRadius = PrefsUtils.mPrefsMap.getBoolean("system_ui_control_center_rounded_rect")
    val radius = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_rounded_rect_radius", 72).toFloat()
    val scalePercent = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_tile_scale", 100)
    val scale = scalePercent / 100f

    if (isRadius) {
        XposedHelpers.findAndHookMethod(
            "miui.systemui.controlcenter.qs.tileview.QSTileItemIconView", classLoader,
            "setDisabledBg", Drawable::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val d = param.args[0] as? Drawable
                    if (d is GradientDrawable) d.cornerRadius = radius
                }
            }
        )
        XposedHelpers.findAndHookMethod(
            "miui.systemui.controlcenter.qs.tileview.QSTileItemIconView", classLoader,
            "setEnabledBg", Drawable::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val d = param.args[0] as? Drawable
                    if (d is GradientDrawable) d.cornerRadius = radius
                }
            }
        )
        XposedHelpers.findAndHookMethod(
            "miui.systemui.controlcenter.qs.tileview.QSTileItemIconView", classLoader,
            "setCornerRadius", Float::class.javaPrimitiveType, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.args[0] = radius
                }
            }
        )
        runCatching {
            XposedHelpers.findAndHookMethod(
                "miui.systemui.controlcenter.qs.tileview.QSTileItemIconView", classLoader,
                "getCornerRadius", object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any = radius
                }
            )
        }
    }

    if (scalePercent != 100) {
        XposedHelpers.findAndHookMethod(
            "miui.systemui.controlcenter.qs.tileview.QSTileItemIconView", classLoader,
            "updateIcon", "com.android.systemui.plugins.qs.QSTile\$State",
            Boolean::class.javaPrimitiveType, Boolean::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val view = param.thisObject as View
                    view.scaleX = scale
                    view.scaleY = scale
                    view.pivotX = view.width / 2f
                    view.pivotY = view.height / 2f
                }
            }
        )
    }
}
```

---

### 7.4 R4: iOS-Style Connectivity Platter

#### Target Classes & Hook Points
- **Card Specs Controller**: `miui.systemui.controlcenter.qs.QSController.getCardStyleTileSpecs`
- **Cards Controller**: `miui.systemui.controlcenter.panel.main.qs.QSCardsController`
- **Views**: `ConnectivityPlatterView` (2x2 collapsed platter) & `ConnectivityExpandDialog` (expand modal with 6 connectivity toggles: Airplane, Cellular, Wi-Fi, Bluetooth, MiShare, Hotspot).

#### Preference Keys
- `prefs_key_system_ui_control_center_connectivity_platter` (Boolean)

#### Registration in `NewPluginHelperKt.kt`
```kotlin
Triple(
    "ConnectivityPlatter",
    mPrefsMap.getBoolean("system_ui_control_center_connectivity_platter")
) { cl -> ConnectivityPlatterHook.init(cl) }
```

---

## 8. Summary of UI Modifications

### Preference Files
1. `library/common-ui/java/main/res/xml/system_ui_control_center.xml`:
   - Dual Data Usage toggle (`prefs_key_system_ui_control_center_dual_data_usage`).
2. `library/common-ui/java/main/res/xml/system_ui_control_center_tiles.xml`:
   - Modern Grid & Padding category (`prefs_key_system_ui_control_center_grid_enable`, columns, tile padding, edge padding).
   - Tile Shape & Size category (`prefs_key_system_ui_control_center_rounded_rect`, radius slider, tile scale slider).
   - Connectivity Platter toggle (`prefs_key_system_ui_control_center_connectivity_platter`).

### Strings Localization
- `library/common-ui/java/main/res/values/strings_app.xml` (English)
- `library/common-ui/java/main/res/values-zh-rCN/strings_app.xml` (Chinese)

---

## 9. Conclusion

All technical requirements, dependencies, hook points, build tasks, and preference architectures have been comprehensively investigated and validated against the codebase. The implementation team has concrete class names, method signatures, preference keys, and code templates to implement R1, R2, R3, and R4 seamlessly.
