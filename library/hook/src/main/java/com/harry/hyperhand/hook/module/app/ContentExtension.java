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

import static com.harry.hyperhand.hook.utils.devicesdk.MiDeviceAppUtilsKt.isPad;

import com.hchen.database.HookBase;
import com.harry.hyperhand.hook.module.base.BaseModule;
import com.harry.hyperhand.hook.module.hook.contentextension.DoublePress;
import com.harry.hyperhand.hook.module.hook.contentextension.HorizontalContentExtension;
import com.harry.hyperhand.hook.module.hook.contentextension.LinkOpenMode;
import com.harry.hyperhand.hook.module.hook.contentextension.SuperImage;
import com.harry.hyperhand.hook.module.hook.contentextension.Taplus;
import com.harry.hyperhand.hook.module.hook.contentextension.UnlockTaplus;
import com.harry.hyperhand.hook.module.hook.contentextension.UseThirdPartyBrowser;

@HookBase(targetPackage = "com.miui.contentextension")
public class ContentExtension extends BaseModule {

    @Override
    public void handleLoadPackage() {
        initHook(new UseThirdPartyBrowser(), mPrefsMap.getBoolean("content_extension_browser"));
        initHook(new DoublePress(), mPrefsMap.getBoolean("content_extension_double_press"));
        initHook(new SuperImage(), mPrefsMap.getBoolean("content_extension_super_image"));
        initHook(new Taplus(), mPrefsMap.getBoolean("security_center_taplus"));
        initHook(new LinkOpenMode(), true);
        initHook(HorizontalContentExtension.INSTANCE, mPrefsMap.getBoolean("content_extension_unlock_taplus_horizontal"));
        initHook(UnlockTaplus.INSTANCE, mPrefsMap.getBoolean("content_extension_unlock_taplus") && isPad());
    }
}
