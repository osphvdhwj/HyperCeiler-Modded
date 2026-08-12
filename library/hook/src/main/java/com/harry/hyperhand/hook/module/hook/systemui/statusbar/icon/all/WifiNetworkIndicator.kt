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
package com.harry.hyperhand.hook.module.hook.systemui.statusbar.icon.all

import android.view.View
import com.harry.hyperhand.hook.module.base.BaseHook
import com.harry.hyperhand.hook.module.base.tool.HookTool
import com.harry.hyperhand.hook.utils.StateFlowHelper.newReadonlyStateFlow
import com.harry.hyperhand.hook.utils.devicesdk.isMoreHyperOSVersion
import com.harry.hyperhand.hook.utils.getObjectField
import com.harry.hyperhand.hook.utils.getStaticObjectField
import com.harry.hyperhand.hook.utils.setObjectField
import de.robv.android.xposed.XposedHelpers

object WifiNetworkIndicator : BaseHook() {
    private val mStatusBarWifiViewNew by lazy {
        findClassIfExists("com.android.systemui.statusbar.pipeline.wifi.data.repository.prod.WifiRepositoryImpl")
    }
    private val mStatusBarWifiView by lazy {
        findClassIfExists("com.android.systemui.statusbar.StatusBarWifiView")
    }

    override fun init() {
        if (isMoreHyperOSVersion(2f)) {
            hideWifiActivityNew()
        } else {
            hideWifiActivity()
        }

    }

    private fun hideWifiActivityNew() {
        hookAllConstructors(mStatusBarWifiViewNew, object : MethodHook() {
            override fun after(param: MethodHookParam) {
                param.thisObject.setObjectField(
                    "wifiActivity",
                    newReadonlyStateFlow(
                        mStatusBarWifiViewNew.getStaticObjectField("ACTIVITY_DEFAULT")
                    )
                )
            }
        })
    }

    private fun hideWifiActivity() {
        HookTool.hookAllMethods(
            mStatusBarWifiView,
            "applyWifiState",
            object : HookTool.MethodHook() {
                override fun after(param: MethodHookParam) {
                    val mWifiActivityView =
                        param.thisObject.getObjectField("mWifiActivityView")
                    XposedHelpers.callMethod(mWifiActivityView, "setVisibility", View.GONE)
                }
            })
    }
}
