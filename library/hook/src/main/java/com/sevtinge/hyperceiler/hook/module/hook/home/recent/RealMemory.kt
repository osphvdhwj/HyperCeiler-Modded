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
package com.sevtinge.hyperceiler.hook.module.hook.home.recent

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.text.format.Formatter
import android.widget.TextView
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.PropUtils.getProp
import com.sevtinge.hyperceiler.hook.utils.devicesdk.isPad
import com.sevtinge.hyperceiler.hook.utils.getObjectField
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils
import io.github.kyuubiran.ezxhelper.core.finder.ConstructorFinder.`-Static`.constructorFinder
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClass
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook
import java.text.DecimalFormat
import java.util.Locale

/**
 * Show real memory in the recents screen header.
 *
 * The old version delegated the display text to MIUI's own string
 * resources (status_bar_recent_memory_info1 / _info2). On HyperOS 2
 * those strings are shaped differently — one of them ends with a " | "
 * separator that expects another piece of text appended. Passing two
 * format arguments when the string only consumes one leaves a dangling
 * pipe at the end of the display ("4.2 GB | ").
 *
 * We now build the display string ourselves: "<available> | <total>".
 * No dependency on Xiaomi resources, no NPE if they get renamed.
 */
object RealMemory : BaseHook() {

    @SuppressLint("DiscouragedApi")
    override fun init() {
        lateinit var context: Context

        fun Any.formatSize(): String = Formatter.formatFileSize(context, this as Long)

        val recentContainerClass = loadClass(
            when (isPad()) {
                false -> "com.miui.home.recents.views.RecentsContainer"
                true -> "com.miui.home.recents.views.RecentsDecorations"
            }
        )

        recentContainerClass.declaredConstructors.constructorFinder()
            .filterByParamCount(2)
            .first().createHook {
                after {
                    context = it.args[0] as Context
                }
            }

        recentContainerClass.methodFinder()
            .filterByName("refreshMemoryInfo")
            .first().createHook {
                before {
                    it.result = null
                    val memoryInfo = ActivityManager.MemoryInfo()
                    val activityManager =
                        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    activityManager.getMemoryInfo(memoryInfo)

                    var totalMem = "\\d+\\.\\d+".toRegex()
                        .find(memoryInfo.totalMem.formatSize())?.value ?: "?"
                    val extmSize = getProp("persist.miui.extm.bdsize")
                    var extmMem = ""
                    if (!getProp("persist.miui.extm.enable").equals("0")) {
                        try {
                            val number = extmSize.toDouble() / 1024
                            val df = DecimalFormat("0.00")
                            extmMem = "+" + df.format(number)
                        } catch (e: NumberFormatException) {
                            XposedLogUtils.logE(
                                TAG, lpparam.packageName,
                                "Get extm size failed by: $e"
                            )
                        }
                    }
                    totalMem = "$totalMem$extmMem GB"
                    val availMem = memoryInfo.availMem.formatSize()

                    // Build our own display so a Xiaomi string change cannot
                    // leave a stray separator behind.
                    val display = "$availMem  |  $totalMem"

                    (it.thisObject.getObjectField("mTxtMemoryInfo1") as TextView).text = display
                    (it.thisObject.getObjectField("mTxtMemoryInfo2") as TextView).text = display
                }
            }
    }
}
