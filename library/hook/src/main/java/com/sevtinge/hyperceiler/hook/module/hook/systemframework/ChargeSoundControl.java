/*
 * This file is part of HyperCeiler.

 * HyperCeiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2025 HyperCeiler Contributions
*/
package com.sevtinge.hyperceiler.hook.module.hook.systemframework;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * Hooks com.android.server.power.Notifier (system_server, from services.jar)
 * to suppress the wired/wireless charging-start sound and/or vibration.
 *
 * AOSP calls Notifier#playChargingStartedFeedback(boolean wireless), which
 * internally fans out to playChargingStartedSound and playChargingStartedVibration.
 * We hook the two inner methods independently so the two toggles are
 * orthogonal.
 */
public class ChargeSoundControl extends BaseHook {

    @Override
    public void init() {
        Class<?> notifier = findClassIfExists("com.android.server.power.Notifier");
        if (notifier == null) {
            logE(TAG, lpparam.packageName, "com.android.server.power.Notifier not found");
            return;
        }

        boolean killSound = mPrefsMap.getBoolean("system_ui_disable_charge_sound");
        boolean killVib   = mPrefsMap.getBoolean("system_ui_disable_charge_vibration");

        if (killSound) {
            try {
                findAndHookMethod(notifier, "playChargingStartedSound",
                        boolean.class, new MethodHook() {
                            @Override
                            protected void before(MethodHookParam param) {
                                param.setResult(null);
                            }
                        });
                logI(TAG, lpparam.packageName, "hooked playChargingStartedSound");
            } catch (Throwable t) {
                logE(TAG, lpparam.packageName, "playChargingStartedSound: " + t);
            }
        }

        if (killVib) {
            try {
                findAndHookMethod(notifier, "playChargingStartedVibration",
                        boolean.class, new MethodHook() {
                            @Override
                            protected void before(MethodHookParam param) {
                                param.setResult(null);
                            }
                        });
                logI(TAG, lpparam.packageName, "hooked playChargingStartedVibration");
            } catch (Throwable t) {
                logE(TAG, lpparam.packageName, "playChargingStartedVibration: " + t);
            }
        }
    }
}
