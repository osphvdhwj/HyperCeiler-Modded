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

import android.view.*
import android.widget.*
import com.harry.hyperhand.hook.module.base.BaseHook
import com.harry.hyperhand.hook.utils.getObjectField
import com.harry.hyperhand.hook.utils.hookAfterMethod

object RemoveIcon : BaseHook() {
    override fun init() {
        "com.miui.home.recents.views.TaskViewHeader".hookAfterMethod("onFinishInflate") {
            val mImage = it.thisObject.getObjectField("mIconView") as ImageView
            mImage.visibility = View.GONE
        }
    }
}
