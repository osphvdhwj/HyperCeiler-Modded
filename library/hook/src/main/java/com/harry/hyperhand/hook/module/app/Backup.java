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
package com.harry.hyperhand.hook.module.app;

import com.hchen.database.HookBase;
import com.harry.hyperhand.hook.module.base.BaseModule;
import com.harry.hyperhand.hook.module.hook.backup.AllowBackupAllApps;
import com.harry.hyperhand.hook.module.hook.backup.UnlockBrokenScreenBackup;

@HookBase(targetPackage = "com.miui.backup")
public class Backup extends BaseModule {

    @Override
    public void handleLoadPackage() {
        initHook(new UnlockBrokenScreenBackup(), mPrefsMap.getBoolean("backup_unlock_broken_screen_backup"));
        initHook(new AllowBackupAllApps(), mPrefsMap.getBoolean("backup_allow_all_apps"));
    }
}
