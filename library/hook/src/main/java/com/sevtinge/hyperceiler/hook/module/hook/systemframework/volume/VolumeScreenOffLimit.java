/*
 * This file is part of HyperCeiler.
 *
 * HyperCeiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023-2025 HyperCeiler Contributions
 */
package com.sevtinge.hyperceiler.hook.module.hook.systemframework.volume;

import android.content.Context;
import android.media.AudioManager;
import android.os.PowerManager;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import de.robv.android.xposed.XposedHelpers;

/**
 * Clamp the media volume index reached while the screen is off.
 *
 * Consumes two preferences declared in framework_volume.xml but never
 * implemented:
 *   prefs_key_system_framework_volume_limit_screen_off_speaker   (0..15)
 *   prefs_key_system_framework_volume_limit_screen_off_earphones (0..15)
 *
 * Choice between "speaker" and "earphones" is decided at call time by
 * AudioService.isStreamActive / current output device. When in doubt we
 * fall back to the speaker cap (safer).
 *
 * Hook target: AudioService.adjustStreamVolume (physical-key path only;
 * SystemUI's volume panel is filtered out via callingPackage != "android").
 */
public class VolumeScreenOffLimit extends BaseHook {

    @Override
    public void init() {
        final Class<?> audioService = findClassIfExists("com.android.server.audio.AudioService");
        if (audioService == null) return;

        MethodHook h = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                try {
                    if (param.args.length < 2) return;
                    if (!(param.args[0] instanceof Integer)) return;
                    if (!(param.args[1] instanceof Integer)) return;

                    int stream  = (Integer) param.args[0];
                    int dir     = (Integer) param.args[1];

                    if (stream != AudioManager.STREAM_MUSIC) return;
                    if (dir != AudioManager.ADJUST_RAISE) return;

                    // Only clamp the physical-key path.
                    boolean fromKey = false;
                    for (Object a : param.args) {
                        if (a instanceof String) {
                            fromKey = "android".equals((String) a);
                            break;
                        }
                    }
                    if (!fromKey) return;

                    Context ctx = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    if (ctx == null) return;

                    PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
                    if (pm == null || pm.isInteractive()) return;

                    AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
                    if (am == null) return;

                    int max = XposedHelpers.getIntField(param.thisObject, "mStreamVolumeAlias") >= 0
                            ? am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            : am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                    boolean headset = am.isWiredHeadsetOn()
                            || XposedHelpers.getBooleanField(am, "mBluetoothA2dpEnabled");

                    int cap = headset
                            ? mPrefsMap.getInt("system_framework_volume_limit_screen_off_earphones", 15)
                            : mPrefsMap.getInt("system_framework_volume_limit_screen_off_speaker", 15);

                    if (cap <= 0 || cap >= max) return;

                    int cur = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (cur >= cap) {
                        // Already at cap → veto the raise by reporting success without change.
                        param.setResult(null);
                    }
                } catch (Throwable ignored) { }
            }
        };

        try {
            findAndHookMethod(audioService, "adjustStreamVolume",
                    int.class, int.class, int.class, String.class, h);
        } catch (Throwable ignored) { }
    }
}
