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
import com.harry.hyperhand.hook.module.hook.phone.DisableRemoveNetworkMode;
import com.harry.hyperhand.hook.module.hook.phone.DualNrSupport;
import com.harry.hyperhand.hook.module.hook.phone.DualSaSupport;
import com.harry.hyperhand.hook.module.hook.phone.ModemFeature;
import com.harry.hyperhand.hook.module.hook.phone.N1BandPhone;
import com.harry.hyperhand.hook.module.hook.phone.N28BandPhone;
import com.harry.hyperhand.hook.module.hook.phone.N5N8BandPhone;
import com.harry.hyperhand.hook.module.hook.phone.UnlockVoiceLink;
import com.harry.hyperhand.hook.module.hook.phone.ViceSlotVolteButton;

@HookBase(targetPackage = "com.android.phone")
public class Phone extends BaseModule {
    @Override
    public void handleLoadPackage() {
        initHook(new UnlockVoiceLink(), mPrefsMap.getBoolean("phone_unlock_voice_link"));
        initHook(ModemFeature.INSTANCE, mPrefsMap.getBoolean("phone_smart_dual_sim"));
        initHook(ViceSlotVolteButton.INSTANCE, mPrefsMap.getBoolean("phone_vice_slot_volte"));
        initHook(new DisableRemoveNetworkMode(), mPrefsMap.getBoolean("phone_disable_remove_network_mode"));

        initHook(DualNrSupport.INSTANCE, mPrefsMap.getBoolean("phone_double_5g_nr"));
        initHook(DualSaSupport.INSTANCE, mPrefsMap.getBoolean("phone_double_5g_sa"));
        initHook(N1BandPhone.INSTANCE, mPrefsMap.getBoolean("phone_n1"));
        initHook(N5N8BandPhone.INSTANCE, mPrefsMap.getBoolean("phone_n5_n8"));
        initHook(N28BandPhone.INSTANCE, mPrefsMap.getBoolean("phone_n28"));
    }
}
