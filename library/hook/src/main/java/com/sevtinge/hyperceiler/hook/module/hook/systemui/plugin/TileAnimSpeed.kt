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

import android.animation.ValueAnimator
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClass
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createAfterHook

/**
 * Rescales the duration of the Quick Settings tile state-change animation.
 *
 * The QSTileViewImpl constructor builds a single ValueAnimator with a
 * hardcoded 350 ms duration and stores it in the `singleAnimator` field.
 * Every subsequent color transition on that tile view runs through this
 * one animator.
 *
 * We hook the constructor's after() phase, read the user's speed
 * preference, and apply a proportional scale to `singleAnimator.duration`:
 *   50%  = twice as fast  (175 ms)
 *   100% = system default (350 ms)
 *   200% = half speed     (700 ms)
 *   0%   = instant        (animation disabled by duration = 1 ms)
 *
 * Verification (MiuiSystemUI.apk, HyperOS 2):
 *   QSTileViewImpl.java:75   public final ValueAnimator singleAnimator;
 *   QSTileViewImpl.java:119  ValueAnimator valueAnimator = new ValueAnimator();
 *   QSTileViewImpl.java:120  valueAnimator.setDuration(350L);
 */
object TileAnimSpeed : BaseHook() {

    private const val PREF = "system_ui_control_center_tile_anim_speed"
    private const val BASE_DURATION = 350L

    // Abstract BaseHook.init() — never called by our Triple-driven path,
    // but must exist to satisfy the abstract base. Real work goes through
    // init(ClassLoader) below.
    override fun init() = Unit

    // Entry point used by NewPluginHelperKt's Triple list.
    // The list types each entry as Triple<String, Boolean, (ClassLoader) -> Unit>.
    fun init(classLoader: ClassLoader) {
        val cls = loadClass(
            "com.android.systemui.qs.tileimpl.QSTileViewImpl",
            classLoader
        )

        // Every constructor variant; hook by matching against ValueAnimator
        // field name so we don't depend on the exact ctor signature list.
        cls.methodFinder()
            .filterByName("<init>")
            .toList()
            .forEach { m ->
                m.createAfterHook { param ->
                    val speed = com.sevtinge.hyperceiler.hook.utils.prefs.PrefsUtils.mPrefsMap
                        .getInt(PREF, 100)
                    if (speed == 100) return@createAfterHook

                    val field = try {
                        cls.getDeclaredField("singleAnimator")
                    } catch (t: Throwable) {
                        return@createAfterHook
                    }
                    field.isAccessible = true
                    val animator = field.get(param.thisObject) as? ValueAnimator
                        ?: return@createAfterHook

                    val newDuration = if (speed <= 0) {
                        1L
                    } else {
                        (BASE_DURATION * speed / 100).coerceIn(1L, 5000L)
                    }
                    animator.duration = newDuration
                }
            }
    }
}
