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
package com.harry.hyperhand.ui.hooker.home;

import static com.harry.hyperhand.hook.utils.devicesdk.MiDeviceAppUtilsKt.isPad;

import android.content.Intent;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.harry.hyperhand.dashboard.DashboardFragment;
import com.harry.hyperhand.ui.R;
import com.harry.hyperhand.ui.sub.AppPickerFragment;
import com.harry.hyperhand.ui.sub.SubPickerActivity;

import fan.preference.SeekBarPreferenceCompat;

public class HomeRecentSettings extends DashboardFragment {

    Preference mHideRecentCard;
    SeekBarPreferenceCompat mTaskViewHeight;
    SwitchPreference mShowMenInfo;
    SwitchPreference mHideCleanIcon;
    SwitchPreference mNotHideCleanIcon;

    @Override
    public int getPreferenceScreenResId() {
        return R.xml.home_recent;
    }

    @Override
    public void initPrefs() {
        mHideRecentCard = findPreference("prefs_key_home_recent_hide_card");
        mTaskViewHeight = findPreference("prefs_key_home_recent_task_view_height");
        mShowMenInfo = findPreference("prefs_key_home_recent_show_memory_info");
        mHideCleanIcon = findPreference("prefs_key_home_recent_hide_clean_up");
        mNotHideCleanIcon = findPreference("prefs_key_always_show_clean_up");

        if (isPad()) {
            mTaskViewHeight.setVisible(false);
        }

        mHideRecentCard.setOnPreferenceClickListener(
                preference -> {
                    Intent intent = new Intent(getActivity(), SubPickerActivity.class);
                    intent.putExtra("mode", AppPickerFragment.LAUNCHER_MODE);
                    intent.putExtra("key", preference.getKey());
                    startActivity(intent);
                    return true;
                }
        );

        mShowMenInfo.setVisible(isPad());

        mHideCleanIcon.setOnPreferenceChangeListener((preference, o) -> {
            if (!(boolean) o) {
                mNotHideCleanIcon.setChecked(false);
            }
            return true;
        });
    }
}
