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

import android.media.AudioManager;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import de.robv.android.xposed.XposedHelpers;

/**
 * Set an absolute number of media volume steps.
 *
 * The previous implementation hooked
 *   SystemProperties.getInt("ro.config.media_vol_steps", ...)
 * which HyperOS 2 never reads — media steps come from
 * AudioService.MAX_STREAM_VOLUME[STREAM_MUSIC] which is populated
 * during AudioService construction. We rewrite that entry before
 * createStreamStates() runs.
 *
 * Coexists with VolumeSteps (percentage multiplier) — if both are on,
 * VolumeMediaSteps wins because it writes an absolute value last
 * (higher priority hooks run after, and we clamp to [15, 100]).
 */
public class VolumeMediaSteps extends BaseHook {

    @Override
    public void init() {
        Class<?> audioService = findClassIfExists("com.android.server.audio.AudioService");
        if (audioService == null) return;

        final int desired = mPrefsMap.getInt("system_framework_volume_media_steps", 15);
        if (desired <= 15) return;

        findAndHookMethod(audioService, "createStreamStates", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                try {
                    int[] max = (int[]) XposedHelpers.getStaticObjectField(
                            audioService, "MAX_STREAM_VOLUME");
                    if (max == null || max.length <= AudioManager.STREAM_MUSIC) return;
                    max[AudioManager.STREAM_MUSIC] = desired;
                    XposedHelpers.setStaticObjectField(
                            audioService, "MAX_STREAM_VOLUME", max);
                } catch (Throwable ignored) { }
            }
        });
    }
}
