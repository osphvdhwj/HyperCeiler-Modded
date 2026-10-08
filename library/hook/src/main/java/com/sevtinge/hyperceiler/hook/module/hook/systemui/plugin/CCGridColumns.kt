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
package com.sevtinge.hyperceiler.hook.module.hook.systemui.plugin

import androidx.recyclerview.widget.GridLayoutManager
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.getObjectField
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClassOrNull
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createAfterHook

/**
 * Number of columns in the HyperOS 2 control-center quick-settings grid.
 *
 * Verified target (MiuiSystemUIPlugin.apk decompile):
 *
 *   miui.systemui.controlcenter.panel.main.recyclerview.MainPanelAdapter
 *       constructor:
 *         GridLayoutManager gridLayoutManager = new GridLayoutManager(this.context, 4);
 *       field: private final GridLayoutManager layoutManager
 *
 *       Inner class MainPanelAdapter$layoutManager$1$1 (a
 *       GridLayoutManager.SpanSizeLookup) hardcodes the maximum
 *       per-item span as 4 inside f.h(x, 1, 4).
 *
 * To make a wider/narrower grid work, we must do BOTH:
 *   1. setSpanCount(N) on the GridLayoutManager stored in layoutManager.
 *   2. Scale the SpanSizeLookup result from its 1..4 range to 1..N, so
 *      a full-width card (originally span 4) still fills the row.
 *
 * Both hooks are installed once per plugin classloader load. Rotation
 * recreates the adapter, so the constructor hook fires again and the
 * new span count is applied automatically.
 */
object CCGridColumns : BaseHook() {

    private const val PREF = "system_ui_control_center_grid_columns"
    private const val DEFAULT = 4
    private const val MIN = 3
    private const val MAX = 7

    private const val ADAPTER_CLS =
        "miui.systemui.controlcenter.panel.main.recyclerview.MainPanelAdapter"
    private const val SPAN_LOOKUP_CLS =
        "miui.systemui.controlcenter.panel.main.recyclerview.MainPanelAdapter\$layoutManager\$1\$1"

    fun init(classLoader: ClassLoader) {
        val cols = com.sevtinge.hyperceiler.hook.utils.prefs.PrefsUtils.mPrefsMap
            .getInt(PREF, DEFAULT)
            .coerceIn(MIN, MAX)
        if (cols == DEFAULT) return

        hookSpanCount(classLoader, cols)
        hookSpanLookup(classLoader, cols)
    }

    private fun hookSpanCount(classLoader: ClassLoader, cols: Int) {
        val adapterCls = loadClassOrNull(ADAPTER_CLS, classLoader) ?: return

        adapterCls.methodFinder()
            .filterByName("<init>")
            .toList()
            .forEach { ctor ->
                ctor.createAfterHook {
                    try {
                        val lm = it.thisObject.getObjectField("layoutManager")
                                as? GridLayoutManager ?: return@createAfterHook
                        if (lm.spanCount != cols) {
                            lm.spanCount = cols
                        }
                    } catch (_: Throwable) { }
                }
            }
    }

    private fun hookSpanLookup(classLoader: ClassLoader, cols: Int) {
        val lookupCls = loadClassOrNull(SPAN_LOOKUP_CLS, classLoader) ?: return

        lookupCls.methodFinder()
            .filterByName("getSpanSize")
            .filterByParamTypes(Int::class.javaPrimitiveType!!)
            .firstOrNull()
            ?.createAfterHook {
                val original = it.result as? Int ?: return@createAfterHook
                if (original <= 0) return@createAfterHook
                // Original is 1..4 (upstream clamp). A full-width item is 4.
                // Scale so 4 -> cols and 1 -> max(1, cols/4).
                val scaled = if (original >= 4) {
                    cols
                } else {
                    (original * cols / 4).coerceAtLeast(1)
                }
                it.result = scaled
            }
    }
}
