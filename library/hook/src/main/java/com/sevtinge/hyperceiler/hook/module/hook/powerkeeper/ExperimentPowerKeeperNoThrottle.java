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
package com.sevtinge.hyperceiler.hook.module.hook.powerkeeper;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * Prevent PowerKeeper from throttling CPU on thermal events.
 *
 * All targets verified against the HyperOS 2 PowerKeeper.apk
 * (decompiled with jadx, see ~/decomp/pk_dec):
 *
 *   com.miui.powerkeeper.perfengine.PeThermalController
 *       public synchronized void h(int)   - entry point that
 *           eventually writes to the "thermal-perfd-recv-client"
 *           socket, i.e. tells perfd to throttle. We no-op it.
 *
 *   com.miui.powerkeeper.feedbackcontrol.ThermalManager
 *       int getBacKProgressCtrlCode()
 *       int getBenchmarkCode()
 *       int getSKCtrlCode()
 *       int getWGDCtrlCode()
 *           - all four return per-feature throttle codes; 0 = no
 *             restriction. We force 0.
 *
 * getDisplayCtrlCode() is intentionally left to
 * DisableGetDisplayCtrlCode which is the dedicated toggle for it.
 */
public class ExperimentPowerKeeperNoThrottle extends BaseHook {

    @Override
    public void init() {
        // 1. Skip the perfd throttle signal.
        Class<?> peThermal = findClassIfExists("com.miui.powerkeeper.perfengine.PeThermalController");
        if (peThermal != null) {
            findAndHookMethodSilently(peThermal, "h", int.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            });
        }

        // 2. Zero out the per-feature throttle codes on ThermalManager.
        Class<?> thermal = findClassIfExists("com.miui.powerkeeper.feedbackcontrol.ThermalManager");
        if (thermal == null) return;

        String[] zeroGetters = {
                "getBacKProgressCtrlCode",
                "getBenchmarkCode",
                "getSKCtrlCode",
                "getWGDCtrlCode",
        };
        for (String m : zeroGetters) {
            findAndHookMethodSilently(thermal, m, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0);
                }
            });
        }
    }
}
