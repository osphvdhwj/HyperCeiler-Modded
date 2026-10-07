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

import android.view.View
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClass
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createBeforeHook

/**
 * Scales the control-center background blur radius.
 *
 * Hook target: miui.systemui.util.MiBlurCompat.setMiBackgroundBlurRadiusCompat(View, int)
 * — this is the single setter every CC panel (brightness / media / volume /
 * toggle-slider / tiles) calls when it applies its background blur.
 *
 * The `before` callback inspects the target View's ancestry and only
 * rewrites the radius when the view lives inside the control-center
 * hierarchy. Everything else (notification shade, lock-screen, recents)
 * passes through untouched.
 *
 * User preference: percentage of the original radius.
 *   100 = system default (no change)
 *   0   = effectively disabled (clamped to 1px so the view stays blur-capable)
 *   200 = double the system radius, capped at 100 (framework limit)
 */
object ControlCenterBlurRadius {

    private const val PREF = "system_ui_control_center_blur_radius_scale"

    fun init(classLoader: ClassLoader) {
        val miBlurCompat = loadClass("miui.systemui.util.MiBlurCompat", classLoader)

        miBlurCompat.methodFinder()
            .filterByName("setMiBackgroundBlurRadiusCompat")
            .filterByParamTypes(View::class.java, Int::class.javaPrimitiveType!!)
            .first()
            .createBeforeHook { param ->
                val view = param.args[0] as? View ?: return@createBeforeHook
                val original = param.args[1] as? Int ?: return@createBeforeHook
                if (original <= 0) return@createBeforeHook      // "disable" — leave alone
                if (!isCCView(view)) return@createBeforeHook    // not control center — skip

                val scale = com.sevtinge.hyperceiler.hook.utils.prefs.PrefsUtils.mPrefsMap
                    .getInt(PREF, 100)
                if (scale <= 0 || scale == 100) return@createBeforeHook

                val scaled = (original * scale / 100).coerceIn(1, 100)
                param.args[1] = scaled
            }
    }

    /**
     * Walk up to 30 ancestors of the given view; return true when any of
     * them is a control-center class. Uses simple name matching so the
     * lookup survives HyperOS renames of the exact classes.
     */
    private fun isCCView(start: View?): Boolean {
        var v: View? = start
        var depth = 0
        while (v != null && depth < 30) {
            val n = v.javaClass.name
            if (n.contains("controlcenter", ignoreCase = true) ||
                n.contains("ControlCenter", ignoreCase = false) ||
                n.contains("qs.QSCard") ||
                n.contains("MainPanel") ||
                n.contains("ToggleSlider")) {
                return true
            }
            v = (v.parent as? View)
            depth++
        }
        return false
    }
}
