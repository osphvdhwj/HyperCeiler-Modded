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
package com.harry.hyperhand.hook.module.hook.home.drawer

import android.view.View
import com.harry.hyperhand.hook.module.base.BaseHook
import com.harry.hyperhand.hook.utils.callMethodAs
import com.harry.hyperhand.hook.utils.findClass
import com.harry.hyperhand.hook.utils.getObjectFieldAs
import com.harry.hyperhand.hook.utils.hookAfterMethod
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClassOrNull
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook

object AppDrawer : BaseHook() {
    override fun init() {
        if (mPrefsMap.getBoolean("home_drawer_all")) {
            try {
                try {
                    loadClassOrNull("com.miui.home.launcher.allapps.category.BaseAllAppsCategoryListContainer")!!
                        .methodFinder()
                        .filterByName("buildSortCategoryList")
                        .single()
                } catch (_: Exception) {
                    try {
                        loadClassOrNull("com.miui.home.launcher.allapps.category.AllAppsCategoryListContainer")!!
                            .methodFinder()
                            .filterByName("buildSortCategoryList")
                            .single()
                    } catch (_: Exception) {
                        loadClassOrNull("com.miui.home.launcher.AllCategoryList")!!
                            .methodFinder()
                            .filterByName("buildSortCategoryList")
                            .single()
                    }
                }.createHook {
                    after {
                        val list = it.result as ArrayList<*>
                        if (list.size > 1) {
                            list.removeAt(0)
                            it.result = list
                        }
                    }
                }
            } catch (e: Throwable) {
                com.harry.hyperhand.hook.utils.log.XposedLogUtils.logE("AppDrawer", lpparam.packageName, "Failed to hook buildSortCategoryList: " + e)
            }
        }

        if (mPrefsMap.getBoolean("home_drawer_editor")) {
            "com.miui.home.launcher.allapps.AllAppsGridAdapter".hookAfterMethod(
                "onBindViewHolder",
                "com.miui.home.launcher.allapps.AllAppsGridAdapter.ViewHolder".findClass(),
                Int::class.javaPrimitiveType
            ) {
                if (it.args[0].callMethodAs<Int>("getItemViewType") == 64) {
                    it.args[0].getObjectFieldAs<View>("itemView").visibility = View.INVISIBLE
                }
            }
        }

    }
}
