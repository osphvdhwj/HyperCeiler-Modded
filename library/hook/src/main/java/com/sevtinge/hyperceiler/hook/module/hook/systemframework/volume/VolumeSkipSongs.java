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
import android.view.KeyEvent;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import de.robv.android.xposed.XposedHelpers;

/**
 * Screen-off VOLUME_UP / VOLUME_DOWN → MEDIA_NEXT / MEDIA_PREVIOUS.
 *
 * Consumes the "skip_songs" preference declared in framework_volume.xml.
 * Only fires on the physical-key path (callingPackage = "android"),
 * so the volume panel in SystemUI is unaffected.
 */
public class VolumeSkipSongs extends BaseHook {

    private final MethodHook mHook = new MethodHook() {
        @Override
        protected void before(MethodHookParam param) {
            try {
                if (param.args.length < 2) return;
                if (!(param.args[0] instanceof Integer)) return;
                if (!(param.args[1] instanceof Integer)) return;

                int streamType = (Integer) param.args[0];
                int direction  = (Integer) param.args[1];

                if (streamType != AudioManager.STREAM_MUSIC) return;
                if (direction != AudioManager.ADJUST_RAISE
                        && direction != AudioManager.ADJUST_LOWER) return;

                // Filter out the SystemUI volume panel: it passes
                // callingPackage = "com.android.systemui". Only physical
                // key presses from PhoneWindowManager come in as "android".
                for (Object a : param.args) {
                    if (a instanceof String) {
                        String s = (String) a;
                        if (!"android".equals(s)) return;
                        break;
                    }
                }

                Context ctx = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                if (ctx == null) return;

                PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
                if (pm != null && pm.isInteractive()) return;

                AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
                if (am == null || !am.isMusicActive()) return;

                int keyCode = (direction == AudioManager.ADJUST_RAISE)
                        ? KeyEvent.KEYCODE_MEDIA_NEXT
                        : KeyEvent.KEYCODE_MEDIA_PREVIOUS;

                am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,   keyCode));

                param.setResult(null);
            } catch (Throwable t) {
                // Never break AudioService
            }
        }
    };

    @Override
    public void init() {
        Class<?> audioService = findClassIfExists("com.android.server.audio.AudioService");
        if (audioService == null) return;

        boolean hooked = false;
        try {
            findAndHookMethod(audioService, "adjustStreamVolume",
                    int.class, int.class, int.class, String.class, mHook);
            hooked = true;
        } catch (Throwable ignored) { }
        if (!hooked) {
            try {
                findAndHookMethod(audioService, "adjustStreamVolume",
                        int.class, int.class, int.class, String.class, int.class, mHook);
                hooked = true;
            } catch (Throwable ignored) { }
        }
    }
}
