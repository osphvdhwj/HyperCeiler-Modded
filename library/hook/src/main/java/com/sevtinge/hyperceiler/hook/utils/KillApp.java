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
        ShellInit.getShell()
                .add("pid=$(pgrep -f \"" + pkg + "\" | grep -v $$)")
                .add("if [[ $pid == \"\" ]]; then")
                .add(" pids=\"\"")
                .add(" pid=$(ps -A -o PID,ARGS=CMD | grep \"" + pkg + "\" | grep -v \"grep\")")
                .add("  for i in $pid; do")
                .add("   if [[ $(echo $i | grep '[0-9]' 2>/dev/null) != \"\" ]]; then")
                .add("    if [[ $pids == \"\" ]]; then")
                .add("      pids=$i")
                .add("    else")
                .add("      pids=\"$pids $i\"")
                .add("    fi")
                .add("   fi")
                .add("  done")
                .add("fi")
                .add("if [[ $pids != \"\" ]]; then")
                .add(" pid=$pids")
                .add("fi")
                .add("if [[ $pid != \"\" ]]; then")
                .add(" for i in $pid; do")
                .add("  kill -s 9 $i &>/dev/null")
                .add(" done")
                .add("else")
                .add(" echo \"No Find Pid!\"")
                .add("fi")
                .over().sync();

        ArrayList<String> outPut = ShellInit.getShell().getOutPut();
        ArrayList<String> error = ShellInit.getShell().getError();
        if (outPut != null && !outPut.isEmpty() && "No Find Pid!".equals(outPut.get(0))) {
            AndroidLogUtils.logW(ITAG.TAG, "Didn't find a pid that can kill: " + pkg);
        }
        if (error != null && !error.isEmpty()) {
            AndroidLogUtils.logE(ITAG.TAG, "An error message was returned: " + error);
        }
    }
}
