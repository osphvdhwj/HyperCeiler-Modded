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
package com.harry.hyperhand.hook.module.app;

import com.hchen.database.HookBase;
import com.harry.hyperhand.hook.module.base.BaseModule;
import com.harry.hyperhand.hook.module.hook.thememanager.AllowDownloadMore;
import com.harry.hyperhand.hook.module.hook.thememanager.AllowThirdTheme;
import com.harry.hyperhand.hook.module.hook.thememanager.DisableThemeAdNew;
import com.harry.hyperhand.hook.module.hook.thememanager.VersionCodeModify;

@HookBase(targetPackage = "com.android.thememanager")
public class ThemeManager extends BaseModule {

    @Override
    public void handleLoadPackage() {
        initHook(new AllowThirdTheme(), mPrefsMap.getBoolean("system_framework_allow_third_theme"));
        initHook(new DisableThemeAdNew(), mPrefsMap.getBoolean("various_theme_disable_ads"));
        initHook(new AllowDownloadMore(), mPrefsMap.getBoolean("theme_manager_allow_download_more"));

        // 修改版本号
        initHook(new VersionCodeModify(), mPrefsMap.getStringAsInt("theme_manager_new_version_code_modify", 0) != 0);
    }

}
