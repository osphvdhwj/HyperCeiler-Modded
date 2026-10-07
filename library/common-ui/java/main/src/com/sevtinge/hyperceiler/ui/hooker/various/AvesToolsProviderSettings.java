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

import android.content.Context;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.sevtinge.hyperceiler.ui.R;
import com.sevtinge.hyperceiler.dashboard.DashboardFragment;
import com.sevtinge.hyperceiler.hook.utils.bridge.AvesBridge;

/**
 * External AI Provider (AVES+) settings.
 *
 * Live behavior: on entry, we probe the AVES+ Tools provider. If it
 * is missing, the master switch is disabled and shows the reason.
 * If present, the version is displayed and the master switch becomes
 * interactive. The individual tool switches remain inert until
 * AvesBridge is actually consumed by a hook — that wiring is a
 * separate change.
 */
public class AvesToolsProviderSettings extends DashboardFragment {

    @Override
    public int getPreferenceScreenResId() {
        return R.xml.aves_tools_provider;
    }

    @Override
    public void initPrefs() {
        Context ctx = requireContext();
        SwitchPreference enable =
                findPreference("prefs_key_aves_bridge_enable");

        boolean present = AvesBridge.isAvailable(ctx);
        String version = present ? AvesBridge.version(ctx) : null;

        if (enable != null) {
            enable.setEnabled(present);
            if (!present) {
                enable.setChecked(false);
                enable.setSummary(R.string.aves_tools_provider_not_installed);
            } else {
                enable.setSummary(version != null
                        ? getString(R.string.aves_tools_provider_detected, version)
                        : getString(R.string.aves_tools_provider_detected_unknown));
            }
        }

        // Every other pref stays disabled until the bridge is consumed
        // by a real hook. Grouped so a future commit can flip them all.
        String[] pending = {
                "prefs_key_aves_bridge_endpoint",
                "prefs_key_aves_bridge_fallback_native",
                "prefs_key_aves_bridge_tools_ocr",
                "prefs_key_aves_bridge_tools_nsfw",
                "prefs_key_aves_bridge_tools_tag",
                "prefs_key_aves_bridge_tools_caption",
                "prefs_key_aves_bridge_tools_search",
                "prefs_key_aves_bridge_tools_translate",
                "prefs_key_aves_bridge_tools_embed",
        };
        for (String k : pending) {
            Preference p = findPreference(k);
            if (p == null) continue;
            p.setEnabled(false);
            p.setSummary(R.string.aves_tools_provider_coming_soon);
        }
    }
}
