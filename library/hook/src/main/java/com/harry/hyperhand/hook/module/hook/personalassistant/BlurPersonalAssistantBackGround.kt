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
package com.harry.hyperhand.hook.module.hook.personalassistant

import android.content.res.Configuration
import com.harry.hyperhand.hook.module.base.BaseHook
import com.harry.hyperhand.hook.utils.callMethod
import com.harry.hyperhand.hook.utils.getIntField
import com.harry.hyperhand.hook.utils.hookBeforeAllMethods
import com.harry.hyperhand.hook.utils.hookBeforeMethod
import com.harry.hyperhand.hook.utils.new
import com.harry.hyperhand.hook.utils.replaceMethod
import com.harry.hyperhand.hook.utils.setObjectField
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClass

object BlurPersonalAssistantBackGround : BaseHook() {
    private val deviceAdapter by lazy {
        loadClass("com.miui.personalassistant.device.DeviceAdapter")
    }
    private val foldableDeviceAdapter by lazy {
        loadClass("com.miui.personalassistant.device.FoldableDeviceAdapter")
    }

    override fun init() {
        deviceAdapter.hookBeforeAllMethods("create") {
            it.result = foldableDeviceAdapter.new(it.args[0])
        }
        try {
            foldableDeviceAdapter.hookBeforeMethod("onEnter", Boolean::class.java) {
                it.thisObject.setObjectField("mScreenSize", 3)
            }
        } catch (e: ClassNotFoundException) {
            foldableDeviceAdapter.hookBeforeMethod("onOpened") {
                it.thisObject.setObjectField("mScreenSize", 3)
            }
        }
        foldableDeviceAdapter.hookBeforeMethod("onConfigurationChanged", Configuration::class.java) {
            it.thisObject.setObjectField("mScreenSize", 3)
        }
        foldableDeviceAdapter.replaceMethod("onScroll", Float::class.java) {
            val f = it.args[0] as Float
            val i = (f * 100.0f).toInt()
            val mCurrentBlurRadius: Int = it.thisObject.getIntField("mCurrentBlurRadius")
            if (mCurrentBlurRadius != i) {
                if (mCurrentBlurRadius <= 0 || i >= 0) {
                    it.thisObject.setObjectField("mCurrentBlurRadius", i)
                } else {
                    it.thisObject.setObjectField("mCurrentBlurRadius", 0)
                }
                it.thisObject.callMethod("blurOverlayWindow", mCurrentBlurRadius)
            }
        }
    }
}
