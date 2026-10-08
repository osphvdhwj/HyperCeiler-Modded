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
package com.sevtinge.hyperceiler.common.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.sevtinge.hyperceiler.ui.R;
import com.sevtinge.hyperceiler.hook.module.hook.GlobalActions;

import java.util.Arrays;
import java.util.List;

import fan.appcompat.app.AlertDialog;

/**
 * Restart apps dialog.
 *
 * Every restart goes through a broadcast to the receiver registered by
 * GlobalActions inside system_server (PhoneWindowManager.init). That
 * receiver holds FORCE_STOP_PACKAGES and hands the request to
 * ActivityManagerService, which both kills and respawns persistent
 * processes like SystemUI and MiHome.
 *
 * The previous implementation used KillApp.killApps(), which shells out
 *   pgrep -f "<pkg>" | kill -9
 * from the app process:
 *   - pgrep -f matched ANY process whose cmdline contained the target
 *     string, including system_server when the target was
 *     com.android.systemui — SIGKILLing system_server requires a full
 *     device reboot.
 *   - SIGKILL bypasses ActivityManagerService, so a killed SystemUI or
 *     MiHome was never respawned; users saw a permanent black screen.
 *   - The call ran on the UI thread.
 */
public class RestartAlertDialog extends AlertDialog {

    List<String> mAppNameList;
    List<String> mAppPackageNameList;

    public RestartAlertDialog(Context context) {
        super(context);
        setTitle(R.string.hyperceiler_restart_quick);
        setView(createMultipleChoiceView(context));
    }

    private MultipleChoiceView createMultipleChoiceView(Context context) {
        Resources mRes = context.getResources();
        MultipleChoiceView view = new MultipleChoiceView(context);
        mAppNameList = Arrays.asList(mRes.getStringArray(R.array.restart_apps_name_hyperos));
        mAppPackageNameList = Arrays.asList(mRes.getStringArray(R.array.restart_apps_packagename));
        view.setData(mAppNameList, null);
        view.deselectAll();
        view.setOnCheckedListener(sparseBooleanArray -> {
            dismiss();
            for (int i = 0; i < sparseBooleanArray.size(); i++) {
                if (sparseBooleanArray.get(i)) {
                    restartApp(context, mAppPackageNameList.get(i));
                }
            }
        });
        return view;
    }

    /**
     * Ask system_server to force-stop the package. It will be respawned
     * by ActivityManagerService when persistent.
     */
    public void restartApp(Context context, String packageName) {
        Intent intent = new Intent(GlobalActions.ACTION_PREFIX + "RestartApps");
        intent.putExtra("packageName", packageName);
        context.sendBroadcast(intent);
    }

    /**
     * Kept for source compatibility. Routes through the same receiver.
     */
    public void restartSystemUI(Context context) {
        restartApp(context, "com.android.systemui");
    }
}
