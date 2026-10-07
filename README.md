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
closely and adds a small set of **independently verified** features.

Every feature toggle in the settings UI is backed by a hook that has
been cross-checked against the target app's decompiled classes on a real
device. If a class, method, or preference cannot be confirmed, the
toggle is not shipped. There is no dead UI here.

## Fork-specific features

These are the additions on top of upstream HyperCeiler:

| Feature | Settings path | What it does |
|---------|---------------|--------------|
| **Package redirect** | Various → Package Redirect | Rewrites intents from stock apps (Notes, Music, Files, Browser, Gallery) to any installed app. One `source.pkg=target.pkg` rule per line, applied live. |
| **Extract text from screenshot** | System UI → Other | Adds an *Extract text* chip to the screenshot preview toolbar. Runs OCR on the shot via the optional AVES+ Tools provider and copies the result to the clipboard. |
| **External AI Provider (AVES+)** | Various → External AI Provider | Bridge to the AVES+ Tools app for on-device OCR, tagging, translation, and image embeddings. UI shows the live provider status; per-tool switches come online as the provider exposes them. |
| **PowerKeeper: no throttling** | PowerKeeper → Experiment | Suppresses the perfd throttle signal and zeroes `ThermalManager` control codes. Verified against HyperOS 2's `PowerKeeper.apk`. |
| **PowerKeeper: game mode boost** | PowerKeeper → Experiment | Forces `PowerStateMachineProxy.isGameModeApp()` true and returns the cached game `PowerState` for unknown packages. |
| **Volume media steps** | System Framework → Volume | Rewrites `AudioService.MAX_STREAM_VOLUME[STREAM_MUSIC]` before stream states are built. Replaces an upstream version that hooked a property HyperOS 2 never reads. |
| **Volume: screen-off limit** | System Framework → Volume | Clamps media volume while the screen is off; separate caps for speaker and earphones. |
| **Volume: skip songs with volume keys** | System Framework → Volume | Screen-off physical VOLUME_UP/DOWN dispatch MEDIA_NEXT/PREVIOUS instead of adjusting volume. |

Anything not listed above is unchanged upstream HyperCeiler functionality.

## Requirements

- Xiaomi device on **HyperOS 2** (Android 15)
- Root with **LSPosed** (or a compatible Xposed framework)
- Module scope must include the target apps — see the scope list below

**Not supported:** heavily modified third-party HyperOS ROMs, modified
system apps, and some international HyperOS builds.

## Setup

1. Install the APK.
2. Enable **HyperCeiler** in LSPosed and select the scope apps you want
   it to hook.
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

The build is arm64-v8a only. versionCode is 5 + git commit count.

Versioning

· versionName is pinned at 2.6.161 and bumped manually on release-worthy changes.
· versionCode auto-increments with the commit count, so every CI build is installable over the previous one.

Compatibility matrix

ROM Status
HyperOS 2.x (Android 15) ✅ primary target
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

HyperCeiler is licensed AGPL-3.0 — this fork inherits and preserves that license.
See LICENSE.

Bug reports about upstream functionality should go to
ReChronoRain/HyperCeiler first.
Fork-specific issues go here.

Credits

This project would not exist without:

· HyperCeiler team — the original project
· Sevtinge — HyperCeiler creator and core maintainer
· All HyperCeiler contributors

HyperCeiler itself reuses code from many open-source projects. The full
upstream credit list is preserved in the upstream repository's README.
