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
package com.harry.hyperhand.hook.module.hook.systemframework

import android.content.ComponentName
import android.content.Intent
import com.harry.hyperhand.hook.module.base.BaseHook
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClass
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook

object NativeKeepNotes : BaseHook() {
    override fun init() {
        runCatching {
            loadClass("android.app.Instrumentation").methodFinder()
                .filterByName("execStartActivity")
                .firstOrNull()?.createHook {
                    before { param ->
                        val intent = param.args.firstOrNull { it is Intent } as? Intent
                        if (intent?.component?.packageName == "com.miui.notes") {
                            intent.component = ComponentName("com.google.android.keep", "com.google.android.keep.activities.BrowseActivity")
                        }
                    }
                }
        }.onFailure {
            logE(TAG, this.lpparam.packageName, "NativeKeepNotes initialization failed: $it")
        }
    }
}
