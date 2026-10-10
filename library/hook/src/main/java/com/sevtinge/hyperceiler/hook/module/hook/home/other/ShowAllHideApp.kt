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
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook

/**
 * Show hidden apps in the launcher's app-drawer.
 *
 * The original DexKit query matched a class that mentions both
 * "appInfo.packageName" and "com.android.fileexplorer" in the same body.
 * On the user's port ROM that string-pair does not appear together, so
 * the query raises NoResultException and BaseHook logs the entire module
 * init as failed.
 *
 * Wrapped in try/catch: the class lookup returns null on any failure
 * (NoResultException, singleOrNull mismatch, decompile issue) and the
 * hook becomes a no-op rather than an error.
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

        cls.methodFinder()
            .filterByName("isHideAppValid")
            .firstOrNull()
            ?.createHook {
                returnConstant(true)
            }
    }
}
