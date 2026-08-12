/*
 * This file is part of HyperHand.
 *
 * HyperHand is free software: you can redistribute it and/or modify
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
 * Copyright (C) 2023-2025 HyperHand Contributions
 */

package com.harry.hyperhand.hook.module.hook.personalassistant;

import com.hchen.hooktool.utils.ResInjectTool;
import com.harry.hyperhand.hook.module.base.BaseHook;

public class SetTravelNotificationStatusBarInfoMaxWidth extends BaseHook {
    @Override
    public void init() throws NoSuchMethodException {
        ResInjectTool.setDensityReplacement("com.miui.personalassistant", "dimen", "pa_travel_notification_statusbar_info_max_width", (float) mPrefsMap.getInt("personal_assistant_set_tv_notif_info_max_width", 60));
    }
}
