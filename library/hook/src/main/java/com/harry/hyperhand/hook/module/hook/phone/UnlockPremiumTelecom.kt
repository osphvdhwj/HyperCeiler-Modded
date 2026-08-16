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
package com.harry.hyperhand.hook.module.hook.phone

import com.harry.hyperhand.hook.module.base.BaseHook
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClass
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook

object UnlockPremiumTelecom : BaseHook() {
    override fun init() {
        runCatching {
            val phoneUtils = loadClass("com.android.phone.MiuiPhoneUtils")
            
            // Dual SIM Dual Active (DSDA)
            phoneUtils.methodFinder()
                .filterByName("isDsdaSupported")
                .firstOrNull()?.createHook { returnConstant(true) }
                
            // Voice over New Radio (VoNR / 5G Calling)
            phoneUtils.methodFinder()
                .filterByName("isVoNRSupported")
                .firstOrNull()?.createHook { returnConstant(true) }
                
            // 5G Gaming Optimization
            phoneUtils.methodFinder()
                .filterByName("isGameFiveGOptimizeSupported")
                .firstOrNull()?.createHook { returnConstant(true) }
                
            // Native Call Transfer
            phoneUtils.methodFinder()
                .filterByName("isCallTransferSupported")
                .firstOrNull()?.createHook { returnConstant(true) }
                
        }.onFailure {
            logE(TAG, this.lpparam.packageName, "UnlockPremiumTelecom initialization failed: $it")
        }
    }
}
