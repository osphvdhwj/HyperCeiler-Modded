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
package com.sevtinge.hyperceiler.hook.utils;

import com.sevtinge.hyperceiler.hook.callback.ITAG;
import com.sevtinge.hyperceiler.hook.utils.log.AndroidLogUtils;
import com.sevtinge.hyperceiler.hook.utils.shell.ShellInit;
import com.sevtinge.hyperceiler.hook.utils.shell.ShellUtils;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fire-and-forget process killer.
 *
 * Every kill goes through a single-threaded executor so the caller's
 * thread — which in every current call site is the UI thread of the
 * HyperHand settings app — never blocks waiting on the su grant or on
 * the shell script to finish. The public API is unchanged: callers still
 * invoke killApps("pkg") and get a boolean back. That boolean now means
 * "the request was queued", not "the kill completed"; nobody was using
 * the completion signal anyway.
 *
 * Background: the previous implementation called
 *   ShellInit.getShell().add(...).over().sync()
 * directly. On a rooted HyperOS 2 device with KernelSU, the first su
 * invocation opens a permission prompt; the calling thread waits on that
 * grant. When that happens on the settings UI thread the app renders
 * nothing — the user sees a permanent black screen and force-stopping
 * the app does not help because the su child is still alive.
 */
public class KillApp {

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "HyperHand-KillApp");
                t.setDaemon(true);
                return t;
            });

    public static boolean killApps(String pkg) {
        return killApps(new String[]{pkg});
    }

    public static boolean killApps(String... pkgs) {
        if (pkgs == null) {
            AndroidLogUtils.logE(ITAG.TAG, "The list of package names cannot be null!");
            return false;
        }
        if (pkgs.length == 0) {
            AndroidLogUtils.logE(ITAG.TAG, "The length of the packet name array cannot be 0!");
            return false;
        }
        // Copy so the caller can mutate its own array without affecting us.
        final String[] copy = pkgs.clone();
        try {
            EXECUTOR.execute(() -> killNow(copy));
            return true;
        } catch (Throwable t) {
            AndroidLogUtils.logE(ITAG.TAG, "Failed to queue kill request: " + t);
            return false;
        }
    }

    private static void killNow(String[] pkgs) {
        for (String pkg : pkgs) {
            if (pkg == null) continue;
            if (pkg.isEmpty()) continue;
            try {
                killOne(pkg);
            } catch (Throwable t) {
                AndroidLogUtils.logE(ITAG.TAG, "kill " + pkg + " failed: " + t);
            }
        }
    }

    private static void killOne(String pkg) {
        try {
            // Use Runtime directly to bypass any ShellUtils hangs
            Runtime.getRuntime().exec(new String[]{"su", "-c", "am force-stop " + pkg}).waitFor();
            AndroidLogUtils.logI(ITAG.TAG, "Force-stopped: " + pkg);
        } catch (Throwable t) {
            AndroidLogUtils.logE(ITAG.TAG, "Failed to force-stop " + pkg + ": " + t);
        }
    }
}
