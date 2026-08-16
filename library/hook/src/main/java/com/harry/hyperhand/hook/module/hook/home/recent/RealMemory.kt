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
package com.harry.hyperhand.hook.module.hook.home.recent

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.text.format.Formatter
import android.widget.TextView
import com.harry.hyperhand.hook.module.base.BaseHook
import com.harry.hyperhand.hook.utils.PropUtils.getProp
import com.harry.hyperhand.hook.utils.devicesdk.isPad
import com.harry.hyperhand.hook.utils.getObjectField
import com.harry.hyperhand.hook.utils.log.XposedLogUtils
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClass
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook

object RealMemory : BaseHook() {
    @SuppressLint("DiscouragedApi")
    override fun init() {
        if (!mPrefsMap.getBoolean("home_recent_show_real_memory")) return

        val recentContainerClass = loadClass(
            when (isPad()) {
                false -> "com.miui.home.recents.views.RecentsContainer"
                true -> "com.miui.home.recents.views.RecentsDecorations"
            }
        )

        recentContainerClass.methodFinder()
            .filterByName("refreshMemoryInfo")
            .firstOrNull()?.createHook {
                after {
                    try {
                        val view = it.thisObject as android.view.View
                        val context = view.context

                        val memoryInfo1StringId = context.resources.getIdentifier(
                            "status_bar_recent_memory_info1", "string", "com.miui.home"
                        )
                        val memoryInfo2StringId = context.resources.getIdentifier(
                            "status_bar_recent_memory_info2", "string", "com.miui.home"
                        )

                        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        val memoryInfo = ActivityManager.MemoryInfo()
                        activityManager.getMemoryInfo(memoryInfo)
                        
                        val totalMem = Formatter.formatFileSize(context, memoryInfo.totalMem)
                        val availMem = Formatter.formatFileSize(context, memoryInfo.availMem)
                        
                        val pipeRegex = "[|｜丨│]".toRegex()
                        val text1 = context.getString(memoryInfo1StringId, availMem, totalMem).replace(pipeRegex, "").trim()
                        val text2 = context.getString(memoryInfo2StringId, availMem, totalMem).replace(pipeRegex, "").trim()
                        
                        (view.getObjectField("mTxtMemoryInfo1") as TextView).text = text1
                        (view.getObjectField("mTxtMemoryInfo2") as TextView).text = text2
                        
                        try {
                            val divider = view.getObjectField("mMemoryInfoDivider") as? android.view.View
                            divider?.visibility = android.view.View.GONE
                        } catch (e: Throwable) {
                            // Ignore if divider view doesn't exist
                        }
                    } catch (e: Throwable) {
                        XposedLogUtils.logE(TAG, "RealMemory after hook crashed: " + e.message)
                    }
                }
            }
    }
}
