/*
 * This file is part of HyperHand.

 * HyperHand is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2025 HyperHand Contributions
 */
package com.harry.hyperhand.hook.utils.log;

import static com.harry.hyperhand.hook.utils.log.LogManager.logLevel;

import de.robv.android.xposed.XposedBridge;

public class XposedLogUtils {
    private static void writeToFile(String msg) {
        try {
            java.io.File file = new java.io.File("/storage/emulated/0/Download/HyperHand_log.txt");
            java.io.FileWriter fw = new java.io.FileWriter(file, true);
            fw.write(msg + "\n");
            fw.close();
        } catch (Exception e) {
            // ignore
        }
    }

    public static void logI(String msg) {
        if (logLevel < 3) return;
        String fullMsg = "[HyperHand][I]: " + msg;
        XposedBridge.log(fullMsg);
        writeToFile(fullMsg);
    }

    public static void logI(String tagOpkg, String msg) {
        if (logLevel < 3) return;
        XposedBridge.log("[HyperHand][I][" + tagOpkg + "]: " + msg);
    }

    public static void logI(String tag, String pkg, String msg) {
        if (logLevel < 3) return;
        XposedBridge.log("[HyperHand][I][" + pkg + "][" + tag + "]: " + msg);
    }

    public static void logW(String msg) {
        if (logLevel < 2) return;
        XposedBridge.log("[HyperHand][W]: " + msg);
    }

    public static void logW(String tag, String pkg, String msg) {
        if (logLevel < 2) return;
        XposedBridge.log("[HyperHand][W][" + pkg + "][" + tag + "]: " + msg);
    }

    public static void logW(String tag, String pkg, Throwable log) {
        if (logLevel < 2) return;
        XposedBridge.log("[HyperHand][W][" + pkg + "][" + tag + "]: " + log);
    }

    public static void logW(String tag, String pkg, String msg, Exception exp) {
        if (logLevel < 2) return;
        XposedBridge.log("[HyperHand][W][" + pkg + "][" + tag + "]: " + msg + ", by: " + exp);
    }

    public static void logW(String tag, String pkg, String msg, Throwable log) {
        if (logLevel < 2) return;
        XposedBridge.log("[HyperHand][W][" + pkg + "][" + tag + "]: " + msg + ", by: " + log);
    }

    public static void logW(String tag, String msg) {
        if (logLevel < 2) return;
        XposedBridge.log("[HyperHand][W][" + tag + "]: " + msg);
    }

    public static void logW(String tag, Throwable log) {
        if (logLevel < 2) return;
        XposedBridge.log("[HyperHand][W][" + tag + "]: " + log);
    }

    public static void logW(String tag, String msg, Exception exp) {
        if (logLevel < 2) return;
        XposedBridge.log("[HyperHand][W][" + tag + "]: " + msg + ", by: " + exp);
    }

    public static void logE(String tag, String msg) {
        if (logLevel < 1) return;
        String fullMsg = "[HyperHand][E][" + tag + "]: " + msg;
        XposedBridge.log(fullMsg);
        writeToFile(fullMsg);
    }

    public static void logE(String msg) {
        if (logLevel < 1) return;
        String fullMsg = "[HyperHand][E]: " + msg;
        XposedBridge.log(fullMsg);
        writeToFile(fullMsg);
    }

    public static void logE(String tag, Throwable log) {
        if (logLevel < 1) return;
        String fullMsg = "[HyperHand][E][" + tag + "]: " + log;
        XposedBridge.log(fullMsg);
        writeToFile(fullMsg);
    }

    public static void logE(String tag, String pkg, String msg) {
        if (logLevel < 1) return;
        String fullMsg = "[HyperHand][E][" + pkg + "][" + tag + "]: " + msg;
        XposedBridge.log(fullMsg);
        writeToFile(fullMsg);
    }

    public static void logE(String tag, String pkg, Throwable log) {
        if (logLevel < 1) return;
        XposedBridge.log("[HyperHand][E][" + pkg + "][" + tag + "]: " + log);
    }

    public static void logE(String tag, String pkg, Exception exp) {
        if (logLevel < 1) return;
        XposedBridge.log("[HyperHand][E][" + pkg + "][" + tag + "]: " + exp);
    }

    public static void logE(String tag, String pkg, String msg, Throwable log) {
        if (logLevel < 1) return;
        XposedBridge.log("[HyperHand][E][" + pkg + "][" + tag + "]: " + msg + ", by: " + log);
    }

    public static void logE(String tag, String pkg, String msg, Exception exp) {
        if (logLevel < 1) return;
        XposedBridge.log("[HyperHand][E][" + pkg + "][" + tag + "]: " + msg + ", by: " + exp);
    }

    public static void logD(String msg) {
        if (logLevel < 4) return;
        XposedBridge.log("[HyperHand][D]: " + msg);
    }

    public static void logD(String tag, String pkg, String msg) {
        if (logLevel < 4) return;
        XposedBridge.log("[HyperHand][D][" + pkg + "][" + tag + "]: " + msg);
    }

    public static void logD(String tag, String msg) {
        if (logLevel < 4) return;
        XposedBridge.log("[HyperHand][D][" + tag + "]: " + msg);
    }

}
