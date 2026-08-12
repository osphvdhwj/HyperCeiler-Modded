/*
 * This file is part of HyperHand.
 *
 * HyperHand is free software: you can redistribute it and/or modify
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
 * Copyright (C) 2023-2025 HyperHand Contributions
 */
package com.harry.hyperhand.ui.hooker.framework;

import com.harry.hyperhand.ui.R;
import com.harry.hyperhand.dashboard.DashboardFragment;
import com.harry.hyperhand.hook.utils.log.AndroidLogUtils;
import com.harry.hyperhand.hook.utils.shell.ShellUtils;

import fan.preference.DropDownPreference;

public class VolumeSettings extends DashboardFragment {

    DropDownPreference mDefaultVolumeStream;

    @Override
    public int getPreferenceScreenResId() {
        return R.xml.framework_volume;
    }

    @Override
    public void initPrefs() {
        mDefaultVolumeStream = findPreference("prefs_key_system_framework_default_volume_stream");

        assert mDefaultVolumeStream != null;
        mDefaultVolumeStream.setOnPreferenceChangeListener((preference, o) -> {
            try {
                String command = "settings put secure system_framework_default_volume_stream " + Integer.parseInt((String) o);
                ShellUtils.execCommand(command, true);
            } catch (Throwable e) {
                AndroidLogUtils.logE("VolumeSettings", "Throwable: " + e.getMessage());
            }
            return true;
        });
    }
}
