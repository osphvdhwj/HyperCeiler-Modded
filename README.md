<div align="center">

<img src="imgs/icon.webp" width="160" height="160" alt="HyperHand" />

# HyperHand

**A personal fork of HyperCeiler — HyperOS enhancement module**

Make HyperOS Great Again — with hooks that actually run.

[![CI Build](https://github.com/osphvdhwj/HyperCeiler-Modded/actions/workflows/ci_build.yml/badge.svg)](https://github.com/osphvdhwj/HyperCeiler-Modded/actions/workflows/ci_build.yml)

</div>

---

## What this fork is

HyperHand is a fork of [HyperCeiler](https://github.com/ReChronoRain/HyperCeiler)
for personal use on Xiaomi HyperOS 2 (Android 15). It tracks upstream
closely and adds a focused set of **independently verified** features.

**Verification policy.** Every hook shipped in this fork has been
cross-checked against the target app's decompiled classes on a real
device. Where a hook cannot attach (class/method removed, anchors
gone on a newer build) it is either re-anchored or documented under
[Known limitations](#known-limitations) — not left to fail silently.

## Features

Features are grouped by the app they hook.

### Camera (`com.android.camera`)

| Feature | What it does |
|---------|--------------|
| **English string fix** | Replaces Chinese-only string resources baked into the base `values/strings.xml` of port ROMs with English. Self-guards when device locale is Chinese. |
| **Version dispatch** | `MaxScreenBrightness` / `SuperHighQuality` fall back to direct hooks when DexKit anchors are missing on older camera builds. Verified against camera 5.1.001370. |
| **Super High Quality unlock** | Hooks the capability gate (`CameraCapabilitiesUtil.isSupportHighQualityPreferred`) that the settings UI reads, making the HQ toggle visible where it is artificially gated. |
| **Track-eyes focus unlock** | Forces `isSupportTrackEye` true on `CameraCapabilities` and `CameraCapabilitiesUtil`. |
| **Black Leica** | Color profile unlock — anchors on `ColorSpace.Named.SRGB` presence. |
| **Custom camera color** | Overrides theme color via structural caller match. |
| **Custom watermark** | Watermark customization via annotation match. |
| **Lab options** | Surfaces the engineering preference screen (`camera.lab.options`). |
| **Max screen brightness** | Keeps screen at max brightness while the camera is open. |
| **Unhardcoded gallery** | Rewrites post-capture `REVIEW` intents so they don't force-launch MIUI Gallery. |

### System UI (`com.android.systemui`)

| Feature | What it does |
|---------|--------------|
| **Control-center blur intensity** | Scales background blur radius of every control-center panel (0–200%). |
| **Tile animation speed** | Rescales the Quick Settings tile state-change animation duration (0–200%). |
| **Tile corner radius** | Scales the corner radius of every CC tile (0–200%). |
| **Tiles per row** | Number of tile columns in the CC grid (3–7). |
| **Extract text from screenshot** | Adds an "Extract text" chip to the screenshot preview; OCRs via AVES+ Tools and copies to clipboard. |

### System Framework (`system`)

| Feature | What it does |
|---------|--------------|
| **Volume media steps** | Rewrites `AudioService.MAX_STREAM_VOLUME[STREAM_MUSIC]` before stream states are built. |
| **Volume: screen-off limit** | Clamps media volume while the screen is off; separate caps for speaker and earphones. |
| **Volume: skip songs with volume keys** | Screen-off physical `VOLUME_UP` / `VOLUME_DOWN` dispatch `MEDIA_NEXT` / `MEDIA_PREVIOUS`. |
| **Package redirect** | Rewrites intents from stock apps to any installed app via a user-editable src=tgt map. |

### PowerKeeper (`com.miui.powerkeeper`)

| Feature | What it does |
|---------|--------------|
| **No throttling** | Suppresses the perfd throttle signal and zeroes `ThermalManager` control codes. |
| **Game mode boost** | Forces `PowerStateMachineProxy.isGameModeApp()` true. |

### Security Center (`com.miui.securitycenter`)

| Feature | What it does |
|---------|--------------|
| **Charge limit** | Stops charging at a user-set percentage via the `charge_control_limit` sysfs node. |
| **App behavior records** | Forces `mc.c.k()` true so the "App behavior records" entry stays visible in App info; click routes to `AppPermissionUsageActivity`. |

### Launcher (`com.miui.home`)

| Feature | What it does |
|---------|--------------|
| **Remove "All" tab** | Removes the All-apps tab from the drawer. |

### Internal

| Feature | What it does |
|---------|--------------|
| **Restart safety** | Restart dialog routes through a system_server broadcast (AMS respawn). KillApp / DialogHelper run `su` off the UI thread. |

Anything not listed above is unchanged upstream HyperCeiler functionality.

## Known limitations

HyperHand is developed against a **Redmi Note 12 Pro+ 5G (`redwoodin`)**
running a HyperOS 2.0.2.0 **port ROM** — the ROM is repackaged from a
different device (`yupik` / SM7325). Some features that work on the
upstream device do not work on this hardware. These are documented,
not worked around with fake toggles.

| Area | Limitation |
|------|------------|
| **4K60 video** | The device's video encoder caps at 4K30 (`media_profiles_yupik_v1.xml`, `maxFrameRate=30`). The `Unlock4k60` hook is a no-op on this hardware — no toggle is surfaced. |
| **Leica mode** | The anchor strings used by upstream (`themeCustomize`, `updateViewCV`, `watermark_westcoast3_evil_queen`) are absent from camera 5.1.001370. Currently a no-op. |
| **Dual camera / parallel processing** | Lab-option toggles are visible after `EnableLabOptions` is enabled, but the underlying feature depends on SoC support the Dimensity 1080 does not provide. The pref writes; the pipeline cannot fulfill it. |
| **Super High Quality side effect** | Forcing the capability gate may hide `MiviNightSe` in camera modes 163 / 171 (the gate is negated in one call site). Under observation; mitigation deferred until impact is measured on-device. |

## Requirements

- Xiaomi device on **HyperOS 2** (Android 15)
- Root with **LSPosed** (or a compatible Xposed framework)
- Module scope must include the target apps — see [Xposed scope](#xposed-scope)

**Not supported:** heavily modified third-party HyperOS ROMs, modified
system apps, and some international HyperOS builds.

## Setup

1. Install the APK.
2. Enable **HyperCeiler** in LSPosed and select the scope apps you want it to hook.
3. Open the app, enable the individual features you want.
4. Reboot, or restart the affected apps.

The module's Xposed scope is identical to HyperCeiler's — the same
selection LSPosed recommends by default.

## Build

CI runs on every push to `main` and uploads the debug APK as a build
artifact.

Local build (JDK 21 + Android SDK required):

```bash
./gradlew assembleDebug
```

Output: app/build/outputs/apk/debug/.

The build is arm64-v8a only.

Versioning

· versionName is pinned at 2.6.161 and bumped manually on release-worthy changes.
· versionCode auto-increments with the commit count, so every CI build is installable over the previous one.

Compatibility matrix

ROM Status
HyperOS 2.x (Android 15) Primary target
HyperOS 1.x (Android 14) Archived at 2.6.160 — no further fixes
MIUI 13–14 (Android 11–13) Archived; use the historical releases upstream
HyperOS 3.x (Android 16) Not yet — awaiting device availability

Xposed scope

<details>
<summary>Tap to expand the full scope list</summary>

App Package
System Framework system
System UI com.android.systemui
System Launcher com.miui.home
Updater com.android.updater
Joyose com.xiaomi.joyose
Xiaomi Settings com.xiaomi.misettings
Security Center com.miui.securitycenter
Notes com.miui.notes
Wallpapers com.miui.miwallpaper
AI Portal / Content Extension com.miui.contentextension
Danmaku Notifications com.xiaomi.barrage
In-call UI com.android.incallui
Telephony com.android.phone
PowerKeeper com.miui.powerkeeper
MMS com.android.mms
Screenshot com.miui.screenshot
Calendar com.android.calendar
Browser com.android.browser
MTB com.xiaomi.mtb
Screen Recorder com.miui.screenrecorder
Permission Manager com.lbe.security.miui
Settings com.android.settings
Sogou IME (Xiaomi) com.sohu.inputmethod.sogou.xiaomi
Weather com.miui.weather2
Milink / Cast com.milink.service
External Storage com.android.externalstorage
AOD Editor com.miui.aod
File Explorer com.android.fileexplorer
System Security Component com.miui.securityadd
Download Manager UI com.android.providers.downloads.ui
Download Manager com.android.providers.downloads
Gallery com.miui.gallery
Xiaomi Creation com.miui.creation
MiShare com.miui.mishare.connectivity
Media Editor com.miui.mediaeditor
Mi Cloud com.miui.cloudservice
Smart Card com.miui.tsmclient
iFlytek IME (Xiaomi) com.iflytek.inputmethod.miui
Package Installer com.miui.packageinstaller
App Store com.xiaomi.market
Personal Assistant com.miui.personalassistant
Theme Manager com.android.thememanager
Guard Provider com.miui.guardprovider
Camera com.android.camera
XiaoAI Translate com.xiaomi.aiasst.vision
XiaoAI Vision com.xiaomi.scanner
XiaoAI com.miui.voiceassist
NFC Service com.android.nfc
Sound Effects com.miui.misound
Backup com.miui.backup
Mi Mover com.miui.huanji
MiTrustService com.xiaomi.trustservice
HTML Viewer com.android.htmlviewer
Telecom com.android.server.telecom
Universal Remote com.duokan.phone.remotecontroller
Analytics com.miui.analytics
Xiaomi Community com.xiaomi.vipaccount
Voice Trigger com.miui.voicetrigger
Sound Recorder com.android.soundrecorder
LPA com.miui.euicc
SIM Activation com.xiaomi.simactivate.service
SystemUI Plugin miui.systemui.plugin

</details>

Upstream and license

HyperCeiler is licensed AGPL-3.0 — this fork inherits and preserves
that license. See LICENSE.

Bug reports about upstream functionality should go to
ReChronoRain/HyperCeiler
first. Fork-specific issues go here.

Credits

This project would not exist without:

· HyperCeiler team — the original project
· Sevtinge — HyperCeiler creator and core maintainer
· All HyperCeiler contributors

HyperCeiler itself reuses code from many open-source projects. The full
upstream credit list is preserved in the upstream repository's README.