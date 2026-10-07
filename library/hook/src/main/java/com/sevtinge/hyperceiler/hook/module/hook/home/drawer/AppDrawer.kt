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
package com.sevtinge.hyperceiler.hook.module.hook.home.drawer

import android.view.View
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.callMethodAs
import com.sevtinge.hyperceiler.hook.utils.findClass
import com.sevtinge.hyperceiler.hook.utils.getObjectFieldAs
import com.sevtinge.hyperceiler.hook.utils.hookAfterMethod
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClassOrNull
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook

object AppDrawer : BaseHook() {

    // CategoryInfo.mCategoryId: 0 = "All", -1 = Personal, -2 = Work
    private const val CATEGORY_ALL = 0

    override fun init() {
        if (mPrefsMap.getBoolean("home_drawer_all")) {
            hookRemoveAllTab()
        }
        if (mPrefsMap.getBoolean("home_drawer_editor")) {
            hookHideEditorTile()
        }
    }

    private fun hookRemoveAllTab() {
        val cls = loadClassOrNull(
            "com.miui.home.launcher.allapps.category.AllAppsCategoryListContainer",
            lpparam.classLoader
        ) ?: return

        cls.methodFinder()
            .filterByName("buildSortCategoryList")
            .firstOrNull()
            ?.createHook {
                after {
                    val original = it.result as? List<*> ?: return@after
                    val filtered = original.filterNot { item ->
                        categoryId(item) == CATEGORY_ALL
                    }
                    if (filtered.size != original.size) {
                        @Suppress("UNCHECKED_CAST")
                        it.result = ArrayList<Any?>(filtered as List<Any?>)
                    }
                }
            }
    }

    /**
     * Read CategoryInfo.getCategoryId() reflectively so we don't hard-depend
     * on the class at compile time. Returns Int.MIN_VALUE when the object
     * does not look like a CategoryInfo.
     */
    private fun categoryId(item: Any?): Int {
        if (item == null) return Int.MIN_VALUE
        return try {
            val m = item.javaClass.getMethod("getCategoryId")
            m.isAccessible = true
            m.invoke(item) as? Int ?: Int.MIN_VALUE
        } catch (t: Throwable) {
            Int.MIN_VALUE
        }
    }

    private fun hookHideEditorTile() {
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
