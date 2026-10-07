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
package com.sevtinge.hyperceiler.ui.hooker.various;

import android.content.Intent;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.sevtinge.hyperceiler.ui.R;
import com.sevtinge.hyperceiler.dashboard.DashboardFragment;

/**
 * UI skeleton for the future "AVES+ Tools Provider" bridge.
 *
 * Nothing is wired yet — every actionable preference below shows the
 * "coming soon" string and either does nothing or opens the AVES+
 * tools app if it is installed. The real IPC layer will be added
 * once AVES+ AI publishes its provider contract.
 */
public class AvesToolsProviderSettings extends DashboardFragment {

    @Override
    public int getPreferenceScreenResId() {
        return R.xml.aves_tools_provider;
    }

    @Override
    public void initPrefs() {
        // Every entry currently only toasts "coming soon" / no-ops.
        String[] keys = {
            "prefs_key_aves_bridge_enable",
            "prefs_key_aves_bridge_endpoint",
            "prefs_key_aves_bridge_tools_ocr",
            "prefs_key_aves_bridge_tools_nsfw",
            "prefs_key_aves_bridge_tools_tag",
            "prefs_key_aves_bridge_tools_caption",
            "prefs_key_aves_bridge_tools_search",
            "prefs_key_aves_bridge_tools_translate",
            "prefs_key_aves_bridge_tools_embed",
            "prefs_key_aves_bridge_fallback_native",
        };
        for (String k : keys) {
            Preference p = findPreference(k);
            if (p == null) continue;
            p.setEnabled(false);   // dim until the provider lands
            p.setSummary(R.string.aves_tools_provider_coming_soon);
        }
    }
}
