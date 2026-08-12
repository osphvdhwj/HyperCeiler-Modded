/*
 * This file is part of HyperHand.
 *
 * HyperHand is free software: you can redistribute it and/or modify
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
 * Copyright (C) 2023-2025 HyperHand Contributions
 */

package com.harry.hyperhand.hook.module.hook.systemui.controlcenter;

import com.harry.hyperhand.hook.module.base.BaseHook;

public class RemoveNotifNumLimit extends BaseHook {
    @Override
    public void init() throws NoSuchMethodException {
        findAndHookMethod("com.android.systemui.statusbar.notification.collection.coordinator.CountLimitCoordinator", "attach", "com.android.systemui.statusbar.notification.collection.NotifPipeline", new replaceHookedMethod() {
            @Override
            protected Object replace(MethodHookParam param) throws Throwable {
                return null;
            }
        });

        try {
            findAndHookMethod("com.android.systemui.statusbar.notification.collection.coordinator.CountLimitCoordinator$$ExternalSyntheticLambda0", "onViewBound", "com.android.systemui.statusbar.notification.collection.NotificationEntry", new replaceHookedMethod() {
                @Override
                protected Object replace(MethodHookParam param) throws Throwable {
                    return null;
                }
            });
        } catch (Throwable t) {
            findAndHookMethod("com.android.systemui.statusbar.notification.collection.coordinator.CountLimitCoordinator$$ExternalSyntheticLambda0", "onViewBound$1", "com.android.systemui.statusbar.notification.collection.NotificationEntry", new replaceHookedMethod() {
                @Override
                protected Object replace(MethodHookParam param) throws Throwable {
                    return null;
                }
            });
        }

    }
}
