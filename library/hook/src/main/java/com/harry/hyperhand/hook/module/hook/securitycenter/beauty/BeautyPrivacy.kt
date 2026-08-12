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
package com.harry.hyperhand.hook.module.hook.securitycenter.beauty

import com.harry.hyperhand.hook.module.base.BaseHook
import com.harry.hyperhand.hook.module.base.dexkit.DexKit
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHooks
import java.lang.reflect.Method

object BeautyPrivacy : BaseHook() {
    private val R0 by lazy<Method> {
        DexKit.findMember("BeautyPrivacy") {
            it.findMethod {
                matcher {
                    usingEqStrings("persist.sys.privacy_camera")
                }
            }.single()
        }
    }

    private val invokeMethod by lazy<List<Method>> {
        DexKit.findMemberList("BeautyPrivacyList") {
            it.findMethod {
                matcher {
                    declaredClass {
                        usingEqStrings("persist.sys.privacy_camera")
                    }
                    paramTypes = emptyList()
                    returnType = "boolean"
                    addInvoke {
                        declaredClass {
                            usingEqStrings("persist.sys.privacy_camera")
                        }
                        returnType = R0.returnType.name
                        paramTypes = listOf(R0.parameterTypes[0].name)
                    }
                }
            }
        }
    }

    override fun init() {
        R0.createHook {
            before {
                it.args[0] = true
            }
        }

        invokeMethod.createHooks {
            returnConstant(true)
        }
    }
}
