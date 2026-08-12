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
import com.harry.hyperhand.hook.module.hook.gallery.ChangeBackupServer;
import com.harry.hyperhand.hook.module.hook.gallery.EnableHdrEnhance;
import com.harry.hyperhand.hook.module.hook.gallery.EnableIdPhoto;
import com.harry.hyperhand.hook.module.hook.gallery.EnableMagicMatting;
import com.harry.hyperhand.hook.module.hook.gallery.EnableOcr;
import com.harry.hyperhand.hook.module.hook.gallery.EnableOcrForm;
import com.harry.hyperhand.hook.module.hook.gallery.EnablePdf;
import com.harry.hyperhand.hook.module.hook.gallery.EnablePhotoMovie;
import com.harry.hyperhand.hook.module.hook.gallery.EnableVideoPost;
import com.harry.hyperhand.hook.module.hook.gallery.UnPrivacyWatermark;

@HookBase(targetPackage = "com.miui.gallery")
public class Gallery extends BaseModule {

    @Override
    public void handleLoadPackage() {
        initHook(new UnPrivacyWatermark(), mPrefsMap.getBoolean("gallery_enable_un_privacy_watermark"));
        initHook(new EnableHdrEnhance(), mPrefsMap.getBoolean("gallery_enable_hdr_enhanced"));
        initHook(new EnablePdf(), mPrefsMap.getBoolean("gallery_enable_pdf"));
        initHook(new EnablePhotoMovie(), mPrefsMap.getBoolean("gallery_enable_photo_movie"));
        initHook(new EnableIdPhoto(), mPrefsMap.getBoolean("gallery_enable_id_photo"));
        initHook(new EnableMagicMatting(), mPrefsMap.getBoolean("gallery_enable_magic_matting"));
        initHook(new EnableVideoPost(), mPrefsMap.getBoolean("gallery_enable_video_post"));
        initHook(new EnableVideoPost(), mPrefsMap.getBoolean("gallery_enable_video_editor"));
        initHook(new EnableOcr(), mPrefsMap.getBoolean("gallery_enable_ocr"));
        initHook(new EnableOcrForm(), mPrefsMap.getBoolean("gallery_enable_ocr_form"));
        initHook(new ChangeBackupServer(), mPrefsMap.getStringAsInt("gallery_backup_server", 0) != 0);
    }
}
