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
package com.harry.hyperhand.hook.module.hook.systemui.statusbar.strongtoast

import android.widget.FrameLayout
import com.harry.hyperhand.hook.module.base.BaseHook
import com.harry.hyperhand.hook.utils.api.LazyClass.NewStrongToast
import com.harry.hyperhand.hook.utils.api.LazyClass.StrongToast
import com.harry.hyperhand.hook.utils.devicesdk.isMoreAndroidVersion
import com.harry.hyperhand.hook.utils.devicesdk.isMoreHyperOSVersion
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createAfterHook


object HideStrongToast : BaseHook() {
    override fun init() {
        if (isMoreHyperOSVersion(2f) && isMoreAndroidVersion(35)) {
            NewStrongToast!!.methodFinder()
        } else {
            StrongToast!!.methodFinder()
        }.filterByName("onAttachedToWindow").single().createAfterHook {
            val strongToastLayout = it.thisObject as FrameLayout
            strongToastLayout.viewTreeObserver.addOnPreDrawListener {
                return@addOnPreDrawListener false
            }
        }
    }
}
