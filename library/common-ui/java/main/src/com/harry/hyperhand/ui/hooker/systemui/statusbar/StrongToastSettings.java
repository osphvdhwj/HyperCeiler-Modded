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
package com.harry.hyperhand.ui.hooker.systemui.statusbar;

import static com.harry.hyperhand.hook.utils.devicesdk.SystemSDKKt.isMoreSmallVersion;

import androidx.preference.Preference;

import com.harry.hyperhand.dashboard.DashboardFragment;
import com.harry.hyperhand.hook.utils.api.miuiStringToast.MiuiStringToast;
import com.harry.hyperhand.ui.R;

public class StrongToastSettings extends DashboardFragment {
    Preference mShortToast;
    Preference mLongToast;

    @Override
    public int getPreferenceScreenResId() { return R.xml.system_ui_status_bar_strong_toast; }

    @Override
    public void initPrefs() {
        mShortToast = findPreference("prefs_key_system_ui_status_bar_strong_toast_test_short_text");
        mLongToast = findPreference("prefs_key_system_ui_status_bar_strong_toast_test_long_text");

        if (isMoreSmallVersion(200, 2f)) {
            setFuncHint(mShortToast, 1);
            setFuncHint(mLongToast, 1);
        }

        mShortToast.setOnPreferenceClickListener(preference -> {
            MiuiStringToast.INSTANCE.showStringToast(requireActivity(), getResources().getString(R.string.system_ui_status_bar_strong_toast_test_short_text_0), 1);
            return true;
        });
        mLongToast.setOnPreferenceClickListener(preference -> {
            MiuiStringToast.INSTANCE.showStringToast(requireActivity(), getResources().getString(R.string.system_ui_status_bar_strong_toast_test_long_text_1), 1);
            return true;
        });
    }
}
