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
import android.os.Handler;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefType;
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefsChangeObserver;

import de.robv.android.xposed.XposedHelpers;

/**
 * Force which stream the physical volume keys adjust when the volume
 * panel isn't already visible.
 *
 * The old implementation created a new PrefsChangeObserver every time
 * getActiveStreamType() was called — leaks a ContentObserver per call.
 * Register once from the AudioService constructor instead.
 */
public class VolumeDefaultStream extends BaseHook {

    private volatile boolean mObserved = false;

    @Override
    public void init() {
        Class<?> audioService = findClassIfExists("com.android.server.audio.AudioService");
        if (audioService == null) return;

        hookAllConstructors(audioService, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                try {
                    if (mObserved) return;
                    Context ctx = null;
                    for (Object a : param.args) {
                        if (a instanceof Context) { ctx = (Context) a; break; }
                    }
                    if (ctx == null) return;
                    Handler h = new Handler(ctx.getMainLooper());
                    new PrefsChangeObserver(ctx, h, true, PrefType.String,
                            "prefs_key_system_framework_default_volume_stream", "0");
                    mObserved = true;
                } catch (Throwable ignored) { }
            }
        });

        findAndHookMethod(audioService, "getActiveStreamType", int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                int s = mPrefsMap.getStringAsInt("system_framework_default_volume_stream", 0);
                if (s > 0) param.setResult(s);
            }
        });
    }
}
