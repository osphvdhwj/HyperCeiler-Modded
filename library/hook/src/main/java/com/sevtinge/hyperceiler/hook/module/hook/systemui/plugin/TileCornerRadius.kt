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

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClassOrNull

/**
 * Scales the corner radius of control-center tiles.
 *
 * Verified against MiuiSystemUIPlugin.apk (HyperOS 2):
 *
 *   miui.systemui.controlcenter.qs.tileview.QSCardItemView
 *       public void setCornerRadius(float r)
 *           -> getBackground() as GradientDrawable -> setCornerRadius(r)
 *
 *   miui.systemui.controlcenter.qs.tileview.QSTileItemIconView
 *       public void setCornerRadius(float r)
 *           -> gradientDrawable.setCornerRadius(r)
 *           (two different drawables)
 *
 * Both readers pull the default from
 *   R.dimen.control_center_universal_corner_radius
 * and pass it here. We intercept that value before it hits the drawable.
 *
 * User preference: percentage of the original radius.
 *   100 = system default
 *   0   = square tiles (radius 0)
 *   200 = double radius, capped at 200 px to avoid clipping
 */
object TileCornerRadius : BaseHook() {

    private const val PREF = "system_ui_control_center_tile_corner_scale"
    private const val MAX_PX = 200f

    override fun init() {
        hookSetter("miui.systemui.controlcenter.qs.tileview.QSCardItemView")
        hookSetter("miui.systemui.controlcenter.qs.tileview.QSTileItemIconView")
    }

    private fun hookSetter(className: String) {
        val cls = loadClassOrNull(className, lpparam.classLoader) ?: return
        findAndHookMethodSilently(cls, "setCornerRadius", Float::class.javaPrimitiveType,
            object : MethodHook() {
                override fun before(param: MethodHookParam) {
                    val original = param.args[0] as? Float ?: return
                    val scale = com.sevtinge.hyperceiler.hook.utils.prefs.PrefsUtils.mPrefsMap
                        .getInt(PREF, 100)
                    if (scale == 100) return
                    val scaled = (original * scale / 100f).coerceIn(0f, MAX_PX)
                    param.args[0] = scaled
                }
            })
    }
}
