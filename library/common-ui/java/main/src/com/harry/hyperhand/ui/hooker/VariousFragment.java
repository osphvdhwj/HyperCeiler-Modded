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
package com.harry.hyperhand.ui.hooker;

import static android.os.Looper.getMainLooper;
import static com.harry.hyperhand.hook.utils.devicesdk.MiDeviceAppUtilsKt.isPad;
import static com.harry.hyperhand.hook.utils.devicesdk.SystemSDKKt.isMoreHyperOSVersion;

import android.os.Handler;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;

import com.harry.hyperhand.dashboard.DashboardFragment;
import com.harry.hyperhand.hook.utils.KillApp;
import com.harry.hyperhand.hook.utils.ThreadPoolManager;
import com.harry.hyperhand.ui.R;

public class VariousFragment extends DashboardFragment {
    PreferenceCategory mDefault;
    SwitchPreference mClipboard;
    SwitchPreference mClipboardClear;
    Preference mMipad; // 平板相关功能

    Handler handler;

    @Override
    public int getPreferenceScreenResId() {
        return R.xml.various;
    }

    @Override
    public void initPrefs() {
        mDefault = findPreference("prefs_key_various_super_clipboard_key");
        mMipad = findPreference("prefs_key_various_mipad");
        mClipboard = findPreference("prefs_key_sogou_xiaomi_clipboard");
        mClipboardClear = findPreference("prefs_key_add_clipboard_clear");
        Preference mFocusMode = findPreference("prefs_key_focus_mode_apps");
        mMipad.setVisible(isPad());
        mClipboardClear.setVisible(isMoreHyperOSVersion(2f));
        handler = new Handler(getMainLooper());

        if (mFocusMode != null) {
            mFocusMode.setOnPreferenceClickListener(preference -> {
                android.content.Intent intent = new android.content.Intent(getActivity(), com.harry.hyperhand.ui.sub.SubPickerActivity.class);
                intent.putExtra("mode", com.harry.hyperhand.ui.sub.AppPickerFragment.LAUNCHER_MODE);
                intent.putExtra("key", preference.getKey());
                startActivity(intent);
                return true;
            });
        }

        mClipboard.setOnPreferenceChangeListener((preference, o) -> {
            initKill();
            return true;
        });
    }

    private void initKill() {
        ThreadPoolManager.getInstance().submit(() -> {
            handler.post(() ->
                KillApp.killApps("com.sohu.inputmethod.sogou.xiaomi",
                    "com.sohu.inputmethod.sogou"));
        });
    }
}
