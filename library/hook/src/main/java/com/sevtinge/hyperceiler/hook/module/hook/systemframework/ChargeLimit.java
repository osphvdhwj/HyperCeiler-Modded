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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import com.sevtinge.hyperceiler.hook.utils.shell.ShellInit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.robv.android.xposed.XposedHelpers;

/**
 * Stop charging at a user-configurable percentage.
 *
 * Writes /sys/class/power_supply/battery/charge_control_limit. On most
 * devices this node is root-only, so the write goes through su. To keep
 * that su call off any UI thread we run it on a daemon executor — same
 * pattern used by KillApp and DialogHelper after the black-screen fix.
 *
 * Two triggers:
 *   1. On hook init, apply the current pref once.
 *   2. A BroadcastReceiver registered in system_server via a BatteryService
 *      hook, which reapplies whenever the module broadcasts the
 *      ACTION_UPDATE_CHARGE_LIMIT intent (from the settings UI when the
 *      user changes the toggle or slider).
 *
 * Value is clamped to [50, charge_control_limit_max] so we never write
 * something the battery driver would reject.
 */
public class ChargeLimit extends BaseHook {

    private static final String PATH =
            "/sys/class/power_supply/battery/charge_control_limit";
    private static final String PATH_MAX =
            "/sys/class/power_supply/battery/charge_control_limit_max";

    private static final String ACTION =
            "com.sevtinge.hyperceiler.ACTION_UPDATE_CHARGE_LIMIT";

    private static final int DEFAULT_LIMIT = 80;
    private static final int HARD_MIN = 50;

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "HyperHand-ChargeLimit");
                t.setDaemon(true);
                return t;
            });

    @Override
    public void init() {
        if (!mPrefsMap.getBoolean("security_center_charge_limit_enable")) return;

        int limit = mPrefsMap.getInt("security_center_charge_limit_value", DEFAULT_LIMIT);
        applyAsync(limit);

        Class<?> bs = findClassIfExists("com.android.server.BatteryService");
        if (bs == null) return;

        findAndHookMethodSilently(bs, "onStart", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                try {
                    Context ctx = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    if (ctx == null) return;
                    IntentFilter filter = new IntentFilter(ACTION);
                    ctx.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context c, Intent intent) {
                            boolean enable = intent.getBooleanExtra("enable", true);
                            int value = intent.getIntExtra("value", DEFAULT_LIMIT);
                            applyAsync(enable ? value : 100);
                        }
                    }, filter);
                } catch (Throwable ignored) { }
            }
        });
    }

    private void applyAsync(int limit) {
        EXECUTOR.execute(() -> writeLimit(limit));
    }

    private void writeLimit(int limit) {
        try {
            if (!new File(PATH).exists()) {
                logW(TAG, lpparam.packageName,
                        "charge_control_limit not exposed by this kernel; feature skipped");
                return;
            }
            int max = readMax();
            int clamped = Math.max(HARD_MIN, Math.min(limit, max));
            ShellInit.getShell()
                    .run("echo " + clamped + " > " + PATH)
                    .sync();
            logD(TAG, lpparam.packageName,
                    "charge limit set to " + clamped + " (requested " + limit + ", max " + max + ")");
        } catch (Throwable t) {
            logE(TAG, lpparam.packageName, t);
        }
    }

    private int readMax() {
        try (BufferedReader r = new BufferedReader(new FileReader(PATH_MAX))) {
            String s = r.readLine();
            if (s != null) return Integer.parseInt(s.trim());
        } catch (Throwable ignored) { }
        return 100;
    }
}
