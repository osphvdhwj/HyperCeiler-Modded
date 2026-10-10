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
package com.sevtinge.hyperceiler.hook.module.hook.camera;

import android.content.res.Resources;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;

public class FixChineseStrings extends BaseHook {

    private static final Map<String, String> MAP = new HashMap<>();

    static {
        // Settings rows
        MAP.put("pref_camera_touch_focus_delay_title", "Extend touch focus timeout");
        MAP.put("pref_video_capture_repeating_title", "Timed photo during video");
        MAP.put("pref_camera_video_sat_enable_title", "Enable smooth video zoom");
        MAP.put("camcorder_tip_super_night_video_1080P_max_video_duration",
                "1080P records up to %d minutes");
        // Download / misc
        MAP.put("download_in_progress_descrip", "Downloading");
        MAP.put("pre_volume_gain_adjust", "Gain adjust");
        // Makeup entries
        MAP.put("makeup_effect_entry_cool_makeup", "Cool");
        MAP.put("makeup_effect_entry_hardline_makeup", "Tough");
        MAP.put("makeup_effect_entry_neutral_makeup", "Neutral");
        MAP.put("makeup_effect_entry_nude_makeup", "Nude");
        MAP.put("makeup_effect_entry_soft_makeup", "Soft");
        MAP.put("makeup_effect_entry_sweet_makeup", "Sweet");
        // Mimoji
        MAP.put("mimoji_download_material_hint",
                "Creating/editing an avatar requires downloading a resource pack.");
        MAP.put("mimoji_download_material_title", "Download required");
        MAP.put("mimoji_downloading", "Downloading");
        // Misc
        MAP.put("music_hint_tt", "Service provided by Ting Music");
        MAP.put("accessibility_video_sky_function_on", "Enable xxxxx function");
        MAP.put("accessibility_video_sky_function_panel_on", "Open xxxxxxx function panel");
    }

    private static Boolean sIsChineseLocale = null;

    private static boolean isChineseLocale() {
        if (sIsChineseLocale == null) {
            try {
                String lang = Resources.getSystem()
                        .getConfiguration().getLocales().get(0).getLanguage();
                sIsChineseLocale = "zh".equals(lang);
            } catch (Throwable t) {
                sIsChineseLocale = false;
            }
        }
        return sIsChineseLocale;
    }

    private static boolean containsCjk(String s) {
        if (s == null) return false;
        for (int i = 0, n = s.length(); i < n; i++) {
            char c = s.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FFF) return true;
        }
        return false;
    }

    @Override
    public void init() {
        hookAllMethods(Resources.class, "getText", new MethodHook() {
            @Override
            protected void after(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (isChineseLocale()) return;

                Object result = param.getResult();
                if (result == null) return;
                if (!containsCjk(result.toString())) return;

                if (param.args == null || param.args.length != 1) return;
                if (!(param.args[0] instanceof Integer)) return;

                Resources res = (Resources) param.thisObject;
                if (res == null) return;

                String entry;
                try {
                    entry = res.getResourceEntryName((Integer) param.args[0]);
                } catch (Throwable t) {
                    return;
                }

                String en = MAP.get(entry);
                if (en != null) param.setResult(en);
            }
        });
    }
}
