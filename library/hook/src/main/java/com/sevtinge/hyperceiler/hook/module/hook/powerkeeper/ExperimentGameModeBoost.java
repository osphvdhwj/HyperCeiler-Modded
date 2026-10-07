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
 * Force the high-performance profile while a game is in the foreground.
 *
 * Verified on HyperOS 2:
 *   com.miui.powerkeeper.statemachine.PowerStateMachine exists
 *   (referenced by upstream LockMaxFps via DisplayFrameSetting).
 *
 * The remaining class names are candidates; only ones present on the
 * device will be hooked. Methods probed are those that commonly gate
 * the power/perf profile switch. All probes are silent — a missing
 * method just means the feature has no effect on this build.
 *
 * To lock in the real targets: see the APK dump note in
 * ExperimentPowerKeeperNoThrottle.
 */
public class ExperimentGameModeBoost extends BaseHook {

    private static final String[] CLASS_CANDIDATES = {
            "com.miui.powerkeeper.statemachine.PowerStateMachine",
            "com.miui.powerkeeper.gamemode.GameModeManager",
            "com.miui.powerkeeper.gamemode.GameModeHelper",
            "com.miui.powerkeeper.gamebooster.GameBooster",
            "com.miui.powerkeeper.powercenter.PowerCenter",
    };

    private static final String[] BOOL_METHODS = {
            "isPowerSaveModeEnabled",
            "isPowerSaveMode",
            "isBatterySaverOn",
            "shouldRestrictPerformance",
            "shouldThrottle",
    };

    private static final String[] INT_METHODS = {
            "getPerformanceProfile",
            "getCurrentProfile",
            "getPerfProfile",
            "getPowerMode",
            "getMode",
    };

    @Override
    public void init() {
        for (String cls : CLASS_CANDIDATES) {
            Class<?> c = findClassIfExists(cls);
            if (c == null) continue;

            for (String m : BOOL_METHODS) {
                findAndHookMethodSilently(c, m, new MethodHook() {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(false);
                    }
                });
            }

            for (String m : INT_METHODS) {
                findAndHookMethodSilently(c, m, new MethodHook() {
                    @Override
                    protected void before(MethodHookParam param) {
                        // "high" profile — safe even if the enum is 0..5.
                        param.setResult(4);
                    }
                });
            }
        }
    }
}
