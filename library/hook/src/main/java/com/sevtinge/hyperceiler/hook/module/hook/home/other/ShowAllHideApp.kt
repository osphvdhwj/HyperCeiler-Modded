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

/**
 * Show hidden apps in the launcher's app-drawer.
 *
 * The DexKit query used to find the target class — a class that mentions
 * both "appInfo.packageName" and "com.android.fileexplorer" — matches
 * nothing on this port ROM. Grep over the decompiled MiuiHome
 * (/product/priv-app/MiuiHome) confirms neither string pair nor the
 * method name "isHideAppValid" exists anywhere in the APK.
 *
 * The hook therefore cannot work on this ROM; there is no class to hook.
 * Stubbed to a no-op so module init does not raise NoResultException into
 * LSPosed's error log.
 *
 * To restore on a ROM where the class exists, replace the empty init
 * with:
 *
 *   findAndHookMethodSilently(
 *       "<dotted.class.Name>", "isHideAppValid",
 *       object : MethodHook() {
 *           override fun before(param: MethodHookParam) { param.result = true }
 *       }
 *   )
 */
object ShowAllHideApp : BaseHook() {
    override fun init() {
        // Intentionally empty — feature is not present on this ROM.
    }
}
