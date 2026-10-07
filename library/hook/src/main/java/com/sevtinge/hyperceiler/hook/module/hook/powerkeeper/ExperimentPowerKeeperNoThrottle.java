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
 * Prevent PowerKeeper from throttling CPU on thermal / power-save events.
 *
 * Verified on HyperOS 2:
 *   com.miui.powerkeeper.feedbackcontrol.ThermalManager exists
 *   (used by DisableGetDisplayCtrlCode).
 *
 * The exact method names that carry the thermal decision are not verified
 * for HyperOS 2. We probe a set of candidates and hook whichever exist,
 * forcing "cool / no throttle" values. Any candidate that isn't present
 * is a silent no-op — this can never crash the process.
 *
 * To lock in the real targets: `pm path com.miui.powerkeeper`, pull the
 * APK, `baksmali` / dexkit and grep ThermalManager for thermal methods,
 * then replace the candidate arrays below with the confirmed names.
 */
public class ExperimentPowerKeeperNoThrottle extends BaseHook {

    @Override
    public void init() {
        Class<?> thermal = findClassIfExists("com.miui.powerkeeper.feedbackcontrol.ThermalManager");
        if (thermal == null) return;

        // Getters that return a thermal level (0 = cool). Setting to 0
        // means "no throttling required".
        String[] intGetters = {
                "getThermalStatus",
                "getThermalLevel",
                "getTemperatureLevel",
                "getThrottleLevel",
                "getDisplayCtrlCode", // already covered elsewhere; keep as belt+braces
        };
        for (String m : intGetters) {
            findAndHookMethodSilently(thermal, m, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0);
                }
            });
        }

        // Setters that receive a thermal level — force it to 0.
        String[] intSetters = {
                "onThermalChanged",
                "setThermalStatus",
                "updateThermalStatus",
                "onTemperatureChanged",
        };
        for (String m : intSetters) {
            findAndHookMethodSilently(thermal, m, int.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.args[0] = 0;
                }
            });
        }

        // Boolean guards.
        String[] boolGetters = {
                "isThermalThrottlingEnabled",
                "isThrottlingEnabled",
                "isInThermalThrottle",
                "isLimited",
        };
        for (String m : boolGetters) {
            findAndHookMethodSilently(thermal, m, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            });
        }
    }
}
