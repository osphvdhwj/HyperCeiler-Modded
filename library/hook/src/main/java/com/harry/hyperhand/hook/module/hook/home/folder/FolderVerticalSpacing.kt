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
package com.harry.hyperhand.hook.module.hook.home.folder

import android.widget.*
import com.harry.hyperhand.hook.utils.devicesdk.DisplayUtils.*
import com.harry.hyperhand.hook.module.base.BaseHook
import com.harry.hyperhand.hook.utils.findClass
import com.harry.hyperhand.hook.utils.hookAfterAllMethods
import de.robv.android.xposed.*

object FolderVerticalSpacing : BaseHook() {
    override fun init() {

        val verticalSpacing = mPrefsMap.getInt("home_folder_vertical_spacing", 0)
        if (verticalSpacing <= 0) return
        "com.miui.home.launcher.Folder".findClass().hookAfterAllMethods(
            "bind"
        ) {
            val mContent = XposedHelpers.getObjectField(it.thisObject, "mContent") as GridView
            mContent.verticalSpacing = dp2px(verticalSpacing.toFloat())
        }

    }
}
