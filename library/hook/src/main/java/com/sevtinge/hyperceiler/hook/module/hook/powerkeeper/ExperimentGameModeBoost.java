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

import de.robv.android.xposed.XposedHelpers;

/**
 * Force the "game mode" performance profile for the currently
 * foreground app.
 *
 * Verified on HyperOS 2 (PowerKeeper.apk decompile):
 *
 *   com.miui.powerkeeper.statemachine.PowerStateMachineProxy
 *       public static boolean isGameModeApp(String pkg)
 *           - public entry point, used by SystemUI / framework.
 *             Return true always = "yes, treat every foreground app
 *             as a game".
 *
 *   com.miui.powerkeeper.statemachine.PowerStateMachine
 *       public PowerState getPrimaryState(String pkg)
 *           - if a caller skips the Proxy and hits the machine
 *             directly, and the app has no state, we synthesise
 *             one with mId = 9 (the "game" id per the private
 *             isGameModeApp's whitelist {9,13,16,106,107}).
 *
 *   com.miui.powerkeeper.thermal.ScenarioManager
 *       isGameModeApp(String)  - newer path used by Proxy when the
 *             ScenarioManager is initialised. Also forced true.
 */
public class ExperimentGameModeBoost extends BaseHook {

    /** PowerState ctor signature from decompile: (int, String). */
    private static final int GAME_STATE_ID = 9;

    @Override
    public void init() {
        // 1. Proxy — the public static isGameModeApp(String).
        Class<?> proxy = findClassIfExists(
                "com.miui.powerkeeper.statemachine.PowerStateMachineProxy");
        if (proxy != null) {
            findAndHookMethodSilently(proxy, "isGameModeApp", String.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        }

        // 2. ScenarioManager — used when the proxy defers.
        Class<?> scenario = findClassIfExists(
                "com.miui.powerkeeper.thermal.ScenarioManager");
        if (scenario != null) {
            findAndHookMethodSilently(scenario, "isGameModeApp", String.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        }

        // 3. PowerStateMachine.getPrimaryState(String) — synthesise a
        //    game-tagged PowerState when it returns null so downstream
        //    callers that read mId treat the app as a game.
        Class<?> machine = findClassIfExists(
                "com.miui.powerkeeper.statemachine.PowerStateMachine");
        if (machine == null) return;

        findAndHookMethodSilently(machine, "getPrimaryState", String.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                if (param.getResult() != null) return;
                try {
                    // PowerState is a non-static inner class of PowerStateMachine,
                    // so reflection cannot construct one without an outer instance.
                    // Instead, reuse the instance already cached under the game
                    // state id — it's built at startup from mAllPowerState.
                    Object sparse = XposedHelpers.getObjectField(param.thisObject, "mAllPowerState");
                    if (sparse == null) return;
                    Object gameState = XposedHelpers.callMethod(sparse, "get", GAME_STATE_ID, null);
                    if (gameState != null) param.setResult(gameState);
                } catch (Throwable ignored) { }
            }
        });
    }
}
