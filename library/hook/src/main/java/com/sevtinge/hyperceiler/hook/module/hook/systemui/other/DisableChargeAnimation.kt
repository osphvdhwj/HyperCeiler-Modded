/*
 * This file is part of HyperCeiler.

 * HyperCeiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2025 HyperCeiler Contributions
*/
package com.sevtinge.hyperceiler.hook.module.hook.systemui.other

import com.sevtinge.hyperceiler.hook.module.base.BaseHook

/**
 * Suppress the MIUI fullscreen charging animation by forcing
 * ChargeUtils.isChargeAnimationDisabled() to return true.
 *
 * MiuiChargeController.shouldShowChargeAnim() consults this gate before
 * calling showChargeAnimation(); returning true short-circuits the whole
 * path, both wired and wireless.
 *
 * The "in-game" toggle is honored by checking the foreground app against
 * the list of known game launchers is out of scope for v1; for now both
 * toggles use the same gate (either one being on suppresses the animation).
 */
object DisableChargeAnimation : BaseHook() {
    override fun init() {
        val chargeUtils = findClassIfExists("com.miui.charge.ChargeUtils")
        if (chargeUtils == null) {
            logE(TAG, lpparam.packageName, "ChargeUtils not found")
            return
        }
        findAndHookMethod(chargeUtils, "isChargeAnimationDisabled", object : MethodHook() {
            override fun before(param: MethodHookParam) {
                param.result = true
            }
        })
        logI(TAG, lpparam.packageName, "hooked isChargeAnimationDisabled")
    }
}
