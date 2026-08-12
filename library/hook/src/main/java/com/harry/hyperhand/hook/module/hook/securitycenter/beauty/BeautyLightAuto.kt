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
package com.harry.hyperhand.hook.module.hook.securitycenter.beauty

import com.harry.hyperhand.hook.module.base.BaseHook
import com.harry.hyperhand.hook.module.base.dexkit.DexKit
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHooks
import java.lang.reflect.Method

object BeautyLightAuto : BaseHook() {
    private val beautyAuto by lazy<List<Method>> {
        DexKit.findMemberList("superWirelessCharge") {
            it.findMethod {
                matcher {
                    usingEqStrings("taoyao")
                    paramCount = 0
                    returnType = "boolean"
                }
            }
        }
    }

    override fun init() {
        if (mPrefsMap.getBoolean("security_center_beauty_face")) {
            beautyAuto.createHooks {
                returnConstant(true)
            }
        }
    }
}
