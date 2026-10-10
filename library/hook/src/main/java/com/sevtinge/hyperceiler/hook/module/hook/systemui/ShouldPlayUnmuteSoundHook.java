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
package com.sevtinge.hyperceiler.hook.module.hook.systemui;

import android.view.View;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import de.robv.android.xposed.XposedHelpers;

/**
 * Forces the DND (Quiet mode) tile to always play the unmute sound.
 *
 * The previous version initialized mQuietModeTile and mZenModeController
 * as class fields, which run at object-construction time — before BaseHook
 * has populated lpparam. Accessing lpparam.classLoader then threw an NPE
 * ("LoadPackageParam.classLoader on a null object reference"), crashing
 * the hook init.
 *
 * Moved the lookups into init(), where lpparam is guaranteed set. Also
 * switched to findClassIfExists so a missing class on a future ROM is a
 * silent no-op instead of a crash.
 */
public class ShouldPlayUnmuteSoundHook extends BaseHook {

    @Override
    public void init() {
        Class<?> quietModeTile = findClassIfExists("com.android.systemui.qs.tiles.QuietModeTile", lpparam.classLoader);
        Class<?> zenModeController = findClassIfExists("com.android.systemui.statusbar.policy.ZenModeController", lpparam.classLoader);

        if (quietModeTile == null || zenModeController == null) return;

        findAndHookMethodSilently(quietModeTile, "handleClick", View.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                XposedHelpers.setBooleanField(zenModeController, "isZenModeOn", true);
            }
        });
    }
}
