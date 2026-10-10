/*
 * This file is part of HyperCeiler.
 *
 * HyperCeiler is free software: you can redistribute it and/or modify
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
 * Copyright (C) 2023-2025 HyperCeiler Contributions
 */
package com.sevtinge.hyperceiler.hook.module.hook.home.other

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.base.dexkit.DexKit
import de.robv.android.xposed.XposedBridge

/**
 * Show hidden apps in the launcher's app-drawer.
 *
 * The original DexKit query matched a class that mentions both
 * "appInfo.packageName" and "com.android.fileexplorer" in the same body.
 * On the user's port ROM that string-pair does not appear together, so
 * the query raises NoResultException and BaseHook logs the entire module
 * init as failed.
 *
 * Wrapped in try/catch: the class lookup returns null on any failure and
 * the hook becomes a no-op rather than an error. Uses BaseHook's
 * findAndHookMethodSilently instead of the ezxhelper methodFinder chain,
 * so a missing method on a future ROM is also silent.
 */
object ShowAllHideApp : BaseHook() {

    override fun init() {
        val cls = try {
            DexKit.findMember("ShowAllHideApp") { bridge ->
                bridge.findClass {
                    matcher {
                        usingStrings("appInfo.packageName", "com.android.fileexplorer")
                    }
                }.singleOrNull()
            }
        } catch (t: Throwable) {
            XposedBridge.log("[HyperHand][ShowAllHideApp] DexKit miss on this ROM: ${t.message}")
            null
        } ?: return

        findAndHookMethodSilently(cls, "isHideAppValid", object : MethodHook() {
            override fun before(param: MethodHookParam) {
                param.result = true
            }
        })
    }
}
