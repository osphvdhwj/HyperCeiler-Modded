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

import android.text.TextUtils;

import com.hchen.database.HookBase;
import com.harry.hyperhand.hook.module.base.BaseModule;
import com.harry.hyperhand.hook.module.hook.screenshot.DeviceShellCustomize;
import com.harry.hyperhand.hook.module.hook.screenshot.SaveToPictures;
import com.harry.hyperhand.hook.module.hook.screenshot.UnlockMinimumCropLimit2;
import com.harry.hyperhand.hook.module.hook.screenshot.UnlockPrivacyMarking;

@HookBase(targetPackage = "com.miui.screenshot")
public class ScreenShot extends BaseModule {

    @Override
    public void handleLoadPackage() {
        initHook(UnlockMinimumCropLimit2.INSTANCE, mPrefsMap.getBoolean("screenshot_unlock_minimum_crop_limit"));
        initHook(SaveToPictures.INSTANCE, mPrefsMap.getBoolean("screenshot_save_to_pictures"));
        initHook(DeviceShellCustomize.INSTANCE, !TextUtils.isEmpty(mPrefsMap.getString("screenshot_device_customize", "")));
        initHook(UnlockPrivacyMarking.INSTANCE, mPrefsMap.getBoolean("screenshot_unlock_privacy_marking"));
    }
}
