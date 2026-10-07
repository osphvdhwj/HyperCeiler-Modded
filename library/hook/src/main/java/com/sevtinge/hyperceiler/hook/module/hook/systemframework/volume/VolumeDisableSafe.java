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

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * Disable the safe-volume (a.k.a. "hearing protection") cap.
 *
 * mode = 0 : only lift the cap while a headset (wired or BT A2DP) is on
 * mode = 1 : always lift the cap
 *
 * On Android 14+ (HyperOS 2) the SoundDoseHelperStub/StubImpl shim classes
 * were removed — only SoundDoseHelper exists. We try them in order and
 * hook whatever we find rather than blowing up on a missing class.
 */
public class VolumeDisableSafe extends BaseHook {

    private static final int MAX = 0x7FFFFFFE;
    private static volatile boolean isHeadsetOn = false;

    private static final int mode = mPrefsMap.getStringAsInt(
            "system_framework_volume_disable_safe_new", 0);

    @Override
    public void init() {
        Class<?> soundDoseHelper = findClassIfExists("com.android.server.audio.SoundDoseHelper");
        Class<?> stubClass = null;
        for (String name : new String[] {
                "com.android.server.audio.SoundDoseHelperStub",
                "com.android.server.audio.SoundDoseHelperStubImpl" }) {
            stubClass = findClassIfExists(name);
            if (stubClass != null) break;
        }

        MethodHook lift = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                if (mode == 1 || isHeadsetOn) param.setResult(MAX);
            }
        };

        if (stubClass != null) {
            findAndHookMethodSilently(stubClass, "updateSafeMediaVolumeIndex", int.class, lift);
        }
        if (soundDoseHelper != null) {
            findAndHookMethodSilently(soundDoseHelper, "safeMediaVolumeIndex", int.class, lift);
            findAndHookMethodSilently(soundDoseHelper, "updateSafeMediaVolumeIndex", int.class, lift);

            if (mode == 0) {
                hookAllConstructors(soundDoseHelper, new MethodHook() {
                    @Override
                    protected void after(MethodHookParam param) {
                        try {
                            Context ctx = null;
                            for (Object a : param.args) {
                                if (a instanceof Context) { ctx = (Context) a; break; }
                            }
                            if (ctx == null) return;
                            IntentFilter f = new IntentFilter();
                            f.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                            f.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                            f.addAction(AudioManager.ACTION_HEADSET_PLUG);
                            ctx.registerReceiver(new Listener(), f);
                        } catch (Throwable ignored) { }
                    }
                });
            }
        }
    }

    private static class Listener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                case BluetoothDevice.ACTION_ACL_CONNECTED -> isHeadsetOn = true;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED -> isHeadsetOn = false;
                case AudioManager.ACTION_HEADSET_PLUG -> {
                    if (intent.hasExtra("state")) {
                        int state = intent.getIntExtra("state", 0);
                        isHeadsetOn = (state == 1);
                    }
                }
            }
        }
    }
}
