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
package com.sevtinge.hyperceiler.hook.module.hook.systemframework;

import android.os.Handler;
import android.os.Looper;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory;

public class ChargeLimit extends BaseHook {

    private static final String CHARGE_CONTROL_LIMIT_PATH = "/sys/class/power_supply/battery/charge_control_limit";

    @Override
    public void init() {
        boolean isEnabled = mPrefsMap.getBoolean("prefs_key_security_center_charge_limit_enable");
        if (!isEnabled) {
            return;
        }
        
        int limitValue = mPrefsMap.getInt("prefs_key_security_center_charge_limit_value", 80);

        // We can hook com.android.server.BatteryService.onStart or just run the apply task
        try {
            Class<?> batteryServiceClass = findClassIfExists("com.android.server.BatteryService");
            if (batteryServiceClass != null) {
                findAndHookMethodSilently(batteryServiceClass, "onStart", new MethodHook() {
                    @Override
                    protected void after(MethodHookParam param) throws Throwable {
                        android.content.Context context = (android.content.Context) getObjectField(param.thisObject, "mContext");
                        if (context != null) {
                            android.content.IntentFilter filter = new android.content.IntentFilter("com.sevtinge.hyperceiler.ACTION_UPDATE_CHARGE_LIMIT");
                            context.registerReceiver(new android.content.BroadcastReceiver() {
                                @Override
                                public void onReceive(android.content.Context ctx, android.content.Intent intent) {
                                    mPrefsMap.reload();
                                    if (mPrefsMap.getBoolean("prefs_key_security_center_charge_limit_enable")) {
                                        applyChargeLimit(mPrefsMap.getInt("prefs_key_security_center_charge_limit_value", 80));
                                    } else {
                                        applyChargeLimit(100); // Reset to 100% when disabled
                                    }
                                }
                            }, filter);
                        }

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (mPrefsMap.getBoolean("prefs_key_security_center_charge_limit_enable")) {
                                applyChargeLimit(limitValue);
                            }
                        }, 10000);
                    }
                });
            }
        } catch (Throwable t) {
            logE("ChargeLimit", "Error hooking BatteryService", t);
        }
        
        // Apply immediately as well, in case we are loaded late
        applyChargeLimit(limitValue);
    }

    private void applyChargeLimit(int limit) {
        File controlFile = new File(CHARGE_CONTROL_LIMIT_PATH);
        if (!controlFile.exists()) {
            return;
        }
        
        try (FileOutputStream fos = new FileOutputStream(controlFile)) {
            fos.write(String.valueOf(limit).getBytes());
            fos.flush();
        } catch (IOException e) {
            // Permission denied or other IO error
            logE("ChargeLimit", "Failed to write charge limit normally, trying root... " + e.getMessage());
            com.sevtinge.hyperceiler.hook.utils.shell.ShellUtils.execCommand("echo " + limit + " > " + CHARGE_CONTROL_LIMIT_PATH, true);
        }
    }
}
