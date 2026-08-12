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
package com.harry.hyperhand.hook.module.hook.home.recent

import com.harry.hyperhand.hook.module.base.BaseHook
import com.harry.hyperhand.hook.module.base.tool.HookTool
import com.harry.hyperhand.hook.utils.callMethod
import com.harry.hyperhand.hook.utils.getObjectField

object HideRecentCard : BaseHook() {
    override fun init() {
        findAndHookMethod(
            "com.android.systemui.shared.recents.system.ActivityManagerWrapper",
            "needRemoveTask",
            "com.android.systemui.shared.recents.model.GroupedRecentTaskInfoCompat",
            object : HookTool.MethodHook() {
                override fun after(param: MethodHookParam) {
                    val pkgName = param.args[0]
                        ?.getObjectField("mMainTaskInfo")
                        ?.getObjectField("realActivity")
                        ?.callMethod("getPackageName")
                    val selectedApps = mPrefsMap.getStringSet("home_recent_hide_card")
                    if (selectedApps.contains(pkgName)) {
                        param.result = true
                    }
                }
            })
    }
}
