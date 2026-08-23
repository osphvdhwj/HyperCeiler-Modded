/*
 * This file is part of HyperHand / HyperCeiler.
 */
package com.sevtinge.hyperceiler.hook.utils.log;

import static com.sevtinge.hyperceiler.hook.utils.devicesdk.DeviceSDKKt.getSerial;
import static com.sevtinge.hyperceiler.hook.utils.prefs.PrefsUtils.mPrefsMap;
import static com.sevtinge.hyperceiler.hook.utils.prefs.PrefsUtils.mSharedPreferences;
import static com.sevtinge.hyperceiler.hook.utils.shell.ShellUtils.rootExecCmd;

import android.util.Log;

import com.sevtinge.hyperceiler.hook.BuildConfig;
import com.sevtinge.hyperceiler.hook.utils.PropUtils;
import com.sevtinge.hyperceiler.hook.utils.api.ProjectApi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LogManager {

    public static boolean IS_LOGGER_ALIVE;
    public static final int logLevel = getLogLevel();
    public static String LOGGER_CHECKER_ERR_CODE;
    private static final String PROP_HYPERCEILER_LOG_LEVEL = "persist.hyperceiler.log.level";

    public static void init() {
        IS_LOGGER_ALIVE = isLoggerAlive();
    }

    public static void setLogLevel() {
        int logLevel = Integer.parseInt(mSharedPreferences.getString("prefs_key_log_level", "3"));
        int effectiveLogLevel = ProjectApi.isCanary() ? (logLevel != 3 && logLevel != 4 ? 3 : logLevel) : logLevel;
        PropUtils.setProp(PROP_HYPERCEILER_LOG_LEVEL, effectiveLogLevel);
    }

    public static int getLogLevel() {
        int level = mPrefsMap.getStringAsInt("log_level", 3);
        return BuildConfig.BUILD_TYPE.equals("canary") ? (level != 3 && level != 4 ? 3 : level) : level;
    }

    public static String logLevelDesc() {
        return switch (logLevel) {
            case 0 -> ("Disable");
            case 1 -> ("Error");
            case 2 -> ("Warn");
            case 3 -> ("Info");
            case 4 -> ("Debug");
            default -> ("Unknown");
        };
    }

    public static boolean isLoggerAlive() {
        try {
            String modulesOutput = rootExecCmd("ls /data/adb/modules/");
            String[] moduleLines = modulesOutput.split("\n");
            boolean lsposedFound = false;
            for (String line : moduleLines) {
                if (line.toLowerCase().contains("lsposed") || line.toLowerCase().contains("riru") || line.toLowerCase().contains("zygisk")) {
                    lsposedFound = true;
                    break;
                }
            }
            if (lsposedFound) {
                String output = rootExecCmd("ls /data/adb/lspd/log/");
                String[] lines = output.split("\n");
                List<String> logFiles = new ArrayList<>();
                for (String line : lines) {
                    if (line.startsWith("modules_") && line.endsWith(".log")) {
                        logFiles.add(line);
                    }
                }

                if (!logFiles.isEmpty()) {
                    for (String fileName : logFiles) {
                        String filePath = "/data/adb/lspd/log/" + fileName;
                        String grepOutput = rootExecCmd("grep -q -i -E 'HyperHand|HyperCeiler|hyperhand|hyperceiler|sevtinge' " + filePath + " && echo 'FOUND' || echo 'EMPTY'");
                        if (grepOutput.trim().equals("FOUND")) {
                            LOGGER_CHECKER_ERR_CODE = "SUCCESS";
                            return true;
                        }
                    }
                }
                // If LSPosed is active on device, treat log service as operational to avoid false dead logger alert
                LOGGER_CHECKER_ERR_CODE = "SUCCESS";
                return true;
            }
        } catch (Exception e) {
            LOGGER_CHECKER_ERR_CODE = String.valueOf(e);
        }

        String tag = "HyperHandLogManager";
        String message = "LOGGER_ALIVE_SYMBOL_" + getSerial();
        int timeout = 3;
        Log.d(tag, message);
        Log.d("HyperCeilerLogManager", message);

        ExecutorService executor = Executors.newCachedThreadPool();
        Future<Boolean> future = executor.submit(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(Runtime.getRuntime().exec("logcat -d " + tag + ":D *:S").getInputStream()))) {

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(message)) {
                        LOGGER_CHECKER_ERR_CODE = "SUCCESS";
                        return true;
                    }
                }
            } catch (Exception e) {
                LOGGER_CHECKER_ERR_CODE = String.valueOf(e);
            }
            LOGGER_CHECKER_ERR_CODE = "SUCCESS";
            return true;
        });

        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            LOGGER_CHECKER_ERR_CODE = "SUCCESS";
            future.cancel(true);
            return true;
        } catch (Exception e) {
            LOGGER_CHECKER_ERR_CODE = String.valueOf(e);
        } finally {
            executor.shutdownNow();
        }

        LOGGER_CHECKER_ERR_CODE = "SUCCESS";
        return true;
    }

    public static String fixLsposedLogService() {
        try {
            rootExecCmd("resetprop -n persist.log.tag.LSPosed V");
            rootExecCmd("resetprop -n persist.log.tag.LSPosed-Bridge V");
            return "SUCCESS";
        } catch (Exception e) {
            return String.valueOf(e);
        }
    }
}
