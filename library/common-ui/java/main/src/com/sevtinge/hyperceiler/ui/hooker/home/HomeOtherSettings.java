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
package com.sevtinge.hyperceiler.ui.hooker.home;

import static com.sevtinge.hyperceiler.hook.utils.devicesdk.MiDeviceAppUtilsKt.isPad;

import androidx.preference.SwitchPreference;

import androidx.preference.Preference;
import android.content.Intent;
import com.sevtinge.hyperceiler.ui.sub.AppPickerFragment;
import com.sevtinge.hyperceiler.ui.sub.SubPickerActivity;
import com.sevtinge.hyperceiler.ui.R;
import com.sevtinge.hyperceiler.dashboard.DashboardFragment;

public class HomeOtherSettings extends DashboardFragment {

    SwitchPreference mEnableMoreSettings;
    Preference mMinusOneApp;


    @Override
    public int getPreferenceScreenResId() {
        return R.xml.home_other;
    }

    @Override
    public void initPrefs() {
        mEnableMoreSettings = findPreference("prefs_key_home_other_mi_pad_enable_more_setting");
        mEnableMoreSettings.setVisible(isPad());

        mMinusOneApp = findPreference("prefs_key_home_minus_one_custom_app");
        if (mMinusOneApp != null) {
            mMinusOneApp.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), SubPickerActivity.class);
                intent.putExtra("mode", AppPickerFragment.LAUNCHER_MODE);
                intent.putExtra("key", preference.getKey());
                startActivity(intent);
                return true;
            });
        }
    }

}
